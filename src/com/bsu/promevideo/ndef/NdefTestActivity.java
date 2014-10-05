package com.bsu.promevideo.ndef;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import com.bsu.promevideo.R;
import com.bsu.promevideo.tools.NFCDataUtils;
import com.bsu.promevideo.tools.TextRecord;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.CheckBox;
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
	private TextView tv;
	private CheckBox cb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ndef_test);

		tv = (TextView) findViewById(R.id.tv_ndeftest);
		cb = (CheckBox) findViewById(R.id.cb_iswrite);

		// 初始化设备
		adapter = NfcAdapter.getDefaultAdapter(this);
		// 截获Intent,使用当前的Activity
		pintent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 获取表示当前标签的对象
		Tag tag = intent.getParcelableExtra(adapter.EXTRA_TAG);
		// 向标签写入Package
		writeNFCTag(tag);
		// Ndef ndef = Ndef.get(tag);
		// NdefMessage nm;
		// String str = "";
		// String mTagText = "";
		// try {
		// nm = ndef.getNdefMessage();
		//
		//
		// NdefRecord ndefr = nm.getRecords()[0];
		// TextRecord textRecord = TextRecord.parse(ndefr);
		// //获取实际的数据占用的大小，并显示在窗口上
		// mTagText += textRecord.getText() + "\n\n纯文本\n";
		//
		// tv.setText(mTagText+"\n");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (FormatException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// 读取标签中的数据
		readNFCTag(intent, tag);
	}

	private void readNFCTag(Intent intent, Tag tag) {
		String mTagText = "";

		System.out.println("============readNFCTag  " + intent.getAction());

		// 判断是否为ACTION_NDEF_DISCOVERED
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			// 从标签读取数据（Parcelable对象）
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

			System.out.println("==============" + rawMsgs.length);

			NdefMessage msgs[] = null;
			int contentSize = 0;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				// 标签可能存储了多个NdefMessage对象，一般情况下只有一个NdefMessage对象
				for (int i = 0; i < rawMsgs.length; i++) {
					// 转换成NdefMessage对象
					msgs[i] = (NdefMessage) rawMsgs[i];
					// 计算数据的总长度
					contentSize += msgs[i].toByteArray().length;

				}
			}
			try {
				System.out.println("=============try "+msgs[0].toString());
				if (msgs != null) {
					// 程序中只考虑了1个NdefRecord对象，若是通用软件应该考虑所有的NdefRecord对象
					NdefRecord record = msgs[0].getRecords()[0];
					// 分析第1个NdefRecorder，并创建TextRecord对象
					TextRecord textRecord = TextRecord.parse(record);
					// 获取实际的数据占用的大小，并显示在窗口上
					mTagText += textRecord.getText() + "\n\n纯文本\n"+ contentSize + " bytes";
					System.out.println("==========="+mTagText);
					tv.setText(mTagText);
				}

			} catch (Exception e) {
				e.printStackTrace();
//				tv.setText(e.getMessage());
			}
		}
	}

	// 向标签写入数据
	public void writeNFCTag(Tag tag) {
		// 必须要指定一个Tag对象
		if (tag == null) {
			Toast.makeText(this, "NFC Tag未建立连接", Toast.LENGTH_LONG).show();
			return;
		}
		// 创建NdefMessage对象
		// NdefRecord.creatApplicationRecord方法创建一个封装Package的NdefRecord对象
		NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {createTextRecord("bk42-lr001"),  NdefRecord.createApplicationRecord("com.bsu.promevideo") });
//		NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {createTextRecord("bk42-lr001")});
		// 获取NdefMessage对象的尺寸
		int size = ndefMessage.toByteArray().length;

		try {
			// 获取Ndef对象
			Ndef ndef = Ndef.get(tag);
			// 处理NDEF格式的数据
			if (ndef != null) {
				// 允许对标签进行IO操作，连接
				ndef.connect();
				// NFC标签不是可写的（只读的）
				if (!ndef.isWritable()) {
					Toast.makeText(this, "NFC Tag是只读的！", Toast.LENGTH_LONG)
							.show();
					return;
				}
				// NFC标签的空间不足
				if (ndef.getMaxSize() < size) {
					Toast.makeText(this, "NFC Tag的空间不足！", Toast.LENGTH_LONG)
							.show();
					return;
				}
				// 向NFC标签写入数据
				ndef.writeNdefMessage(ndefMessage);
				Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_LONG).show();
			} else {
				// 创建NdefFormatable对象
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						// 允许标签IO操作，进行连接
						format.connect();
						// 重新格式化NFC标签，并写入数据
						format.format(ndefMessage);
						Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_LONG)
								.show();
					} catch (Exception e) {
						Toast.makeText(this, "写入NDEF格式数据失败！", Toast.LENGTH_LONG)
								.show();
					}
				} else {
					Toast.makeText(this, "NFC标签不支持NDEF格式！", Toast.LENGTH_LONG)
							.show();
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	//创建一个封装要写入的文本的NdefRecord对象  
    public NdefRecord createTextRecord(String text) {  
        //生成语言编码的字节数组，中文编码  
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(  
                Charset.forName("US-ASCII"));  
        //将要写入的文本以UTF_8格式进行编码  
        Charset utfEncoding = Charset.forName("UTF-8");  
        //由于已经确定文本的格式编码为UTF_8，所以直接将payload的第1个字节的第7位设为0  
        byte[] textBytes = text.getBytes(utfEncoding);  
        int utfBit = 0;  
        //定义和初始化状态字节  
        char status = (char) (utfBit + langBytes.length);  
        //创建存储payload的字节数组  
        byte[] data = new byte[1 + langBytes.length + textBytes.length];  
        //设置状态字节  
        data[0] = (byte) status;  
        //设置语言编码  
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);  
        //设置实际要写入的文本  
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length,  
                textBytes.length);  
        //根据前面设置的payload创建NdefRecord对象  
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  
                NdefRecord.RTD_TEXT, new byte[0], data);  
        return record;  
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
}
