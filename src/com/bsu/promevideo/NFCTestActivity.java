package com.bsu.promevideo;

import java.io.IOException;

import com.bsu.promevideo.tools.NFCDataUtils;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NFCTestActivity extends Activity {

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private TextView mText,tv_actiontype,tv_tagtype;
	private Button bt_write;
	private int mCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfctest);

		tv_actiontype = (TextView)findViewById(R.id.tv_actiontype);	//action类型
		tv_tagtype = (TextView)findViewById(R.id.tv_tagtype);		//tag类型
		
		bt_write = (Button) findViewById(R.id.bt_write);
		bt_write.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(NFCTestActivity.this,NFCWriteActivity.class);
				NFCTestActivity.this.startActivity(intent);
			}});
		
		mText = (TextView) findViewById(R.id.txtBeam);
		mText.setText("Scan a tag");

		mAdapter = NfcAdapter.getDefaultAdapter(this); // 实例化NFC设备

		// 截获Intent,使用前台Activity
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		tech.addDataScheme("vnd.android.nfc");
		mFilters = new IntentFilter[] { ndef, tech,
				new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED), };
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null)
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,mTechLists);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// 701: TAG: Tech [android.nfc.tech.MifareClassic,android.nfc.tech.NfcA, android.nfc.tech.NdefFormatable]
		// 智能标签: TAG: Tech [android.nfc.tech.MifareUltralight,android.nfc.tech.NfcA, android.nfc.tech.Ndef]
		// 扣: TAG: Tech [android.nfc.tech.MifareClassic,android.nfc.tech.NfcA, android.nfc.tech.NdefFormatable]
		
		// 获得个新意图后执行如下操作
		mText.setText("Discovered tag " + ++mCount + " with intent:" + intent);
		tv_actiontype.setText(NFCDataUtils.simpleActionType(intent.getAction().toString()));
		// 读取数据
		// 获得intent里的内容
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] msgs = getNdefMessages(intent);
			String body = new String(msgs[0].getRecords()[0].getPayload());
			mText.setText(body);
		} else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
//			Parcelable[] resMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES); 	// 获得Ndef消息
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); 								// 获得标签支持的技术
			tv_tagtype.setText(NFCDataUtils.getTechList2String(tag));
			
			String tagtype = NFCDataUtils.witchMifareType(tag);
			if(tagtype.equals("MifareClassic")){
				mText.setText(NFCDataUtils.readMifareClassicData(tag));	
			}else if(tagtype.equals("MifareUltralight")){
				mText.setText(NFCDataUtils.readMifareUltralightData(tag));
			}else{
				
			}
		}
	}

	private NdefMessage[] getNdefMessages(Intent intent) {
		NdefMessage[] msgs = null;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				byte[] empty = new byte[] {};
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
						empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
		} else {
			finish();
		}
		return msgs;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mAdapter != null)
			mAdapter.disableForegroundDispatch(this);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	// private void processIntent(Intent intent) {
	// // 从intent中获得tag数据
	// Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	// for (String tech : tagFromIntent.getTechList())
	// System.out.println(">>>>>>>" + tech);
	//
	// boolean auth = false;
	// // 读取TAG
	// MifareClassic mfc = MifareClassic.get(tagFromIntent);
	// try {
	// String metaInfo = "";
	// mfc.connect();
	// int type = mfc.getType();
	// int sectorCount = mfc.getSectorCount();
	// String typeS = "";
	// switch (type) {
	// case MifareClassic.TYPE_CLASSIC:
	// typeS = "TYPE_CLASSIC";
	// break;
	// case MifareClassic.TYPE_PLUS:
	// typeS = "TYPE_PLUS";
	// break;
	// case MifareClassic.TYPE_PRO:
	// typeS = "TYPE_PRO";
	// break;
	// case MifareClassic.TYPE_UNKNOWN:
	// typeS = "TYPE_UNKNOWN";
	// break;
	// }
	// metaInfo += "卡片类型:" + typeS + "\n共" + sectorCount + "个扇区\n共"
	// + mfc.getBlockCount() + "个块\n存储空间:" + mfc.getSize() + "B\n";
	// for (int j = 0; j < sectorCount; j++) {
	// auth = mfc.authenticateSectorWithKeyA(j,
	// MifareClassic.KEY_DEFAULT);
	// int bCount;
	// int bIndex;
	// if (auth) {
	// metaInfo += "Sector " + j + ":验证成功\n";
	// // 读取扇区中的块
	// bCount = mfc.getBlockCountInSector(j);
	// bIndex = mfc.sectorToBlock(j);
	// for (int i = 0; i < bCount; i++) {
	// byte[] data = mfc.readBlock(bIndex);
	// metaInfo += "Block " + bIndex + ":"
	// + bytesToHexString(data) + "\n";
	// bIndex++;
	// }
	// }
	// }
	// mText.setText(metaInfo);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// Parcelable[] rawMsgs =
	// intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	// // only one message sent during the beam
	// NdefMessage msg = (NdefMessage) rawMsgs[0];
	// // NDEF:NFC Data Exchange Format，即NFC数据交换格式
	// // record 0 contains the MIME type, record 1 is the AAR, if present
	// mText.setText(new String(msg.getRecords()[0].getPayload()));
	// }

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record
	 * 
	 * @param mimeType
	 */
	// public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
	// byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
	// NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	// mimeBytes, new byte[0], payload);
	// return mimeRecord;
	// }
	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // If NFC is not available, we won't be needing this menu
	// if (mNfcAdapter == null) {
	// return super.onCreateOptionsMenu(menu);
	// }
	// // MenuInflater inflater = getMenuInflater();
	// // inflater.inflate(menu.CATEGORY_SYSTEM, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// // case R.id.menu_settings:
	// case 0:
	// Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
	// startActivity(intent);
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }
}
