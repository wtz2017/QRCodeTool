package com.test.qrcodetool;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RadioGroup rgTabButtons;
    private ViewPager mViewPager;

    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> mPermissionList = new ArrayList<>();
    private final static int REQUEST_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new FragmentCreateCode());
        fragments.add(new FragmentReadCode());
        FragmentPagerAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager) findViewById(R.id.vp_container);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);

        rgTabButtons = (RadioGroup) findViewById(R.id.rgTabBtns);
        rgTabButtons.setOnCheckedChangeListener(onCheckedChangeListener);
        ((RadioButton) rgTabButtons.getChildAt(0)).setChecked(true);

        if (Build.VERSION.SDK_INT >= 23) {
            judgePermission();
        }
    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int checkedItem = 0;
            switch (checkedId) {
                case R.id.rb1:
                    checkedItem = 0;
                    break;
                case R.id.rb2:
                    checkedItem = 1;
                    break;
            }
            Log.d(TAG, "onCheckedChanged...checkedItem=" + checkedItem);
            if (mViewPager != null && mViewPager.getCurrentItem() != checkedItem) {
                mViewPager.setCurrentItem(checkedItem);
            }
        }
    };

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected...position=" + position);
            ((RadioButton) rgTabButtons.getChildAt(position)).setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void judgePermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            // TODO: 2017/10/9
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            this.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = this.shouldShowRequestPermissionRationale(permissions[i]);
                        if (showRequestPermission) {//
//                            judgePermission();//重新申请权限
//                            return;
                        } else {
                            //已经禁止
                        }
                    }
                }
                // TODO: 2017/9/29 Do something
                break;
            default:
                break;
        }
    }
}
