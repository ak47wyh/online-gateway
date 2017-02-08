package com.iboxpay.settlement.gateway.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    //  public static byte[] zip(byte[] in)
    //    throws IOException
    //  {
    //    ByteArrayInputStream ins = new ByteArrayInputStream(in);
    //    ByteArrayOutputStream outs = new ByteArrayOutputStream();
    //    zip(ins, outs, "noname");
    //    return outs.toByteArray();
    //  }

    public static void zip(InputStream in, OutputStream out, String name) throws IOException {
    }
    
    /**
	 * 功能描述：压缩文件对象
	 * @param zipFileName 压缩文件名(带有路径)
	 * @param inputFile   文件对象
	 * @return
	 * @throws Exception
	 */
	public static void zip(String zipFileName, File inputFile) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName), Charset.forName("GBK"));
		if(inputFile.isDirectory()){
			zip(out, inputFile, "");
		}else{
			zip(out, inputFile, inputFile.getName());
		}
		out.close();
	}
	/**
	/**
	 * 
	 * @param out  压缩输出流对象
	 * @param file 
	 * @param base
	 * @throws Exception
	 */
	public static void zip(ZipOutputStream outputStream, File file, String base) throws Exception {
		if (file.isDirectory()) {
			File[] fl = file.listFiles();
			outputStream.putNextEntry(new ZipEntry(base + "/"));
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < fl.length; i++) {
				zip(outputStream, fl[i], base + fl[i].getName());
			}
		}else {
			outputStream.putNextEntry(new ZipEntry(base));
			FileInputStream inputStream = new FileInputStream(file);
			int j;
			byte[] buffer = new byte[1024];
			while ((j = inputStream.read(buffer)) != -1){
				outputStream.write(buffer, 0, j);
				outputStream.flush();
			}
			inputStream.close();
		}
	}

    public static byte[] unzip(byte[] in) throws IOException {
        ByteArrayInputStream ins = new ByteArrayInputStream(in);
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        unzip(ins, outs);
        return outs.toByteArray();
    }

    public static String unzip(InputStream in, OutputStream out) throws IOException {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in));
        String name = null;
        try {
            ZipEntry ze = zin.getNextEntry();
            if (ze == null) {
                return null;
            }
            out = new BufferedOutputStream(out);

            byte[] buf = new byte[256];
            int l;
            while ((l = zin.read(buf)) != -1) {
                out.write(buf, 0, l);
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (zin != null) {
                zin.close();
            }
        }
        if (out != null) {
            out.close();
        }
        if (zin != null) {
            zin.close();
        }
        return name;
    }

    public static void main(String[] args) throws Exception {
//        FileInputStream fi = new FileInputStream("d:/tmp/1.txt");
//        FileOutputStream fo = new FileOutputStream("d:/tmp/1.zip");
//        zip(fi, fo, "64");
//        fi = new FileInputStream("d:/tmp/1.zip");
//        FileWriter fw = new FileWriter("d:/tmp/64.zip");
//        
    	File fi = new File("F:/20150610366盒子代付.xls");
    	
    	zip("F:/upload - 副本.zip", fi);
    }
}
