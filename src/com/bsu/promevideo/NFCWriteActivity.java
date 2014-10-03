package com.bsu.promevideo;

import com.bsu.promevideo.tools.NFCDataUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class NFCWriteActivity extends Activity {
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private EditText et_page,et_data;		//页码编辑框，数据编辑框
	private String writePage,writeData;		//页码号，写入数据
	private CheckBox cb_cleardata;			//清除当页数据
	private boolean flag_cleardata;			//清除当页数据标识
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfcwrite);
		
		et_page = (EditText) findViewById(R.id.et_page);	//写入页页码
		et_data = (EditText) findViewById(R.id.et_data);	//写入的数据
		writePage = et_page.getText().toString();			//获得写入的页码
		cb_cleardata = (CheckBox) findViewById(R.id.cb_cleardata);
//		bt_cleardata.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View arg0) {
//				
//			}});
		
		mAdapter = NfcAdapter.getDefaultAdapter(this); 		// 实例化NFC设备

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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); 								// 获得标签支持的技术
			
			String tagtype = NFCDataUtils.witchMifareType(tag);
			if(tagtype.equals("MifareClassic")){

			}else if(tagtype.equals("MifareUltralight")){
				byte[] bytes;
				int page = Integer.parseInt(et_page.getText().toString());
				if(cb_cleardata.isChecked()){
					bytes = new byte[]{0x00,0x00,0x00,0x00};
					
				}else{
					writeData = et_data.getText().toString();
					bytes = writeData.getBytes();			//所有要写入的数据
				}
				try {
					NFCDataUtils.writeMifareUltralightData(tag, page, bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}else{
				
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mAdapter != null)
			mAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null)
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,mTechLists);
	}
}
