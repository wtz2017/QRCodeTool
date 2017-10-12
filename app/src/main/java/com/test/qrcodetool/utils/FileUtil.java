package com.test.qrcodetool.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by WTZ on 2017/10/9.
 */

public class FileUtil {

    public static File bitmapToFile(Bitmap bmp, String path, boolean recycle) {
        File file = new File(path);
        FileOutputStream fops = null;
        try {
            fops = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fops);
            fops.flush();
            if (recycle) {
                bmp.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fops != null) {
                try {
                    fops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static File getStorageDir(Context context, String subDirName) {
        if (context == null) {
            return null;
        }

        final String rootPath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                || !isExternalStorageRemovable() ? Environment.getExternalStorageDirectory()
                .getPath() : context.getFilesDir().getAbsolutePath();

        File folder = new File(rootPath + File.separator + subDirName);
        checkAndMkDirs(folder);

        return folder;
    }

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static boolean checkAndMkDirs(File folder) {
        if (folder == null) {
            return false;
        }

        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }
}
