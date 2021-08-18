package com.bdxh.classbrand;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bdxh.classbrand.activity.BaseActivity;
import com.bdxh.classbrand.app.App;
import com.bdxh.classbrand.bean.ResultAdb;
import com.bdxh.classbrand.common.Config;
import com.bdxh.classbrand.common.UniqueIDUtils;
import com.bdxh.classbrand.dialog.CommonDialog;
import com.bdxh.classbrand.provider.ImageProvider;
import com.bdxh.classbrand.utils.AppRunningUtil;
import com.bdxh.classbrand.utils.LoadUrlUtils;
import com.bdxh.classbrand.utils.Utils;
import com.bdxh.classbrand.web.PreloadWebView;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends BaseActivity implements IGetMessageCallBack {

    private static final int REQ_CODE = 88;
    private MMKV mmkv;
    private WebView mWebView;
    private Handler mHandler = new Handler();
    private int delay = 100;
    private ImageProvider.ImageListener listener;
    private ProgressDialog progressDialog;
    private String mFileName = Environment.getExternalStorageDirectory() + "/" + "class_brand.apk";
    private CommonDialog downApkDialog;

    @Override
    public int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initBind() {
        ActivityCompat.requestPermissions(this, Config.NEEDED_PERMISSIONS, REQ_CODE);
        MMKV.initialize(this);
        mmkv = MMKV.defaultMMKV();

        progressDialog = new ProgressDialog(this);
        WindowManager.LayoutParams params = Objects.requireNonNull(progressDialog.getWindow()).getAttributes();
        params.gravity = Gravity.CENTER;
        progressDialog.getWindow().setAttributes(params);
        initView();
        setPowerOnOff();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        FrameLayout ll_content = findViewById(R.id.ll_content);
        mWebView = PreloadWebView.getInstance().getWebView(getContext());
        ll_content.addView(mWebView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mWebView.setVisibility(View.VISIBLE);
                LogUtils.d("onPageStarted:  url=" + url);
                progressDialog.setMessage("加载数据中...");
                progressDialog.show();

                if (!NetworkUtils.isConnected()) {
                    mWebView.setVisibility(View.GONE);
                    if (downApkDialog == null) {
                        NetNotify();
                        LogUtils.e(" onPageFinished 没有网络,请重试...");
                        downApkDialog.show();
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (NetworkUtils.isConnected()) {
                    mWebView.setVisibility(View.VISIBLE);
                    if (downApkDialog != null && mWebView.getProgress() == 100) {
                        downApkDialog.dismiss();
                        LogUtils.e(" onPageFinished  关闭提示框");
                    }
                    initConnectTion();
                } else {
                    mWebView.setVisibility(View.GONE);
                    if (downApkDialog != null && !downApkDialog.isShowing()) {
                        LogUtils.e(" onPageFinished 没有网络,请重试...");
                        downApkDialog.show();
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                LogUtils.d("shouldOverrideUrlLoading: url=" + url);
                return shouldIntercept(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                LogUtils.d("shouldInterceptRequest: url="+ url);
                return super.shouldInterceptRequest(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//                LogUtils.d("onConsoleMessage:" + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });

        mWebView.addJavascriptInterface(new JSInterface(), "jSInterface");
        loadContent(0);

        listener = new ImageProvider.ImageListener() {
            @Override
            public void onLoaded(String groupid, int index, boolean isSuccess) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("javascript:image_load_cb(");
                stringBuilder.append(index);
                stringBuilder.append(", ");
                stringBuilder.append(isSuccess ? "true" : "false");
                stringBuilder.append(", ");
                stringBuilder.append(isSuccess ? "true" : "false");
                stringBuilder.append(")");
                LoadUrlUtils.loadUrl(mWebView, stringBuilder.toString());
            }
        };
        ImageProvider.registerImageListener(listener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mWebView.reload();
        if (NetworkUtils.isConnected()) {
            mWebView.setVisibility(View.VISIBLE);
            mWebView.postDelayed(() -> LiveEventBus.get(Config.DO_CONNECT).post("Refresh"), 3000);
        } else {
            mWebView.setVisibility(View.GONE);
        }
    }


    private void NetNotify() {
        downApkDialog = new CommonDialog.Builder(this)
                .setTitle("提示")
                .setCancelable(false)
                .setMessage("没有网络,确认网络连接后刷新...")
                .setNegativeButton("确认", (dialogInterface, i) -> {
                    mWebView.reload();
                    dialogInterface.dismiss();
                }).create();

        WindowManager.LayoutParams params = Objects.requireNonNull(downApkDialog.getWindow()).getAttributes();
        params.gravity = Gravity.CENTER;
        downApkDialog.getWindow().setAttributes(params);
        downApkDialog.getWindow().setGravity(Gravity.CENTER);
    }


    private void loadContent(long delay) {
        LogUtils.d("loadContent:" + delay);
        delay += 100;
        mHandler.postDelayed(() -> LoadUrlUtils.loadUrl(mWebView, getJsContent()), delay);
    }

    private boolean shouldIntercept(WebView webView, String url) {
        if (!url.startsWith("bytedance://") && !url.startsWith("sslocal://")) {
            return false;
        }
        Uri parse = null;
        try {
            parse = Uri.parse(url);
        } catch (Exception e) {
            LogUtils.e(e);
        }
        if (parse != null) {
            boolean detectJs = "detectJs".equals(parse.getHost());
            boolean setcontent = "setContent".equals(parse.getQueryParameter("function"));
            boolean result = "false".equals(parse.getQueryParameter("result"));
            if (detectJs && setcontent && result) {
                PreloadWebView.loadBaseHtml(webView);
                loadContent(delay);
            }
            LogUtils.d(parse.getHost());
        }
        return true;
    }

    private String getJsContent() {
        InputStream is = null;
        StringBuilder content = new StringBuilder();
        try {
            is = getAssets().open("jscontent.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String temp;
            while ((temp = br.readLine()) != null) {
                content.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE) {
            for (String neededPermission : Config.NEEDED_PERMISSIONS) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, neededPermission)) {
                    Toast.makeText(this, "请允许应用读写外部存储", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageProvider.unregisterImageListener(listener);

        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (null != downApkDialog && downApkDialog.isShowing()) {
            downApkDialog.dismiss();
        }

        mHandler.removeCallbacksAndMessages(null);

        if (mWebView != null) {
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    public void setMessage(String message) {
        LogUtils.i("  数据：--------------》 " + message);
    }

    @Override
    public void setCommand(ResultAdb.DataBean message) {
//        LogUtils.json(message.toString());
        String data = message.getOperation();
//        LogUtils.d(data);
        switch (data) {
            case "Refresh":   //刷新
                mWebView.loadUrl("javascript:window.location.reload(true)");
                LogUtils.e("  webView刷新  ");
                break;

            case "ScreenCapture":   //screenshots
                LogUtils.e("  截屏操作  ");
                capture();
                break;

            case "Shutdown":
                PowerOnOffManager.getInstance(this).shutdown();
                break;

            case "InstallApk":
                LogUtils.i("安装apk");
                mWebView.postDelayed(this::installApk, 3000);
                break;

            case "Show":
                AppRunningUtil.isRunBackground(MainActivity.this.getApplicationContext());
                break;

            case "Reboot":
                LogUtils.e("  reboot.......... ");
                Utils.reboot();
                break;

            case "Power":
                //Power ; timeOn ; 2021-04-28 14:30 ;timeOff ; 2021-04-28 11:30
                int[] timeOn = timeOnOff(message.getTimeOn());
                int[] timeOff = timeOnOff(message.getTimeOff());
                LogUtils.d("<<---开机--->>" + Arrays.toString(timeOn) + "<<---关机--->>" + Arrays.toString(timeOff));
                PowerOnOffManager.getInstance(MainActivity.this).clearPowerOnOffTime();
                PowerOnOffManager.getInstance(MainActivity.this).setPowerOnOff(timeOn, timeOff);
                String onTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOnTime();
                boolean powerOnTime = PowerOnOffManager.getInstance(MainActivity.this).isSetPowerOnTime();
                LogUtils.d("  定时开机时间 " + onTime + "  是否设置开关机: " + powerOnTime);
                break;
            default:
                break;
        }
    }

    public class JSInterface {
        @JavascriptInterface
        public String pushMac() {
            String mac = null;
            if (NetworkUtils.isConnected()) {
                mac = UniqueIDUtils.getLocalMacIdFromIp().replaceAll(":", "").trim();
                mmkv.encode(Config.MAC, mac);
                LogUtils.d("  获取设备唯一标识 =  " + mac);
            }
            return mac;
        }

        @JavascriptInterface
        public void refresh() {
            OnClick();
        }
    }

    private void OnClick() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("加载数据中...");
            progressDialog.show();
        }
        if (NetworkUtils.isConnected()) {
            mWebView.setVisibility(View.VISIBLE);
            mWebView.loadUrl("javascript:window.location.reload(true)");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void installApk() {
        File file = new File(mFileName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(this, "com.bdxh.classbrand.fileprovider", file);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }
        try {
            this.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        View view = this.getWindow().getDecorView();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        try {
            String fileName = Environment.getExternalStorageDirectory().getPath() + "/webview_capture.jpg";
            FileOutputStream fos = new FileOutputStream(fileName);
            //压缩bitmap到输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            String filePath = "data:image/jpeg;base64,";
            byte[] bytes = ConvertUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG);
            String encode2String = EncodeUtils.base64Encode2String(bytes);
            String sb = String.format("%s%s", filePath, encode2String);
            LogUtils.a("Activity 截图转码Base64 <<--->> ");
            LiveEventBus.get(Config.KEY_DATA).post(sb);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        } finally {
            bitmap.recycle();
        }
    }

    private void setPowerOnOff() {
        Calendar instance = Calendar.getInstance();
        int[] timeoff = {instance.get(Calendar.YEAR), instance.get(Calendar.MONTH) + 1, instance.get(Calendar.DATE), 18, 59};
        instance.add(Calendar.DATE, 1);
        int[] timeon = {instance.get(Calendar.YEAR), instance.get(Calendar.MONTH) + 1, instance.get(Calendar.DATE), 7, 59};
        LogUtils.d("开机--->>" + Arrays.toString(timeon) + "<<---关机--->>" + Arrays.toString(timeoff));
        PowerOnOffManager.getInstance(MainActivity.this).clearPowerOnOffTime();
        PowerOnOffManager.getInstance(MainActivity.this).setPowerOnOff(timeon, timeoff);
        String offTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOffTime();
        String onTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOnTime();
        boolean powerOnTime = PowerOnOffManager.getInstance(MainActivity.this).isSetPowerOnTime();
        LogUtils.d("  定时关机时间 " + offTime + "  定时开机时间 " + onTime + "  是否设置开关机: " + powerOnTime);
    }

    private int[] timeOnOff(String data) {
        String onData = data.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "").trim();
        //年
        int year = Integer.parseInt(onData.substring(0, 4));
        //月
        int month = Integer.parseInt(onData.substring(4, 6));
        //日
        int day = Integer.parseInt(onData.substring(6, 8));
        //时
        int hour = Integer.parseInt(onData.substring(8, 10));
        //分
        int minute = Integer.parseInt(onData.substring(10, 12));
        return new int[]{year, month, day, hour, minute};
    }
}