/**
 * 
 */
package com.iboxpay.settlement.gateway.jd.service.utils;

/**
 * @author liufengyi
 *
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.zxing.common.BitMatrix;

public final class MatrixToImageWriter {

	private static Logger logger = LoggerFactory.getLogger(MatrixToImageWriter.class);

	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;

	private static final int IMAGE_WIDTH = 80;
	private static final int IMAGE_HEIGHT = 80;
	private static final int IMAGE_HALF_WIDTH = IMAGE_WIDTH / 2;
	private static final int FRAME_WIDTH = 2;

	private MatrixToImageWriter() {
	}

	public static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}

	public static void writeToFile(BitMatrix matrix, String format, File file)
			throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, file)) {
			throw new IOException("Could not write an image of format "
					+ format + " to " + file);
		}
	}

	public static byte[] toByteArray(BitMatrix matrix, String format) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BufferedImage image = toBufferedImage(matrix);
		try {
			if (!ImageIO.write(image, format, stream)) {
				logger.error("Could not write an image of format " + format);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("创建二维码logo图片错误" + e.toString(), e);
		}
		return stream.toByteArray();
	}

	public static byte[] toByteArray(String logoFile, BitMatrix matrix,
			String format) {
		BufferedImage image = toBufferedImage(matrix);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		/**
		 * 读取Logo图片
		 */
		BufferedImage logo = null;
		try {
			logo = ImageIO.read(new File("/xx/xx/logo.png"));
			int widthLogo = logo.getWidth();
			int heightLogo = logo.getHeight();

			// 计算图片放置位置
			int x = (matrix.getWidth() - widthLogo) / 2;
			int y = (matrix.getHeight() - logo.getHeight()) / 2;
			Graphics2D g = image.createGraphics();
			// 开始绘制图片
			g.drawImage(logo, x, y, widthLogo, heightLogo, null);
			g.dispose();

			if (!ImageIO.write(image, format, stream)) {
				logger.error("Could not write an image of format " + format);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("创建二维码logo图片错误" + e.toString(), e);
		}
		return stream.toByteArray();
	}

	public static void writeToStream(BitMatrix matrix, String format,
			OutputStream stream) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, stream)) {
			throw new IOException("Could not write an image of format "
					+ format);
		}
	}

	public static void writeToStreamWithLogo(String logoFile, BitMatrix matrix,
			String format, OutputStream stream) {
		BufferedImage image = toBufferedImage(matrix);
		/**
		 * 读取Logo图片
		 */
		BufferedImage logo = null;
		try {
			logo = ImageIO.read(new File("/xx/xx/logo.png"));
			int widthLogo = logo.getWidth();
			int heightLogo = logo.getHeight();

			// 计算图片放置位置
			int x = (matrix.getWidth() - widthLogo) / 2;
			int y = (matrix.getHeight() - logo.getHeight()) / 2;
			Graphics2D g = image.createGraphics();
			// 开始绘制图片
			g.drawImage(logo, x, y, widthLogo, heightLogo, null);
			g.dispose();

			if (!ImageIO.write(image, format, stream)) {
				throw new IOException("Could not write an image of format "
						+ format);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("创建二维码logo图片错误" + e.toString(), e);
		}
	}

	/**
	 * 去除二维码的白边
	 * 
	 * @param matrix
	 * @return
	 */
	public static BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1]))
					resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	public static void writeToStreamWithLogo1(String logoFile,
			BitMatrix matrix, String format, OutputStream stream) {

		// 读取源图像
		BufferedImage scaleImage = null;
		try {
			scaleImage = scale(logoFile, IMAGE_WIDTH, IMAGE_HEIGHT, true);
			int[][] srcPixels = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
			for (int i = 0; i < scaleImage.getWidth(); i++) {
				for (int j = 0; j < scaleImage.getHeight(); j++) {
					srcPixels[i][j] = scaleImage.getRGB(i, j);
				}
			}

			// 二维矩阵转为一维像素数组
			int halfW = matrix.getWidth() / 2;
			int halfH = matrix.getHeight() / 2;
			int width = matrix.getWidth();
			int height = matrix.getHeight();
			int[] pixels = new int[width * height];

			for (int y = 0; y < matrix.getHeight(); y++) {
				for (int x = 0; x < matrix.getWidth(); x++) {
					// 读取图片
					if (x > halfW - IMAGE_HALF_WIDTH
							&& x < halfW + IMAGE_HALF_WIDTH
							&& y > halfH - IMAGE_HALF_WIDTH
							&& y < halfH + IMAGE_HALF_WIDTH) {
						pixels[y * width + x] = srcPixels[x - halfW
								+ IMAGE_HALF_WIDTH][y - halfH
								+ IMAGE_HALF_WIDTH];
					}
					// 在图片四周形成边框
					else if ((x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
							&& x < halfW - IMAGE_HALF_WIDTH + FRAME_WIDTH
							&& y > halfH - IMAGE_HALF_WIDTH - FRAME_WIDTH && y < halfH
							+ IMAGE_HALF_WIDTH + FRAME_WIDTH)
							|| (x > halfW + IMAGE_HALF_WIDTH - FRAME_WIDTH
									&& x < halfW + IMAGE_HALF_WIDTH
											+ FRAME_WIDTH
									&& y > halfH - IMAGE_HALF_WIDTH
											- FRAME_WIDTH && y < halfH
									+ IMAGE_HALF_WIDTH + FRAME_WIDTH)
							|| (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
									&& x < halfW + IMAGE_HALF_WIDTH
											+ FRAME_WIDTH
									&& y > halfH - IMAGE_HALF_WIDTH
											- FRAME_WIDTH && y < halfH
									- IMAGE_HALF_WIDTH + FRAME_WIDTH)
							|| (x > halfW - IMAGE_HALF_WIDTH - FRAME_WIDTH
									&& x < halfW + IMAGE_HALF_WIDTH
											+ FRAME_WIDTH
									&& y > halfH + IMAGE_HALF_WIDTH
											- FRAME_WIDTH && y < halfH
									+ IMAGE_HALF_WIDTH + FRAME_WIDTH)) {
						pixels[y * width + x] = 0xfffffff;
					} else {
						// 此处可以修改二维码的颜色，可以分别制定二维码和背景的颜色；
						pixels[y * width + x] = matrix.get(x, y) ? 0xff000000
								: 0xfffffff;
					}
				}
			}

			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			image.getRaster().setDataElements(0, 0, width, height, pixels);
			if (!ImageIO.write(image, format, stream)) {
				throw new IOException("Could not write an image of format "
						+ format);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("创建二维码logo图片错误" + e.toString(), e);
		}
	}

	private static BufferedImage scale(String srcImageFile, int height,
			int width, boolean hasFiller) throws IOException {
		double ratio = 0.0; // 缩放比例
		File file = new File(srcImageFile);
		BufferedImage srcImage = ImageIO.read(file);
		Image destImage = srcImage.getScaledInstance(width, height,
				BufferedImage.SCALE_SMOOTH);
		// 计算比例
		if ((srcImage.getHeight() > height) || (srcImage.getWidth() > width)) {
			if (srcImage.getHeight() > srcImage.getWidth()) {
				ratio = (new Integer(height)).doubleValue()
						/ srcImage.getHeight();
			} else {
				ratio = (new Integer(width)).doubleValue()
						/ srcImage.getWidth();
			}
			AffineTransformOp op = new AffineTransformOp(
					AffineTransform.getScaleInstance(ratio, ratio), null);
			destImage = op.filter(srcImage, null);
		}
		if (hasFiller) {// 补白
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphic = image.createGraphics();
			graphic.setColor(Color.white);
			graphic.fillRect(0, 0, width, height);
			if (width == destImage.getWidth(null))
				graphic.drawImage(destImage, 0,
						(height - destImage.getHeight(null)) / 2,
						destImage.getWidth(null), destImage.getHeight(null),
						Color.white, null);
			else
				graphic.drawImage(destImage,
						(width - destImage.getWidth(null)) / 2, 0,
						destImage.getWidth(null), destImage.getHeight(null),
						Color.white, null);
			graphic.dispose();
			destImage = image;
		}
		return (BufferedImage) destImage;
	}
}
