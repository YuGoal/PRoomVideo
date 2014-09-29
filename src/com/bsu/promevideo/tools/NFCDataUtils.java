package com.bsu.promevideo.tools;

import android.nfc.Tag;
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
	 * 
	 * @param tag
	 *            获得的标签对象
	 * @return 返回读取的字符串
	 */
	public static String readMifareUltralightData(Tag tag) {
		//tt表示TagTechnology
		MifareUltralight tt = MifareUltralight.get(tag);
		try {
			tt.connect();
			// 判断是否为MifareUltralight C数据
			if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {

				// MIFARE Ultralight C Tag 结构 每页4个字节,前4页是厂商系统等信息,每次读4页
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < 44; i += MifareUltralight.PAGE_SIZE) {
//					String p = (String) ((i<10) ? "0"+i:i);
					sb.append("page ").append(i).append(":")
							.append(bytesToHexString(tt.readPages(i)))
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
	 */
	public static void writeMifareUltralightData(Tag tag,int pageIndex,byte[] data){
		MifareUltralight tt = MifareUltralight.get(tag);
		try {
			tt.connect();
			// 判断是否为MifareUltralight C数据
			if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT_C) {
				// 写入数据,一次只能写1页4个字节				
				tt.writePage(pageIndex, data);
			} else if (tt.getType() == MifareUltralight.TYPE_ULTRALIGHT) {
			} else {
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
	}
	
	/**
	 * 字符序列转换为16进制字符串形式,便于阅读
	 * @param src	字符串数组
	 * @return 	返回字符串形式
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
}
