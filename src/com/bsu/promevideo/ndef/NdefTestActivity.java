package com.bsu.promevideo.ndef;

import java.nio.charset.Charset;
import java.util.Locale;

import com.bsu.promevideo.R;
import com.bsu.promevideo.tools.NFCDataUtils;
import com.bsu.promevideo.tools.TextRecord;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ndef标签操作测试，最简单的ndef程序
 * 
 * @author fengchong
 *
 */
public class NdefTestActivity extends Activity {
	private NfcAdapter adapter;
	private PendingIntent pintent;
	private TextView tv_readdata ;					//读取到的数据
	private TextView tv_page,tv_writedata,tv_aar;	//标签
	private CheckBox cb_iswrite,cb_cleardata;		//是否写入数据和是否清除数据
	private EditText et_page,et_data,et_aar;		//要写入数据的页，默认为第8页和要写入的数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ndef_test);

		tv_readdata = (TextView) findViewById(R.id.tv_readdata);
		tv_page = (TextView) findViewById(R.id.tv_page);
		tv_writedata = (TextView) findViewById(R.id.tv_writedata);
		tv_aar = (TextView) findViewById(R.id.tv_aar);
		cb_iswrite = (CheckBox) findViewById(R.id.cb_iswrite);
		cb_cleardata = (CheckBox) findViewById(R.id.cb_cleardata);
		et_page = (EditText) findViewById(R.id.et_page);
		et_data = (EditText) findViewById(R.id.et_data);
		et_aar = (EditText) findViewById(R.id.et_aar);
		
		cb_iswrite.setChecked(false);
		setWriteEnable(false);
		
		cb_iswrite.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					NdefTestActivity.this.setWriteEnable(true);
				else
					NdefTestActivity.this.setWriteEnable(false);
			}});
		
		// 初始化设备
		adapter = NfcAdapter.getDefaultAdapter(this);
		// 截获Intent,使用当前的Activity
		pintent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//如果写入checkbox未选中，默认读取数据
		if(!cb_iswrite.isChecked()){
			// 读取标签中的数据
			String tagdata = NFCDataUtils.readNdefData(intent);
			tv_readdata.setText(tagdata);			
		//如果写入checkbox选中
		}else{
			// 获取表示当前标签的对象
			Tag tag = intent.getParcelableExtra(adapter.EXTRA_TAG);
			//写入标签数据
			if(!this.cb_cleardata.isChecked())
				NFCDataUtils.writeNdefData(this, tag, et_data.getText().toString(),et_aar.getText().toString());	
			else
				NFCDataUtils.writeNdefData(this, tag, "",et_aar.getText().toString());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (adapter != null)
			adapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (adapter != null)
			adapter.enableForegroundDispatch(this, pintent, null, null);
	}

	/**
	 * 设置写入部分是否应用
	 * @param b
	 */
	private void setWriteEnable(boolean b){
		tv_page.setEnabled(b);
		tv_writedata.setEnabled(b);
		tv_aar.setEnabled(b);
		cb_cleardata.setEnabled(b);
		et_page.setEnabled(b);
		et_data.setEnabled(b);
		et_aar.setEnabled(b);
	}
}
