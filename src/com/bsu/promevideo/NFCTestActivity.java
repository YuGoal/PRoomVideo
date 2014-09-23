package com.bsu.promevideo;

import java.nio.charset.Charset;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.tech.MifareClassic;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class NFCTestActivity extends Activity implements CreateNdefMessageCallback,OnNdefPushCompleteCallback{

	NfcAdapter mNfcAdapter;
	TextView tv_msg;
	private static final int MESSAGE_SENT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfctest);
		
		tv_msg = (TextView) findViewById(R.id.txtBeam);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);					//实例化NFC设备
		if(mNfcAdapter == null){
			//如果没发现nfc设备,给出提示并返回
			tv_msg.setText("NFC is not available on this device.");
			return;
		}else if(mNfcAdapter!=null){
			if(!mNfcAdapter.isEnabled()){
				tv_msg.setText("请先启用NFC功能");
				return;
			}else{
				Toast.makeText(this, "启动NFC注册成功...", Toast.LENGTH_LONG).show();
				mNfcAdapter.setNdefPushMessageCallback(this, this);		//注册NDEF回调消息
				mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
			}
		}
	}
	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
		time.setToNow();
		String text = ("Beam me up!nn "+"Beam Time:"+time.format("%H:%M:%S"));
		NdefMessage msg = new NdefMessage(new NdefRecord[]{
				createMimeRecord("application/com.bsu.promevideo",text.getBytes())
		});
		return msg;
	}
	
	 @Override  
	    public void onNdefPushComplete(NfcEvent arg0) {  
	        // A handler is needed to send messages to the activity when this  
	        // callback occurs, because it happens from a binder thread  
	        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();  
	    }  
	  
	    private final Handler mHandler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {  
	            switch (msg.what) {  
	            case MESSAGE_SENT:  
	                Toast.makeText(getApplicationContext(), "Message sent!",  
	                        Toast.LENGTH_LONG).show();  
	                break;  
	            }  
	        }  
	    };  
	  
	    @Override  
	    public void onResume() {  
	        super.onResume();  
	        Toast.makeText(this, "等待接受action信息...", Toast.LENGTH_SHORT).show();  
	        String action = this.getIntent().getAction();  
	        System.out.println("action:"+action);
	        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
	        	System.out.println("ACTION_NDEF_DISCOVERED");
	            processIntent(getIntent());  
	        }  else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)){
	        	System.out.println("执行ACTION_TECH_DISCOVERED");
	        } else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
	        	System.out.println("=========================执行ACTION_TAG_DISCOVERED");
	        	processIntent(getIntent());  
	        }
	  
	    }  
	  
	    @Override  
	    public void onNewIntent(Intent intent) {  
	        // onResume gets called after this to handle the intent  
	        setIntent(intent);  
	    }  
	  
	    /** 
	     * Parses the NDEF Message from the intent and prints to the TextView 
	     */  
	    void processIntent(Intent intent) {  
	    	//从intent中获得tag数据
	    	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	for(String tech :tagFromIntent.getTechList())
	    		System.out.println(">>>>>>>"+tech);
	    	
	    	boolean auth = false;
	    	//读取TAG
	    	MifareClassic mfc = MifareClassic.get(tagFromIntent);
	    	try{
	    		String metaInfo = "";
	    		mfc.connect();
	    		int type = mfc.getType();
	    		int sectorCount = mfc.getSectorCount();
	    		String typeS = "";
	    		switch(type){
	    		case MifareClassic.TYPE_CLASSIC:
	    			typeS = "TYPE_CLASSIC";
	    			break;
	    		case MifareClassic.TYPE_PLUS:
	    			typeS = "TYPE_PLUS";
	    			break;
	    		case MifareClassic.TYPE_PRO:
	    			typeS = "TYPE_PRO";
	    			break;
	    		case MifareClassic.TYPE_UNKNOWN:
	    			typeS = "TYPE_UNKNOWN";
	    			break;
	    		}
	    		metaInfo += "卡片类型:"+typeS+"\n共"+sectorCount+"个扇区\n共"+mfc.getBlockCount()+"个块\n存储空间:"+mfc.getSize()+"B\n";
	    		for(int j=0;j<sectorCount;j++){
	    			auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
	    			int bCount;
	    			int bIndex;
	    			if(auth){
	    				metaInfo+="Sector "+j+":验证成功\n";
	    				//读取扇区中的块
	    				bCount = mfc.getBlockCountInSector(j);
	    				bIndex = mfc.sectorToBlock(j);
	    				for(int i=0;i<bCount;i++){
	    					byte[] data = mfc.readBlock(bIndex);
	    					metaInfo += "Block "+bIndex+":"+bytesToHexString(data)+"\n";
	    					bIndex++;
	    				}
	    			}
	    		}
	    		tv_msg.setText(metaInfo);
	    		
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    	
//	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);  
//	        // only one message sent during the beam  
//	        NdefMessage msg = (NdefMessage) rawMsgs[0];  
//	        // NDEF:NFC Data Exchange Format，即NFC数据交换格式  
//	        // record 0 contains the MIME type, record 1 is the AAR, if present  
//	        mInfoText.setText(new String(msg.getRecords()[0].getPayload()));  
	    }  
	  
	    /** 
	     * Creates a custom MIME type encapsulated in an NDEF record 
	     * @param mimeType 
	     */  
	    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {  
	        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));  
	        NdefRecord mimeRecord = new NdefRecord(  
	        NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);  
	        return mimeRecord;  
	    }  
	  
	    @Override  
	    public boolean onCreateOptionsMenu(Menu menu) {  
	        // If NFC is not available, we won't be needing this menu  
	        if (mNfcAdapter == null) {  
	            return super.onCreateOptionsMenu(menu);  
	        }  
//	      MenuInflater inflater = getMenuInflater();  
//	      inflater.inflate(menu.CATEGORY_SYSTEM, menu);  
	        return true;  
	    }  
	  
	    @Override  
	    public boolean onOptionsItemSelected(MenuItem item) {  
	        switch (item.getItemId()) {  
//	      case R.id.menu_settings:  
	        case 0:  
	            Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);  
	            startActivity(intent);  
	            return true;  
	        default:  
	            return super.onOptionsItemSelected(item);  
	        }  
	    }  
	    
	  //字符序列转换为16进制字符串 
	    private String bytesToHexString(byte[] src) { 
	        StringBuilder stringBuilder = new StringBuilder("0x"); 
	        if (src == null || src.length <= 0) { 
	           return null; 
	        } 
	        char[] buffer = new char[2]; 
	        for (int i = 0; i < src.length; i++) { 
	            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16); 
	            buffer[1] = Character.forDigit(src[i] & 0x0F, 16); 
	            System.out.println(buffer); 
	            stringBuilder.append(buffer); 
	        } 
	        return stringBuilder.toString(); 
	    } 
}
