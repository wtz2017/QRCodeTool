package com.test.qrcodetool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtr.zxing.activity.CaptureActivity;
import com.dtr.zxing.decode.DecodeThread;

import static android.app.Activity.RESULT_OK;

/**
 * Created by WTZ on 2017/10/8.
 */

public class FragmentReadCode extends Fragment {
    private static final String TAG = FragmentCreateCode.class.getSimpleName();

    private Button mScan;
    private Button mQueryBarcode;
    private TextView mResult;
    private ImageView mOriginerImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_read_code, container, false);
        mScan = (Button) rootView.findViewById(R.id.btn_scan);
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });
        mQueryBarcode = (Button) rootView.findViewById(R.id.btn_query_barcode);
        mQueryBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryBarcode();
            }
        });
        mResult = (TextView) rootView.findViewById(R.id.tv_result);
        mOriginerImage = (ImageView) rootView.findViewById(R.id.iv_originer);
        return rootView;
    }

    private void queryBarcode() {
//        startActivity(new Intent(FragmentReadCode.this.getContext(), QueryBarcodeActivity.class));

        // webview能打开第一页，但提交数据查询时提示“非法ip”，没有正常显示人机验证页面
        // 报错：Uncaught TypeError: Cannot read property 'Constructor' of undefined
        // , source: http://www.ancc.org.cn/bootstrap/js/bootstrap-accessibility.min.js (4)
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse("http://www.ancc.org.cn/Service/queryTools/Internal.aspx");
        intent.setData(content_url);
        startActivity(intent);
    }

    private void scan() {
        startActivityForResult(new Intent(FragmentReadCode.this.getContext(), CaptureActivity.class), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult...requestCode=" + requestCode
        + ", resultCode=" + resultCode + ", data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            String scanResult = (extras != null) ? extras.getString("result") : "";
            mResult.setText(scanResult);

            Bitmap barcode = null;
            byte[] compressedBitmap = extras.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                // Mutable copy:
                barcode = barcode.copy(Bitmap.Config.RGB_565, true);
            }
            mOriginerImage.setImageBitmap(barcode);
        } else {
            mResult.setText("Scan failed!");
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

}
