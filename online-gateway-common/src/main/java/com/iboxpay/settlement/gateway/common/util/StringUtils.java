package com.iboxpay.settlement.gateway.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class StringUtils {

    /**
    * <p>Checks if a String is empty ("") or null.</p>
    *
    * <pre>
    * StringUtils.isEmpty(null)      = true
    * StringUtils.isEmpty("")        = true
    * StringUtils.isEmpty(" ")       = false
    * StringUtils.isEmpty("bob")     = false
    * StringUtils.isEmpty("  bob  ") = false
    * </pre>
    *
    * <p>NOTE: This method changed in Lang version 2.0.
    * It no longer trims the String.
    * That functionality is available in isBlank().</p>
    *
    * @param str  the String to check, may be null
    * @return <code>true</code> if the String is empty or null
    */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is
     *  not empty and not null and not whitespace
     * @since 2.0
     */
    public static boolean isNotBlank(String str) {
        return !StringUtils.isBlank(str);
    }

    public static String trim(String s) {
        if (s != null) s = s.trim();

        return s;
    }

    /**
     * 为null的返回空串
     * @param s
     * @return
     */
    public static String trimEmpty(String s) {
        if (s != null)
            return s.trim();
        else return "";
    }

    /**
     * 联行号中提取地区码
     * @param cnaps
     * @return
     */
    public static String getAreaCodeFromCnaps(String cnaps) {
        if (cnaps.length() != 12) throw new IllegalArgumentException("不是合法联行号:" + cnaps);
        return cnaps.substring(3, 7);
    }

    /**
     * 联行号中提取银行编码
     * @param cnaps
     * @return
     */
    public static String getBankCodeFromCnaps(String cnaps) {
        if (cnaps == null) return null;
        cnaps = cnaps.trim();
        if (cnaps.length() != 12) throw new IllegalArgumentException("不是合法联行号:" + cnaps);
        return cnaps.substring(0, 3);
    }

    public static String[] split(String line, String seperator) {
        if (isBlank(line)) return null;
        List<String> slices = new LinkedList<String>();
        int i = 0, j;
        while ((j = line.indexOf(seperator, i)) >= 0) {
            slices.add(line.substring(i, j).trim());
            i = j + seperator.length();
        }
        slices.add(line.substring(i).trim());
        return slices.toArray(new String[0]);
    }

	/**
	*	数字不足位数左补0
	* @param str
	* @param strLength
	* @param left
	*/
	public static String addZeroToString(String str, int strLength, boolean left) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				if(left) {
					sb.append("0").append(str);//左补0
				}
				else { 
					sb.append(str).append("0");//右补0
				}
				str = sb.toString();
				strLen = str.length();
			}
		}
		
		return str;
	}
}
