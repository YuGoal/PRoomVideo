package com.bsu.promevideo.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.widget.Toast;

/**
 * nfc数据的工具类，可帮助开发者读写各种格式的nfc数据
 * 
 * @author fengchong
 * 
 */
public class NFCDataUtils {
	/**
	 * 读取MifareUltralight数据
	 * @param tag	获得的标签对象
	 * @return 		返回读取的字符串
	 */
	public static String readMifareUltralightData(Tag tag) {
		//tt表示TagTechnology
		MifareUltralight tt = MifareUltralight.get(tag);
		try {
			tt.connect();
			// 判断是否为MifareUltralight C数据
			if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
				// MIFARE Ultralight C Tag 结构 48页 每页4个字节,前4页是厂商系统等信息,最后4页用来验证身份不可读
				// 读取数据时每次读4页
				StringBuffer sb = new StringBuffer();
				int pageCount = 48;
				for (int i = 0; i <(pageCount-4)/4 ; i++) {
					sb.append("page").append(i*4).append(":")
							.append(bytesToHexString(tt.readPages(i*4)))
							.append("\n");
				}
				return sb.toString();
			} else if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT) {
				return "";
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				tt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * 读取标签中的从startPage页到startPage页的数据
	 * @param tag			标签对象
	 * @param startPage		开始页
	 * @return				返回startPage后4页的数据的字符串形式
	 */
	public static String readMifareUltralightDataByPage(Tag tag,int startPage){
		if(startPage>44 || startPage<0)
			return null;
		MifareUltralight tt = MifareUltralight.get(tag);
		try {
			tt.connect();
			// 判断是否为MifareUltralight C数据
			if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
				// MIFARE Ultralight C Tag 结构 48页 每页4个字节,前4页是厂商系统等信息,最后4页用来验证身份不可读
				// 读取数据时每次读4页
				byte[] bytes = tt.readPages(startPage);
				String s = new String(NFCDataUtils.deleteBytesZero(bytes));
				return s;
			} else if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT) {
				return "";
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				tt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 写入MifareUltralight数据
	 * @param tag			tag对象
	 * @param pageIndex		要写入的页码索引,一般从4(第5页)开始写,前4页是系统信息
	 * @param data			要写入的数据,由于每页只包括个字节,所以data的长度只能是长度为4的byte数组
	 * @throws Exception 
	 */
	public static void writeMifareUltralightData(Tag tag,int pageIndex,byte[] data) throws Exception{
		MifareUltralight tt = MifareUltralight.get(tag);
		try {
			tt.connect();
			// 判断是否为MifareUltralight C数据
			if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
				byte[] newbytes;
				// 写入数据,一次只能写1页4个字节
				int wc = data.length/4;						//一次写入4个字节，判断要写几次
				//如果data的长度不能被4整除，后面要用0x00补位
				int add = data.length%4;
				if(add==0)
					newbytes = data;
				else{
					//如果不能整除，要把写入数据多补1页
					wc++;
					//先初始化补位后的新数组长度
					newbytes = new byte[data.length+add];
					//为新数组赋值
					for(int i=0;i<newbytes.length;i++){
						if(i<data.length)
							newbytes[i] = data[i];
						else
							newbytes[i] = 0x00;
					}
				}
					
				for(int i=0;i<wc;i++){
					byte[] wb = new byte[4];
					System.arraycopy(newbytes, i*4, wb, 0, 4);		//取源数组的4个字节拷贝到要写入的数组中
					try {
						tt.writePage((i+pageIndex), wb);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT) {
			} else {
			}
		} finally {
			try {
				tt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 读取MifareClassic数据
	 * @param tag
	 * @return
	 */
	public static String readMifareClassicData(Tag tag){
		MifareClassic tt = MifareClassic.get(tag);
		int sectorCount = tt.getSectorCount();				//分区数量，1k卡16个分区；2k卡32个分区；4k卡64分区。
		int blockCount = tt.getBlockCount();				//块数量，每个分区4个块,1、2、3块可以记录数据，4块叫Trailer，存放该分区的key。写卡时不能写每区的4块
		int byteCount = tt.getSize();						//字节数，每个块16个字节
		try{
			tt.connect();
			StringBuffer sb = new StringBuffer();
			//分区循环
			for(int i=0;i<sectorCount;i++){
				//块循环,没分区4块
				for(int j=0;j<4;j++){
					//对第i区块进行校验，如果校验成功读取数据
					boolean auth = tt.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
					if(auth){
						//读取第i*4+j区块的数据
						byte[] bytes = tt.readBlock((i*4+j));
						sb.append(NFCDataUtils.bytesToHexString(bytes));
						sb.append(" ");
					}
				}
			}
			return sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				tt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void writeMifareClassesData(Tag tag){
		NdefFormatable ndeff = NdefFormatable.get(tag);
		Locale locale = Locale.US;
		
	}
	/**
	 * 读取ndef数据
	 * @param intent	意图对象，通过意图对象获得数据
	 * @return	
	 */
	public static String readNdefData(Intent intent){
		String mTagText = "";

		// 判断是否为ACTION_NDEF_DISCOVERED
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			// 从标签读取数据（Parcelable对象）
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

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
				if (msgs != null) {
					// 程序中只考虑了1个NdefRecord对象，若是通用软件应该考虑所有的NdefRecord对象
					NdefRecord record = msgs[0].getRecords()[0];
					// 分析第1个NdefRecorder，并创建TextRecord对象
					TextRecord textRecord = TextRecord.parse(record);
					// 获取实际的数据占用的大小，并显示在窗口上
					mTagText += textRecord.getText() + "\n\n纯文本\n"+ contentSize + " bytes";
					System.out.println("==========="+mTagText);
					return textRecord.getText();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}
	/**
	 * 	 * 写入ndef数据，可以不管写入数据的页，由ndef相关类自己控制
	 * @param activity		前台的activity对象
	 * @param tag			标签对象
	 * @param writedata		要写入的数据
	 * @param aarparam		为了保证标签能被转用程序识别，需要代入程序包名
	 */
	public static void writeNdefData(Activity activity,Tag tag,String writedata,String aarparam){
		// 必须要指定一个Tag对象
				if (tag == null) {
					Toast.makeText(activity, "NFC Tag未建立连接", Toast.LENGTH_LONG).show();
					return;
				}
				// 创建NdefMessage对象
				// NdefRecord.creatApplicationRecord方法创建一个封装Package的NdefRecord对象
				NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {createTextRecord(writedata),  NdefRecord.createApplicationRecord(aarparam) });
//				NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {createTextRecord("bk42-lr001")});
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
							Toast.makeText(activity, "NFC Tag是只读的！", Toast.LENGTH_LONG)
									.show();
							return;
						}
						// NFC标签的空间不足
						if (ndef.getMaxSize() < size) {
							Toast.makeText(activity, "NFC Tag的空间不足！", Toast.LENGTH_LONG)
									.show();
							return;
						}
						// 向NFC标签写入数据
						ndef.writeNdefMessage(ndefMessage);
						Toast.makeText(activity, "已成功写入数据！", Toast.LENGTH_LONG).show();
					} else {
						// 创建NdefFormatable对象
						NdefFormatable format = NdefFormatable.get(tag);
						if (format != null) {
							try {
								// 允许标签IO操作，进行连接
								format.connect();
								// 重新格式化NFC标签，并写入数据
								format.format(ndefMessage);
								Toast.makeText(activity, "已成功写入数据！", Toast.LENGTH_LONG)
										.show();
							} catch (Exception e) {
								Toast.makeText(activity, "写入NDEF格式数据失败！", Toast.LENGTH_LONG)
										.show();
							}
						} else {
							Toast.makeText(activity, "NFC标签不支持NDEF格式！", Toast.LENGTH_LONG)
									.show();
						}
					}
				} catch (Exception e) {
					Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
				}
	}
	
	/**
	 * 创建一个封装要写入的文本的NdefRecord对象  
	 * @param text	要写入的文本
	 * @return		返回实现好的NdefRecord对象
	 */
    private static NdefRecord createTextRecord(String text) {  
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
	
	/**
	 * 字符序列转换为16进制字符串形式,便于阅读
	 * @param src	字符串数组
	 * @return 		返回字符串形式
	 */
	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}
	/**
	 * 返回Action的类型
	 * @param str	带包名的ActionType
	 * @return		返回简化后的ActionType名
	 */
	public static String simpleActionType(String str){
		return str.substring(str.lastIndexOf(".")+1, str.length());
	}
	/**
	 * 返回当前标签所支持的技术
	 * @param tag	标签对象
	 * @return		
	 */
	public static String getTechList2String(Tag tag){
		String[] tstrs = tag.getTechList();
		StringBuffer sb = new StringBuffer();
		for(String s:tstrs)
			sb.append(simpleActionType(s)).append(",");
		return sb.toString();
	}
	/**
	 * 判断数据为MifareClassic类型还是MifareUltralight数据类型
	 * @param tag	tag对象，通过其中信息判断时哪种Mifare数据类型
	 * @return		返回类型的字符串信息
	 */
	public static String witchMifareType(Tag tag){
		String[] tstrs = tag.getTechList();
		for(String s:tstrs){
			if(s.equals("android.nfc.tech.MifareClassic") )
				return simpleActionType(s);
			else if(s.equals("android.nfc.tech.MifareUltralight"))
				return simpleActionType(s);
			else if(s.equals("android.nfc.tech.NfcA"))
				System.out.println(simpleActionType(s));
				return simpleActionType(s);
		}
		return null;
	}
	/**
	 * 判断标签为什么类型
	 * @param tag	tag对象，从中判断标签的类型
	 * @return		返回标签的字符串信息
	 */
	public static String witchTagType(Tag tag){
		String[] tstrs = tag.getTechList();
		for(String s:tstrs){
			if(s.equals("android.nfc.tech.Ndef"))
				return simpleActionType(s);
		}
		return null;
	}
	
	/**
	 * 截断字节数组后面的0
	 * @param bytes	要处理的字节数组
	 * @return		返回去掉0的字节数组
	 */
	public static byte[] deleteBytesZero(byte[] bytes){
		int index = 0;
		for(int i=bytes.length-1;i>=0;i--){
			if(bytes[i]!=0)
				break;
			index++;
		}
		byte[] newbytes = new byte[bytes.length-index];
		for(int i=0;i<bytes.length-index;i++)
			newbytes[i] = bytes[i];
		return newbytes;
	}
}
