package com.bsu.promevideo;

import java.io.IOException;
import java.nio.charset.Charset;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NFCTestActivity extends Activity {

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private TextView mText;
	private int mCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfctest);

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

		// onResume gets called after this to handle the intent
		// setIntent(intent);
		// 获得个新意图后执行如下操作
		mText.setText("Discovered tag " + ++mCount + " with intent:" + intent);
		// 读取数据
		// 获得intent里的内容
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] msgs = getNdefMessages(intent);
			String body = new String(msgs[0].getRecords()[0].getPayload());
			mText.setText(body);
		} else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Parcelable[] resMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES); // 获得Ndef消息
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // 获得标签支持的技术
			// 701: TAG: Tech [android.nfc.tech.MifareClassic,android.nfc.tech.NfcA, android.nfc.tech.NdefFormatable]
			// 智能标签: TAG: Tech [android.nfc.tech.MifareUltralight,android.nfc.tech.NfcA, android.nfc.tech.Ndef]
			// 扣: TAG: Tech [android.nfc.tech.MifareClassic,android.nfc.tech.NfcA, android.nfc.tech.NdefFormatable]

			String str = readTag(tag);
			System.out.println(str);
			
			
			// NdefFormatable ndef = NdefFormatable.get(tag);
			// Ndef ndef = Ndef.get(tag);
			// NfcA ndef = NfcA.get(tag);
			// MifareClassic tech = MifareClassic.get(tag);
			MifareUltralight tech = MifareUltralight.get(tag);

//			try {
//				tech.connect();
//				byte[] nm = tech.readPages(0);
//				tag.toString();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

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
	//
	// // 字符序列转换为16进制字符串
	/**
	 * 字符序列转换为16进制字符串
	 * 
	 * @param src
	 * @return
	 */
	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
//			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	/**
	 * 读取ndef数据
	 */
	private void readNFCTag() {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msgs[] = null;
			int contentSize = 0;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
					contentSize += msgs[i].toByteArray().length;
				}
			}
			try {
				if (msgs != null) {
					NdefRecord record = msgs[0].getRecords()[0];
					// TextRecord textRecord = TextRecord.parse(record);
					// mTagText += textRecord.getText() + "\n\ntext\n"
					// + contentSize + " bytes";
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/**
	 * 读取MifareUltralight数据
	 * @param tag
	 * @return
	 */
	public String readTag(Tag tag) {
		MifareUltralight ultralight = MifareUltralight.get(tag);

		try {
			ultralight.connect();
			// MIFARE Ultralight C Tag 结构 每页4个字节,前4页是厂商信息,每次读4页
			String str = "";
			for(int i=0;i<44;i+=4){
				str+="page "+i+":";
				str+=bytesToHexString(ultralight.readPages(i))+"\n";
			}
			System.out.println(ultralight.getType());
			//写入数据,一次只能写1页4个字节
			ultralight.writePage(9, new byte[]{1,1,1,1});
			
			return str;
//			byte[] data = ultralight.readPages(5);
//			return new String(datas, Charset.forName("GB2312"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ultralight.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return null;
	}

//	private void writeTag(){
//		
//	}
	
}
