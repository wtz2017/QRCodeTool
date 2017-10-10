package com.test.qrcodetool.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.kevin.crop.UCrop;
import com.test.qrcodetool.CropActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by WTZ on 2017/10/10.
 */

public class ImagePicker {
    private final static String TAG = ImagePicker.class.getSimpleName();

    public final static int ACTIVITY_REQUESTCODE_CAMERA = 0;
    public final static int ACTIVITY_REQUESTCODE_GALLERY = 1;

    public static void pickByCamera(Fragment launcher, File target) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(target));
            launcher.startActivityForResult(intent,
                    ACTIVITY_REQUESTCODE_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pickFromGallery(Fragment launcher, File target) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            launcher.startActivityForResult(intent,
                    ACTIVITY_REQUESTCODE_GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pickByPhotoCut(Fragment launcher, File target) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            //设定宽高比
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            //设定剪裁图片宽高
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);

            intent.putExtra("noFaceDetection", true);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(target));
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            launcher.startActivityForResult(intent,
                    ACTIVITY_REQUESTCODE_GALLERY);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * android 4.4以下适用
     */
    public static void cutImageByCamera(Fragment launcher, File target) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(target), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(target));
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        launcher.startActivityForResult(intent,
                ACTIVITY_REQUESTCODE_GALLERY);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public static void startCropActivity(Uri uri, Uri destinationUri, Fragment fragment) {
        UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300)
                .withTargetActivity(CropActivity.class)
                .start(fragment.getActivity(), fragment);
    }

    /**
     * 处理剪切成功的返回值
     *
     * @param result
     */
    public static void handleCropResult(Context context, Intent result, OnPictureSelectedListener listener) {
        final Uri resultUri = UCrop.getOutput(result);
        if (null != resultUri && null != listener) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listener.onPictureSelected(resultUri, bitmap);
        } else {
            listener.onPictureSelected(null, null);
            Toast.makeText(context, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理剪切失败的返回值
     *
     * @param result
     */
    public static void handleCropError(Context context, Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(context, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnPictureSelectedListener {
        /**
         * 图片选择的监听回调
         *
         * @param fileUri
         * @param bitmap
         */
        void onPictureSelected(Uri fileUri, Bitmap bitmap);
    }
}
