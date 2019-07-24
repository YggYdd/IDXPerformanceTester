package com.inspur.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 格式化时间
 */
public class TimeUtils {
	// 长度为17的时间日期格式
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * 取得当前时间
	 * 
	 * @return
	 */
	public static String getCurrentDateTime() {
		return sdf.format(Calendar.getInstance().getTime());
	}

	/**
	 * 取得当前日期
	 * 
	 * @return
	 */
	public static String getCurrentDate() {
		return sdf.format(Calendar.getInstance().getTime());
	}

	/**
	 * 格式化时间
	 * 
	 * @param time
	 * @return String
	 */
	public static String formatTime(long time) {
		return sdf.format(new Date(time));
	}

	/**
	 * 转换成毫秒时间
	 * 
	 * @param time
	 * @return long
	 */
	public static long parseLong(String time) {
		long returnTime = 0L;
		if (time != null && !"".equals(time)) {
			try {
				Date d = sdf.parse(time);
				returnTime = d.getTime();
			} catch (ParseException e) {
			}
		}

		return returnTime;
	}

	
	public static long calcTimeDiff(String sStartTime, String sEndTime) {
		return parseLong(sEndTime) - parseLong(sStartTime);
	}

}
