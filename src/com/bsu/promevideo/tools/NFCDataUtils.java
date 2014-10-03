package com.bsu.promevideo.tools;

import java.io.IOException;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;

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
				// 写入数据,一次只能写1页4个字节
				int wc = data.length/4;						//一次写入4个字节，判断要写几次
				for(int i=0;i<wc;i++){
					byte[] wb = new byte[4];
					System.arraycopy(data, i*4, wb, 0, 4);		//取源数组的4个字节拷贝到要写入的数组中
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
	
	public static void writeMifareClassesData(){
		
	}
	
	public static void readNdefData(Tag tag){
		
	}
	
	public static void writeNdefData(){
		
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
			if(s.equals("android.nfc.tech.MifareClassic"))
				return simpleActionType(s);
			else if(s.equals("android.nfc.tech.MifareUltralight"))
				return simpleActionType(s);
		}
		return null;
	}
}
