package com.iboxpay.settlement.gateway.kq.service.util;
import org.apache.commons.codec.binary.Base64;

public final class Base64Util
{
  public static byte[] encode(byte[] binaryData)
  {
    return Base64.encodeBase64(binaryData);
  }

  public static byte[] encode(byte[] binaryData, boolean isChunked)
  {
    return Base64.encodeBase64(binaryData, isChunked);
  }

  public static byte[] decode(byte[] base64Data)
  {
    return Base64.decodeBase64(base64Data);
  }

  public static boolean isArrayByteBase64(byte[] data)
  {
    if (data == null) {
      throw new IllegalArgumentException();
    }
    return Base64.isArrayByteBase64(data);
  }

  public static boolean isStringBase64(String base64String)
  {
    if (base64String == null) {
      throw new IllegalArgumentException();
    }
    return Base64.isArrayByteBase64(base64String.getBytes());
  }
}