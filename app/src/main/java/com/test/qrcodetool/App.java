package com.test.qrcodetool;

import android.app.Application;
import android.util.Log;

import com.test.qrcodetool.utils.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by WTZ on 2017/10/11.
 */

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

    public final static boolean DEBUG = false;

    private static final String LOG_DIR = "qrcode" + File.separator + "log";

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            startLogcat();
        }
    }

    private void startLogcat() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<String> commandList = new ArrayList<String>();
                commandList.add("logcat");

                commandList.add("-v");
                commandList.add("time");

                commandList.add("-f");
                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String nowTime = df.format(date);
                File file = new File(getLogDir(), nowTime + ".txt");
                commandList.add(file.getAbsolutePath());

                Log.d(TAG, "start logcat...save:" + file.getAbsolutePath());
                try {
                    Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private File getLogDir() {
        return FileUtil.getStorageDir(getApplicationContext(), LOG_DIR);
    }
}
