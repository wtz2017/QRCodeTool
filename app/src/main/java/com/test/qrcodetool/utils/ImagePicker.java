package com.test.qrcodetool.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
    public final static int ACTIVITY_REQUESTCODE_CROP = 2;

    public static void pickByCamera(Fragment launcher, File target) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // 通过FileProvider创建一个content类型的Uri
                uri = FileProvider.getUriForFile(launcher.getContext().getApplicationContext(), "com.test.qrcodetool.fileprovider", target);
            } else {
                uri = Uri.fromFile(target);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
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

    public static void cutImageByCamera(Fragment launcher, File source, File crop) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri uriSource;
        Uri uriCrop;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//TODO
            // 通过FileProvider创建一个content类型的Uri
            uriSource = FileProvider.getUriForFile(launcher.getContext().getApplicationContext(), "com.test.qrcodetool.fileprovider", source);
            uriCrop = FileProvider.getUriForFile(launcher.getContext().getApplicationContext(), "com.test.qrcodetool.fileprovider", crop);
        } else {
            uriSource = Uri.fromFile(source);
            uriCrop = Uri.fromFile(crop);
        }
        intent.setDataAndType(uriSource, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriCrop);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        launcher.startActivityForResult(intent,
                ACTIVITY_REQUESTCODE_CROP);
    }

    /**
     * 读取uri所在的图片
     *
     * @param uri      图片对应的Uri
     * @param mContext 上下文对象
     * @return 获取图像的Bitmap
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context mContext) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
