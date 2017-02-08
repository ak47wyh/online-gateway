package com.iboxpay.settlement.gateway.alipay.servie.utils;

public class AlipayExtendsCryptUtils {

	public static final String MAP_STRING_1 = "012U345abcXMdmntuvwxyzABCEFefghijklGHI6789DJKLNOPQopqrsRSTVWYZ";

	public static final String MAP_STRING_2 = "4567UVW89abdghXYZ23LMNijklmnopqrstuvwxyzABcCDEFGHIJK01OPQRSTef";

	public static String encode(String str) {
		boolean isOdd = str.length() % 2 != 0;
		String map1 = null;
		String map2 = null;
		if (isOdd) {
			map1 = MAP_STRING_1;
			map2 = MAP_STRING_2;
		} else {
			map1 = MAP_STRING_2;
			map2 = MAP_STRING_1;
		}
		char[] ret = new char[str.length()];
		char c = 0;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			int j = map1.indexOf(c);
			if (j < 0 || (j+i) >= (map2.length())) {
				ret[i] = c;
			} else {
				ret[i] = map2.charAt(j + i);
			}
		}
		return new String(ret);
	}

	public static String decode(String str) {
		boolean isOdd = str.length() % 2 != 0;
		String map1 = null;
		String map2 = null;
		if (isOdd) {
			map1 = MAP_STRING_1;
			map2 = MAP_STRING_2;
		} else {
			map1 = MAP_STRING_2;
			map2 = MAP_STRING_1;
		}
		char[] ret = new char[str.length()];
		char c = 0;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			int j = map2.indexOf(c);
			j = j - i;
			if (j < 0 || (j+i) >= (map2.length())) {
				ret[i] = c;
			} else {
				ret[i] = map1.charAt(j);
			}
		}
		return new String(ret);
	}
}
