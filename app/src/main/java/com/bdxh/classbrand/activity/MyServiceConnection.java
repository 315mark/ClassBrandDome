package com.bdxh.classbrand.activity;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.bdxh.classbrand.IGetMessageCallBack;
import com.bdxh.classbrand.service.MQTTService;

public class MyServiceConnection implements ServiceConnection {

    private MQTTService mqttService;
    private IGetMessageCallBack mIGetMessageCallBack ;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mqttService = ((MQTTService.CustomBinder) iBinder).getService();
        mqttService.setIGetMessageCallBack(mIGetMessageCallBack);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public MQTTService getMqttService() {
        return mqttService;
    }

    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack) {
        mIGetMessageCallBack = IGetMessageCallBack;
    }

}
