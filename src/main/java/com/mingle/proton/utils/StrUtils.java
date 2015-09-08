package com.mingle.proton.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class StrUtils {

	public static final Logger log = Logger.getLogger(StrUtils.class);
	/** "," */
	private static final String DIVIDER = ",";

	public static void main(String[] args) {
		System.out.println(isCharacter("111888"));
	}

	public static boolean isCharacter(String str) {
		if (null == str) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (false == Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * JDK默认的split方法碰到长度为0的会不返回，有问题
	 * 
	 * @param str
	 * @param token
	 * @return List
	 */
	public static List<String> str2ListWithNull(String str, String token) {
		List<String> list = new LinkedList<String>();
		if (StringUtils.isEmpty(str)) {
			return list;
		}
		int posBegin = 0, posEnd = 0;
		for (; -1 != (posEnd = str.indexOf(token, posBegin)); posBegin = posEnd + token.length()) {
			list.add(str.substring(posBegin, posEnd));
		}
		list.add(str.substring(posBegin));
		return list;
	}

	public static String[] str2ArrayWithNull(String str, String token) {
		List<String> list = str2ListWithNull(str, token);
		if (null == list) {
			return null;
		}
		String[] arrays = new String[list.size()];
		int z = 0;
		for (String temp : list) {
			arrays[z++] = temp;
		}
		return arrays;
	}

	/**
	 * 转换出错信息，把异常的堆栈转换成String类型
	 * 
	 * @param ex
	 *            异常对象
	 * @return String
	 */
	public static String ex2Str(Throwable ex) {
		CharArrayWriter caw = new CharArrayWriter();
		ex.printStackTrace(new PrintWriter(caw, true));
		return caw.toString();
	}

	/**
	 * 根据指定数字，返回此数字的文本，根据需要的长度，在返回的文本前加零 example:addZeroBefore(34, 4);返回"0034"
	 * 
	 * @param num
	 * @param needLength
	 * @return
	 */
	public static String addZeroBefore(int num, int needLength) {
		return addZeroBefore("" + num, needLength);
	}

	public static String addZeroBefore(String str, int needLength) {
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() < needLength) {
			buf.insert(0, "0");
		}
		return buf.toString();
	}

	/**
	 * 按照分隔符“,”把字符串转换到列表
	 * 
	 * @param str
	 *            要转换的字符串
	 * @return List
	 */
	public static List<String> str2List(String str) {
		return str2List(str, DIVIDER);
	}

	/**
	 * 按照制定分隔符把字符串转换到列表
	 * 
	 * @param str
	 *            要转换的字符串
	 * @param token
	 *            分隔符
	 * @return List
	 */
	public static List<String> str2List(String str, String token) {
		return Arrays.asList(str.split(token));
	}

	/**
	 * 按照分隔符“,”把列表转换到字符串
	 * 
	 * @param collect
	 *            要转换的列表
	 * @return String
	 */
	public static String list2Str(Collection<String> collect) {
		return list2Str(collect, DIVIDER);
	}

	/**
	 * 按照制定分隔符把列表转换到字符串
	 * 
	 * @param collect
	 *            要转换的列表
	 * @param token
	 *            分隔符
	 * @return String
	 */
	public static String list2Str(Collection<String> collect, String token) {
		if (StringUtils.isEmpty(token)) {
			throw new IllegalArgumentException("invalid token:" + token);
		}
		StringBuffer buf = new StringBuffer(collect.size() * 5);
		for (String temp : collect) {
			buf.append(token + temp);
		}
		return buf.delete(0, token.length()).toString();
	}

	private final static Pattern CALLBACK_REGEX = Pattern.compile("^[\\w$]+$");

	/**
	 * callback方法名的XSS过滤
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isCallbackName(String name) {
		if (name == null)
			return false;
		return CALLBACK_REGEX.matcher(name).find();
	}
}
