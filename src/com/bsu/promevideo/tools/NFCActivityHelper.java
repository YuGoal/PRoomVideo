package com.bsu.promevideo.tools;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

/**
 * 为了方便的让Activity实现nfc功能，实现的类
 * @author fengchong
 *
 */
public class NFCActivityHelper {
	private Activity context;
	private NfcAdapter adapter;
	private PendingIntent pintent;
	private IntentFilter[] filters;
	private String[][] techLists;
	private OnNFCReadListener listener;
	public NFCActivityHelper(Activity a,NfcAdapter nfca,PendingIntent pi){
		context = a;		
		adapter = nfca;
		pintent = pi;
	}
	
	/**
	 * 用于Activity的onCreate执行
	 */
	public void onCreate(){
		//初始化设备
//		adapter = NfcAdapter.getDefaultAdapter(context);
		//截获Intent,使用当前的Activity
//		pintent = PendingIntent.getActivity(context, 0, new Intent(context,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		//获得3种IntentFilter
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		tech.addDataScheme("vnd.android.nfc");
		filters = new IntentFilter[] { ndef, tech,new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED), };
	}
	/**
	 * 用于在Activity中的onResume里执行
	 */
	public void onResume(){
		if(adapter != null)
			adapter.enableForegroundDispatch(context, pintent, filters, techLists);
	}
	/**
	 * 用于在Activity中的onPause里的执行
	 */
	public void onPause(){
		if(adapter != null)
			adapter.disableForegroundDispatch(context);
	}
	/**
	 * 设置读取nfc数据的监听器
	 * @param l
	 */
	public void setOnNFCReadListener(OnNFCReadListener l){
		listener = l;
	}
	
	/**
	 * 用在Activity中的onNewIntent里的执行
	 * @param intent
	 */
	public void onNewIntent(Intent intent){
		//当读取到一个ACTION_TAG_DISCOVERED标签
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String tagtype = NFCDataUtils.witchMifareType(tag);
			if(tagtype.equals("MifareClassic")){
				if(listener!=null)
					listener.read(NFCDataUtils.readMifareClassicData(tag));
			}
			else if(tagtype.equals("MifareUltralight")){
				if(listener!=null)
					listener.read(NFCDataUtils.readMifareUltralightDataByPage(tag, 8));
//					listener.read(NFCDataUtils.readMifareUltralightData(tag));
			}
			else{}
		//当读到一个ACTION_NDEF_DISCOVERED数据
		}else if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			
		}
	}
	
}
