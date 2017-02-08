package com.iboxpay.settlement.gateway.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IOUtils {

    public static byte[] readFully(InputStream is) throws IOException {
        try {
            byte[] result = new byte[0];
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                byte[] _result = new byte[result.length + len];
                System.arraycopy(buf, 0, _result, result.length, len);
                result = _result;
            }
            return result;
        } finally {
            try {
                is.close();
            } catch (Exception e) {}
        }
    }

    /**
     * 测试ip/端口是否可用
     * @param ip
     * @param port
     * @return
     */
    public static boolean testConnection(String ip, int port) {
        Socket client = null;
        try {
            client = new Socket(ip, port);
            return true;
        } catch (Exception e) {
            //			logger.error("测试连接失败("+ip +":"+ port+ ")");
        } finally {
            try {
                if (client != null) client.close();
            } catch (IOException e) {}
        }
        return false;
    }

    /**
     * 关闭流，异常不会抛出任何异常
     * @param in
     */
    public static void closeQuietly(InputStream in) {
        try {
            if (in != null) in.close();
        } catch (Exception e) {}
    }

    /**
     * 关闭流，异常不会抛出任何异常
     * @param out
     */
    public static void closeQuietly(OutputStream out) {
        try {
            if (out != null) out.close();
        } catch (Exception e) {}
    }
}
