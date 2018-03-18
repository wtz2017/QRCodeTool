/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtr.zxing.decode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.dtr.zxing.activity.CaptureActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.test.qrcodetool.R;

public class DecodeHandler extends Handler {

	private final CaptureActivity activity;
	private final MultiFormatReader multiFormatReader;
	private boolean running = true;
	private final boolean isDebug = false;

	public DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		if (!running) {
			return;
		}
		switch (message.what) {
		case R.id.decode:
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			running = false;
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void decode(byte[] data, int width, int height) {
        Size size = activity.getCameraManager().getPreviewSize();
        Log.d("DecodeHandler", "decode getPreviewSize: width=" + size.width + ", height=" + size.height);

        if (isDebug) {
            testYuvToBitmap(data, size.width, size.height, "yuv_rgb_origin.jpg");
        }

        if (activity.isPhone()) {
            // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < size.height; y++) {
                for (int x = 0; x < size.width; x++)
                    rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
            }

            // 宽高也要调整
            int tmp = size.width;
            size.width = size.height;
            size.height = tmp;

            data = rotatedData;
        }

        if (isDebug) {
            testYuvCropToBitmap(data, size.width, size.height, "yuv_rgb_crop.jpg");
        }

		Result rawResult = null;
		PlanarYUVLuminanceSource source = buildLuminanceSource(data, size.width, size.height);
        if (source != null) {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);
			} catch (ReaderException re) {
				re.printStackTrace();
                // continue
            } finally {
				multiFormatReader.reset();
			}
		}

		Handler handler = activity.getHandler();
        if (rawResult != null) {
			// Don't log the barcode contents for security.
			if (handler != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
				Bundle bundle = new Bundle();
				bundleThumbnail(source, bundle);
				message.setData(bundle);
				message.sendToTarget();
			}
		} else {
			if (handler != null) {
				Message message = Message.obtain(handler, R.id.decode_failed);
				message.sendToTarget();
			}
		}

	}

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
		int[] pixels = source.renderThumbnail();
		int width = source.getThumbnailWidth();
		int height = source.getThumbnailHeight();
		Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
		bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
	}

	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
		Rect rect = activity.getCropRect();
		if (rect == null) {
			return null;
		}
		Log.d("DecodeHandler", "getCropRect: left=" + rect.left + ", top=" + rect.top
        + ", cropWidth=" + rect.width() + ", cropHeight=" + rect.height()
        + ", totalImageWidth=" + width + ", totalImageHeight=" + height);
		// Go ahead and assume it's YUV rather than die.
		return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
	}

    public static void decodeYUV420SPrgb565(int[] rgb, byte[] yuv420sp, int width,
                                            int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    private void testYuvToBitmap(byte[] data, int width, int height, String fileName) {
        int[] rgb = new int[width * height];
        decodeYUV420SPrgb565(rgb, data, width, height);
        Bitmap temp = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.RGB_565) ;
        File file2 = new File("/sdcard/" + fileName);
        try {
            FileOutputStream out = new FileOutputStream(file2);
            if (temp.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (Exception e) {
        }
    }

    private void testYuvCropToBitmap(byte[] data, int width, int height, String fileName) {
        int[] rgb = new int[width * height];
        decodeYUV420SPrgb565(rgb, data, width, height);
        Rect rect = activity.getCropRect();
        int[] crop = getCropData(rgb, width, rect.left, rect.top, rect.width(), rect.height());
        Bitmap temp = Bitmap.createBitmap(crop, rect.width(), rect.height(), Bitmap.Config.RGB_565) ;
        File file2 = new File("/sdcard/" + fileName);
        try {
            FileOutputStream out = new FileOutputStream(file2);
            if (temp.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (Exception e) {
        }
    }

    private int[] getCropData(int[] rgbData, int imgWith, int cropLeft, int cropTop, int cropWidth, int cropHeight) {
        int[] cropData = new int[cropWidth * cropHeight];
        for (int y = 0, rowStart = cropTop * imgWith + cropLeft; y < cropHeight; y++, rowStart += imgWith) {
            for (int x1 = rowStart, x2 = 0; x1 < rowStart + cropWidth; x1++, x2++) {
                cropData[y * cropWidth + x2] = rgbData[x1];
            }
        }
        return cropData;
    }
}
