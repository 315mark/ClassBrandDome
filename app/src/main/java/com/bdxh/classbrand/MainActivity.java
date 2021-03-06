package com.bdxh.classbrand;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
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

import com.bdxh.classbrand.activity.BaseActivity;
import com.bdxh.classbrand.bean.ResultAdb;
import com.bdxh.classbrand.common.Config;
import com.bdxh.classbrand.common.UniqueIDUtils;
import com.bdxh.classbrand.dialog.CommonDialog;
import com.bdxh.classbrand.provider.ImageProvider;
import com.bdxh.classbrand.utils.AppRunningUtil;
import com.bdxh.classbrand.utils.LoadUrlUtils;
import com.bdxh.classbrand.utils.TimerSetting;
import com.bdxh.classbrand.utils.Utils;
import com.bdxh.classbrand.web.PreloadWebView;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
    int beginHour, beginMin,  endHour, endMin;
    private Runnable TimerCheck ; //??????
    boolean isRunning = false;
    private List<ResultAdb.DataBean> dataList= new ArrayList<>();

    @Override
    public int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
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
        settings.setSupportZoom(false); //????????????
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDisplayZoomControls(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mWebView.setVisibility(View.VISIBLE);
                LogUtils.d("onPageStarted:  url=" + url);
                progressDialog.setMessage("???????????????...");
                progressDialog.show();

                if (!NetworkUtils.isConnected()) {
                    mWebView.setVisibility(View.GONE);
                    if (downApkDialog == null) {
                        NetNotify();
                        LogUtils.e(" onPageFinished ????????????,?????????...");
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
                        LogUtils.e(" onPageFinished  ???????????????");
                    }
                    initConnectTion();
                } else {
                    mWebView.setVisibility(View.GONE);
                    if (downApkDialog != null && !downApkDialog.isShowing()) {
                        LogUtils.e(" onPageFinished ????????????,?????????...");
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

            @Nullable
            @Override
            public Bitmap getDefaultVideoPoster() {
                try{
                    //?????????????????????h5??????????????? ?????????   ?????????????????????
                    LogUtils.w(" ????????????????????????");
                    return BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.mipmap.vidoe_icon);

                }catch(Exception e){
                    LogUtils.w(" ??????????????????????????????,,,,,,,,,,,,,");
                    return super.getDefaultVideoPoster();
                }
            }
        });

        mWebView.addJavascriptInterface(new JSInterface(), "jSInterface");
        loadContent(0);

        listener = new ImageProvider.ImageListener(){
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

    private void checkTask(ResultAdb.DataBean dataBean,Date dateon,Date dateoff){
        TimerCheck = () -> {
          try {
                isRunning = true;
                LogUtils.d("  ????????????????????????  ");
                boolean isRange = TimerSetting.isEffectiveDate(dateon, dateoff);
                registTimer(dataBean,isRange);
            } catch (Exception e) {
                isRunning = false;
                e.printStackTrace();
            }
        };
        mHandler.post(TimerCheck);
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* if (!isRunning) {
            String power_on = mmkv.decodeString(Config.POWER_ON,"");
            String power_off = mmkv.decodeString(Config.POWER_OFF,"");
            if (!power_on.isEmpty() && !power_off.isEmpty()){
                Date dateon = TimeUtils.string2Date(power_on,new SimpleDateFormat("yyyy-MM-dd HH:mm"));
                Date dateoff = TimeUtils.string2Date(power_off , new SimpleDateFormat("yyyy-MM-dd HH:mm"));
                boolean isRange = TimerSetting.isEffectiveDate(dateon, dateoff);
                registTimer(power_on,power_off ,isRange);
            }
            LogUtils.d("????????????");
        }*/
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
                .setTitle("??????")
                .setCancelable(false)
                .setMessage("????????????,???????????????????????????...")
                .setNegativeButton("??????", (dialogInterface, i) -> {
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
                    Toast.makeText(this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
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
        LogUtils.i("  ?????????--------------??? " + message);
    }

    @Override
    public void setCommand(ResultAdb.DataBean message) {
//        LogUtils.json(message.toString());
        String data = message.getOperation();
//        LogUtils.d(data);
        switch (data) {
            case "Refresh":   //??????
                mWebView.loadUrl("javascript:window.location.reload(true)");
                LogUtils.e("  webView??????  ");
                break;

            case "ScreenCapture":   //screenshots
                LogUtils.e("  ????????????  ");
                capture();
                break;

            case "Shutdown":
                PowerOnOffManager.getInstance(this).shutdown();
                break;

            case "InstallApk":
                LogUtils.i("??????apk");
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
                dataList.add(message);
                Date dateon = TimeUtils.string2Date(message.getTimeOn(), new SimpleDateFormat("yyyy-MM-dd HH:mm"));
                Date dateoff = TimeUtils.string2Date(message.getTimeOff(), new SimpleDateFormat("yyyy-MM-dd HH:mm"));
                checkTask(message ,dateon, dateoff);
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
                LogUtils.d("  ???????????????????????? =  " + mac);
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
            progressDialog.setMessage("???????????????...");
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
            //??????bitmap???????????????
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            String filePath = "data:image/jpeg;base64,";
            byte[] bytes = ConvertUtils.bitmap2Bytes(bitmap, Bitmap.CompressFormat.JPEG);
            String encode2String = EncodeUtils.base64Encode2String(bytes);
            String sb = String.format("%s%s", filePath, encode2String);
            LogUtils.a("Activity ????????????Base64 <<--->> ");
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
        LogUtils.d("??????--->>" + Arrays.toString(timeon) + "<<---??????--->>" + Arrays.toString(timeoff));
        PowerOnOffManager.getInstance(MainActivity.this).clearPowerOnOffTime();
        PowerOnOffManager.getInstance(MainActivity.this).setPowerOnOff(timeon, timeoff);
        String offTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOffTime();
        String onTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOnTime();
        boolean powerOnTime = PowerOnOffManager.getInstance(MainActivity.this).isSetPowerOnTime();
        LogUtils.d("  ?????????????????? " + offTime + "  ?????????????????? " + onTime + "  ?????????????????????: " + powerOnTime);
    }

    private int[] timeOnOff(String data,String type) {
        String onData = data.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "").trim();
        //???
        int year = Integer.parseInt(onData.substring(0, 4));
        //???
        int month = Integer.parseInt(onData.substring(4, 6));
        //???
        int day = Integer.parseInt(onData.substring(6, 8));
        //???
        int hour = Integer.parseInt(onData.substring(8, 10));
        //???
        int minute = Integer.parseInt(onData.substring(10, 12));
        if (type.equals("off")){
            beginHour = hour;
            beginMin = minute ;
        }else if ( type .equals("on")){
            endHour= hour;
            endMin = minute ;
        }
        return new int[]{year, month, day, hour, minute};
    }

    private void registTimer(ResultAdb.DataBean dataBean, boolean isRange){
        //Power ; timeOn ; 2021-04-28 14:30 ;timeOff ; 2021-04-28 11:30
        int[] timeOn = timeOnOff(dataBean.getTimeOn() , "on");
        int[] timeOff = timeOnOff(dataBean.getTimeOff(),"off");
        LogUtils.d(" ?????????????????? " + isRange);
        PowerOnOffManager.getInstance(MainActivity.this).clearPowerOnOffTime();
        if (isRange){
            PowerOnOffManager.getInstance(MainActivity.this).setPowerOnOff(timeOn, timeOff);
            LogUtils.d("<<---   ????????????????????????  --->>");

            dataList.remove(dataBean);
            if (!dataList.isEmpty()){
                mmkv.encode(Config.POWER_ON,dataList.get(0).getTimeOn());
                mmkv.encode(Config.POWER_OFF,dataList.get(0).getTimeOff());
            }
            mHandler.removeCallbacks(TimerCheck);
            isRunning = false;
        }else{
            isRunning = true;
            mHandler.postDelayed(TimerCheck , 20000);
        }
        LogUtils.d("<<-- -??????--->>" + Arrays.toString(timeOn) + "<<---??????--->>" + Arrays.toString(timeOff));
        String onTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOnTime();
        boolean powerOnTime = PowerOnOffManager.getInstance(MainActivity.this).isSetPowerOnTime();
        LogUtils.d("  ?????????????????? " + onTime + "  ?????????????????????: " + powerOnTime);
    }

    private void registTimer(String TimeOn , String TimeOff , boolean isRange){
        //Power ; timeOn ; 2021-04-28 14:30 ;timeOff ; 2021-04-28 11:30
        int[] timeOn = timeOnOff(TimeOn , "on");
        int[] timeOff = timeOnOff(TimeOff,"off");

        LogUtils.d(" ?????????????????? " + isRange);
        PowerOnOffManager.getInstance(MainActivity.this).clearPowerOnOffTime();
        if (isRange){
            PowerOnOffManager.getInstance(MainActivity.this).setPowerOnOff(timeOn, timeOff);
            LogUtils.d("<<---   ????????????????????????  --->>");
            mHandler.removeCallbacks(TimerCheck);
            isRunning = false;
        }else{
            isRunning = true;
            mHandler.postDelayed(TimerCheck , 20000);
        }
        LogUtils.d("<<-- registTimer -??????--->>" + Arrays.toString(timeOn) + "<<---??????--->>" + Arrays.toString(timeOff));
        String onTime = PowerOnOffManager.getInstance(MainActivity.this).getPowerOnTime();
        boolean powerOnTime = PowerOnOffManager.getInstance(MainActivity.this).isSetPowerOnTime();
        LogUtils.d(" registTimer  ?????????????????? " + onTime + "  ?????????????????????: " + powerOnTime);
    }
}