package com.test.qrcodetool.utils;

import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrcodeUtil {
    private final static String TAG = QrcodeUtil.class.getSimpleName();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Bitmap CreateQrCode(String content, int width, int height) throws WriterException {
        Log.d(TAG, "CreateQrCode...content = " + content);
        Log.d(TAG, "CreateQrCode...width=" + width + ", height=" + height);
        if (TextUtils.isEmpty(content) || width <=0 || height <= 0) {
            return null;
        }

        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION,  ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        int[] rec = matrix.getEnclosingRectangle();// 获取二维码有效图案的属性
        Log.d(TAG, "CreateQrCode...Rectangle.length=" + rec.length);
        Log.d(TAG, "CreateQrCode...Rectangle.[0]=" + rec[0]);
        Log.d(TAG, "CreateQrCode...Rectangle.[1]=" + rec[1]);
        Log.d(TAG, "CreateQrCode...Rectangle.[2]=" + rec[2]);
        Log.d(TAG, "CreateQrCode...Rectangle.[3]=" + rec[3]);

        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] pixels = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * w + x] = 0xff000000;
                } else {
                    pixels[y * w + x] = 0xffffffff;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Bitmap CreateColorQrCode(String content, int width, int height) throws WriterException {
        Log.d(TAG, "CreateQrCode...content = " + content);
        Log.d(TAG, "CreateQrCode...width=" + width + ", height=" + height);
        if (TextUtils.isEmpty(content) || width <=0 || height <= 0) {
            return null;
        }

        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION,  ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        int[] rec = matrix.getEnclosingRectangle();// 获取二维码有效图案的属性
        Log.d(TAG, "CreateQrCode...Rectangle.length=" + rec.length);
        Log.d(TAG, "CreateQrCode...Rectangle.[0]=" + rec[0]);
        Log.d(TAG, "CreateQrCode...Rectangle.[1]=" + rec[1]);
        Log.d(TAG, "CreateQrCode...Rectangle.[2]=" + rec[2]);
        Log.d(TAG, "CreateQrCode...Rectangle.[3]=" + rec[3]);

        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] pixels = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * w + x] = 0xff000000;
                    if (x < w / 2 && y < h / 2) {
                        pixels[y * w + x] = 0xFFCD00CD;// 粉色
                    } else if (x < w / 2 && y > h / 2) {
                        pixels[y * w + x] = 0xFF0000FF;// 蓝色
                    } else if (x > w / 2 && y > h / 2) {
                        pixels[y * w + x] = 0xFF008B00;// 绿色
                    } else {
                        pixels[y * w + x] = 0xFFCD8500;// 黄色
                    }
                } else {
                    pixels[y * w + x] = 0xffffffff;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    public static Bitmap addLogoForQRCode(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    /**
     * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错，要修改底层jar包的内容
     *
     * @param content
     *            将要生成一维码的内容
     * @return 返回生成好的一维码bitmap
     * @throws WriterException
     *             WriterException异常
     */
    public static Bitmap CreateOneDCode(String content) throws WriterException {
        // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.CODE_128, 600, 300, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Log.d(TAG, "matrix.getWidth=" + width);
        Log.d(TAG, "matrix.getHeight=" + height);

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 扫描二维码、条形码图片的方法
     *
     * @param path
     * @return
     */
    public static Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // 设置码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] pixels = new int[width * height];
        scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        /**
         * 第三个参数是图片的像素
         */
        RGBLuminanceSource source = new RGBLuminanceSource(width, height,
                pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
//        QRCodeReader reader = new QRCodeReader();
//        OneDReader oneReader = new OneDReader();
        MultiFormatReader reader = new MultiFormatReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return null;
    }
}
