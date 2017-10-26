package com.test.qrcodetool;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class QueryBarcodeActivity extends Activity {
    private static final String TAG = QueryBarcodeActivity.class.getSimpleName();

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_barcode);

        mWebView = (WebView) this.findViewById(R.id.wv_query_barcode);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, "onReceivedError...errorCode=" + errorCode + ", description=" + description
                        + ", failingUrl=" + failingUrl);
                Toast.makeText(getApplicationContext(), "连接异常:" + description,
                        Toast.LENGTH_SHORT).show();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                return true;
            }

            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                // 重写此方法可以让webview处理https请求
                handler.proceed();
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);//支持缩放
        webSettings.setDisplayZoomControls(true);// 显示缩放按钮
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);//提高渲染等级
        webSettings.setAppCacheEnabled(true);//缓存
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webSettings.setSavePassword(false);//是否自动保存密码
        webSettings.setSaveFormData(false);//表格数据
        webSettings.setAllowFileAccess(false); // 是否允许访问文件
        webSettings.supportMultipleWindows();//多窗口
        webSettings.setLoadsImagesAutomatically(true);//自动加载图片
        /**
         * 用WebView显示图片，可使用这个参数 设置网页布局类型： 1、LayoutAlgorithm.NARROW_COLUMNS ：
         * 适应内容大小 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
         */
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWebView.requestFocusFromTouch();//支持手势焦点，如果不设置，可能会导致网页上面输入法弹不出来
        mWebView.requestFocus();
        mWebView.loadUrl("http://www.ancc.org.cn/Service/queryTools/Internal.aspx");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown...keyCode=" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
