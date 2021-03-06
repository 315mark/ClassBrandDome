package com.bdxh.classbrand.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;


import com.bdxh.classbrand.BuildConfig;
import com.bdxh.classbrand.IGetMessageCallBack;
import com.bdxh.classbrand.MainActivity;
import com.bdxh.classbrand.app.App;
import com.bdxh.classbrand.bean.DeviceVersionInfo;
import com.bdxh.classbrand.bean.MsgInfo;
import com.bdxh.classbrand.bean.ResultAdb;
import com.bdxh.classbrand.bean.ResultInfo;
import com.bdxh.classbrand.common.Config;
import com.bdxh.classbrand.common.UniqueIDUtils;
import com.bdxh.classbrand.dialog.CommonDialog;
import com.bdxh.classbrand.download.ControlCallBack;
import com.bdxh.classbrand.download.DownloadCenter;
import com.bdxh.classbrand.download.DownloadCenterListener;
import com.bdxh.classbrand.utils.AppRunningUtil;
import com.bdxh.classbrand.utils.GsonUtil;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tencent.mmkv.MMKV;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import androidx.lifecycle.Observer;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MQTTService extends Service {

    public static final String TAG = MQTTService.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    private ResultAdb bean = new ResultAdb();

    private DeviceVersionInfo versionInfo;

    private String host = Config.HOST;
    private String userName = Config.USERNAME;
    private String passWord = Config.PASSWORD;
    private static String myTopic = Config.SUBSCRIBE_TOPIC + UniqueIDUtils.getLocalMacIdFromIp().replace(":", "").trim();//??????????????????
    private static String sendTopic = Config.SUBSCRIBE_TOPIC_STATE;//?????????????????????
    private String clientId = UUID.randomUUID().toString().replace("-", "");//???????????????
    private IGetMessageCallBack mGetMessageCallBack;


    @Override
    public void onCreate() {
        super.onCreate();
        // onBind ???????????? ??????????????????

        init();
        LiveEventBus.get(Config.KEY_DATA, String.class).observeForever(observer);
        LiveEventBus.get(Config.DO_CONNECT, String.class).observeForever(observer2);

    }

    private Observer<String> observer = s -> {
        ThreadUtils.getCpuPool().submit(() -> uploadScreenshot(s));
    };

    private Observer<String> observer2 = s -> {
        getVsersion();
    };

    public static void publish(String msg) {
        String topic = sendTopic;
        Integer qos = 0;
        Boolean retained = false;
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public static void publish(String msg, IGetMessageCallBack callBack) {
        String topic = sendTopic;
        Integer qos = 0;
        Boolean retained = false;
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        MMKV mmkv = MMKV.defaultMMKV();
        clientId = mmkv.decodeString(Config.MAC);
        if (TextUtils.isEmpty(clientId)) {
            clientId = UniqueIDUtils.getLocalMacIdFromIp().replaceAll(":", "").trim();
        }
        LogUtils.e(" MyMqttService ??????mac??????  ==>>  " + clientId);

        // ????????????????????????+??????+????????????
        String uri = host;
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), uri, clientId);
        // ??????MQTT????????????????????????
        mqttAndroidClient.setCallback(mqttCallback);
        mMqttConnectOptions = new MqttConnectOptions();
        // ????????????
//        mMqttConnectOptions.setCleanSession(true);
        // ?????????????????????????????????
//        mMqttConnectOptions.setConnectionTimeout(10);
        // ????????????????????????????????????
//        mMqttConnectOptions.setKeepAliveInterval(20);
        // ?????????
        mMqttConnectOptions.setUserName(userName);
        // ??????
        mMqttConnectOptions.setPassword(passWord.toCharArray());     //????????????????????????????????????

        // mq ????????????
        mMqttConnectOptions.setCleanSession(true);
        mMqttConnectOptions.setKeepAliveInterval(20);
        mMqttConnectOptions.setConnectionTimeout(10);
        mMqttConnectOptions.setAutomaticReconnect(true);
        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        LogUtils.d( " message : " + message);
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // ???????????????
            // MQTT??????????????????????????????????????????????????????????????????????????????????????????Broker???????????????
            //?????????????????????Broker??????????????????LWT???Bro???ker??????????????????????????????????????????
            //??????????????????????????????Broker????????????????????????topic????????????????????????LWT?????????
            try {
                mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                LogUtils.i(" Exception Occured : ", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        if (doConnect) {
            doClientConnection();
        }
    }

    @Override
    public void onDestroy(){
        // ????????????MQTT??????????????????
        destroy();
        //??????????????????
        super.onDestroy();
    }

    public static void destroy() {
        try {
            LogUtils.d("????????????");
            mqttAndroidClient.disconnect(); //????????????
            if (mqttAndroidClient != null) {
                mqttAndroidClient.close();
                mqttAndroidClient.unregisterResources();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????MQTT?????????
     */
    private void doClientConnection() {
        if (!isAlreadyConnected() && isConnectIsNormal()){
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isAlreadyConnected() {
        if (mqttAndroidClient != null) {
            try {
                return mqttAndroidClient.isConnected();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    // MQTT??????????????????
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            LogUtils.i("???????????? ");
            try {
                // ??????myTopic??????
                LogUtils.i(" IMqttActionListener :" + myTopic);
                mqttAndroidClient.subscribe(myTopic, 0);
            } catch (MqttException e) {
                e.printStackTrace();
                LogUtils.e(" IMqttActionListener :" + e.getMessage());
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
        }
    };

    // MQTT????????????????????????
    private MqttCallback mqttCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            try {
                LogUtils.i("???????????? ------------mqttCallback");
                // ??????myTopic??????
                LogUtils.i(" connectComplete :" + myTopic);
                mqttAndroidClient.subscribe(myTopic, 0);
            } catch (MqttException e) {
                e.printStackTrace();
                LogUtils.e(" connectComplete :" + e.getMessage());
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String str1 = new String(message.getPayload());
//            LogUtils.i( "mGetMessageCallBack:" + mGetMessageCallBack);

//            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();

            LogUtils.i(MessageFormat.format("??????????????? {0}", str1));
            ResultAdb bean = GsonUtil.GsonToBean(new String(message.getPayload()), ResultAdb.class);
            String operation = bean.data.getOperation();
            if ("Update".equals(operation) || "Refresh".equals(operation)){
                ThreadUtils.getCpuPool().execute(() -> getVsersion());
            }

            if ("GetState".equals(operation)) {
                ThreadUtils.getCpuPool().execute(() -> pushInfo());
            }

            if (mGetMessageCallBack != null) {
                LogUtils.i(bean.data);
                mGetMessageCallBack.setCommand(bean.data);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            //publish??????????????????
            LogUtils.i("??????????????????");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            LogUtils.i("???????????? ");
        }
    };

    /**
     * ????????????????????????
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            LogUtils.i("MQTT?????????????????????" + name);
            return true;
        } else {
            LogUtils.i( "MQTT ??????????????????");
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new CustomBinder();
    }

    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack) {
        this.mGetMessageCallBack = IGetMessageCallBack;
    }

    public class CustomBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * ?????? ??????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void response(String message) {
        String topic = Config.SUBSCRIBE_TOPIC_STATE;
        Integer qos = 1;
        Boolean retained = false;
        try {
            //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void pushInfo() {
        bean.data = new ResultAdb.DataBean();
        bean.data.setMac(clientId);
        bean.data.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        bean.data.setVersionId(BuildConfig.VERSION_NAME);
        bean.data.setIp(NetworkUtils.getIPAddress(true));
        bean.data.setIpV6(NetworkUtils.getIPAddress(false));
        bean.data.setNetIp(NetworkUtils.getIpAddressByWifi());
        //???????????????????????????Toast???????????????????????????UI???????????????????????????EventBus????????????
        //TODO  ???????????????????????????????????????????????????????????????????????????????????????????????? ????????????????????????
        response(GsonUtil.BeanToJson(bean));
        LogUtils.json(" ??????json??????????????? " + GsonUtil.BeanToJson(bean));
    }


    private void uploadScreenshot(String str) {
        ResultInfo info = new ResultInfo();
        ResultInfo.DataBean result = new ResultInfo.DataBean();
        result.setDeviceCode(clientId);
        result.setMediaFileBase64(str);
        info.setData(result);
        String toJson = GsonUtil.BeanToJson(info);
        MediaType JSON = MediaType.parse("application/json");
        DownloadCenter.getInstance().uploadScreenshot(RequestBody.create(JSON, toJson));
        DownloadCenter.getInstance().resultInfo(new DownloadCenter.HttpPostListener() {
            @Override
            public void success(String data) {
                MsgInfo bean = GsonUtil.GsonToBean(data, MsgInfo.class);
                if (!"1".equals(bean.result)) {
                    return;
                }
                LogUtils.d("success: " + bean.getJsonData().getPrimaryFileUrl());
            }

            @Override
            public void fail() {
                LogUtils.e("??????????????????");
            }
        });
    }

    private void getVsersion() {
        String appVersionCode = BuildConfig.VERSION_NAME;
        ResultInfo info = new ResultInfo();
        ResultInfo.DataBean result = new ResultInfo.DataBean();
        result.setDeviceType("1");
        result.setVersionId("1");
        info.setData(result);
        String toJson = GsonUtil.BeanToJson(info);
        MediaType JSON = MediaType.parse("application/json");
        DownloadCenter.getInstance().getVsersionCode(RequestBody.create(JSON, toJson));
        DownloadCenter.getInstance().resultInfo(new DownloadCenter.HttpPostListener() {
            @Override
            public void success(String data){
                MsgInfo bean = GsonUtil.GsonToBean(data, MsgInfo.class);
                LogUtils.d("?????? " + bean.toString());
                if (!"1".equals(bean.result)){
                    return;
                }

                versionInfo = bean.getJsonData().getDeviceVersionInfo();
                String versionCode = versionInfo.getVersionCode();
                if (versionCode.isEmpty()) {
                    return;
                }
                LogUtils.d(" ?????????????????? " + versionCode);
                int i = AppRunningUtil.compareVersion(versionCode, appVersionCode);
                String url = versionInfo.getPackageUrl();
                LogUtils.d("??????????????????????????? " + i+ " <-----> " + appVersionCode);
                if (i == 1 && !url.isEmpty()) {
                    //???????????????????????????????????????
                    downLoadApk(url);
                }
            }

            @Override
            public void fail() {
                LogUtils.e("??????????????????");
            }
        });
    }

    private void downLoadApk(String url) {
        LogUtils.e(url);
        downloadUrl(url, new File(Environment.getExternalStorageDirectory(), "class_brand.apk"));
        DownloadCenter.getInstance().addListener(mDownloadCenterListener);
    }

    private void downloadUrl(final String url, File targetFile) {
        DownloadCenter.getInstance().download(url, targetFile, 1000000);
    }

    private DownloadCenterListener mDownloadCenterListener = new DownloadCenterListener() {
        @Override
        public void onStart(final ControlCallBack callBack) {
            super.onStart(callBack);
        }

        @Override
        public void onPaused(final String url) {
            super.onPaused(url);
        }

        @Override
        public void onSuccess(final String url) {
            super.onSuccess(url);
            LogUtils.i(" ???????????? ");
            if (mGetMessageCallBack != null) {
                ResultAdb.DataBean bean = new ResultAdb.DataBean();
                bean.setOperation("InstallApk");
                mGetMessageCallBack.setCommand(bean);
            }
        }

        @Override
        public void onError(final String url, Throwable e) {
            super.onError(url, e);
        }

        @Override
        public void onProgress(final String url, final long bytesRead, final long contentLength, final boolean done) {
            super.onProgress(url, bytesRead, contentLength, done);
        }

        @Override
        public void onDeleted(final String url) {
            super.onDeleted(url);
        }
    };
}
