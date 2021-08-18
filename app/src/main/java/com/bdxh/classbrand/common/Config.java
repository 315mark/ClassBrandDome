package com.bdxh.classbrand.common;

import android.Manifest;

public class Config {

    //svn://120.24.246.112/bdxh/project/SmartCampus/client/VoiceDemo

    public static final String PARAM = "param";
    public static final String TOKEN = "token";
    public static final String BLEDEVICE = "bledevice";
    public static final String isLOGIN = "isLogin";
    public static final String isPOLLCONTROL = "isPollControl";
    public static final String CONN_BLE_MAC="connBleMac";
    public static final String CONN_TIME="connTime";
    public static final String isPOLL = "isPoll";
    public static final String Config_Title = "config_title";
    public static final String Config_Show_Icon = "showIcon";
    public static final String MAC = "mac";

    /**
     * SharedPreferences名字
     */
    public static final String SP_NAME = "load_resource";
    /**
     * 资源路径
     */
    public static final String SP_RESOURCE_PATH = "sp_resource_path";


    public static final String KEY_DATA = "key_data";
    public static final String[] mPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION ,
            Manifest.permission.ACCESS_COARSE_LOCATION ,
            Manifest.permission.WRITE_EXTERNAL_STORAGE ,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static final String[] NEEDED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static final int REQUEST_CODE_OPEN_GPS = 1;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final int REQUEST_CODE_LOCATION = 887;
    public static final int REQUEST_CODE_BLUETOOTH = 886;
    public static final int REQUEST_READ_PHONE_STATE = 885;
    public static final int RUSH_DATA_INFO = 233;

    public static String DO_CONNECT = "connect";//重连
    public static String HOST = "tcp://myxxmq.bdxhtx.com:1883";//服务器地址（协议+地址+端口号）
    public static String USERNAME = "mingyuan";//用户名
    public static String PASSWORD = "mingyuan888888";//密码
    public static String PUBLISH_TOPIC = "/demo/test/log";//发布主题  这种大家都能接收到
    public static String RESPONSE_TOPIC = "demo/src";//响应主题
    public static String SUBSCRIBE_TOPIC = "/class/brand/";// 订阅主题  后台服务单独发给 根据mac地址进行
    public static String SUBSCRIBE_TOPIC_STATE = "/class/brandstate";// 订阅主题  后台服务单独发给 根据mac地址进行

   //  客户端ID，一般以客户端唯一标识符表示，这里用 mac 表示 /class/brand/mac



    public static final int MQTT_PUSH_SUCCESS_MSG = 18;
    public static final int EVENT_SCREENSHOT = 22;//截图事件

    public static final int RECONNECT = 122;

}
