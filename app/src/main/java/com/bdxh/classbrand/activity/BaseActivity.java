package com.bdxh.classbrand.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bdxh.classbrand.IGetMessageCallBack;
import com.bdxh.classbrand.service.MQTTService;

public abstract class BaseActivity extends AppCompatActivity {
    private Context mContext;
    private MyServiceConnection serviceConnection;

    public MyServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public abstract int initLayout();

    public abstract void initBind();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout());
        mContext = this;
        initBind();
    }

    // 此处申请绑定
    public void initConnectTion() {
        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack((IGetMessageCallBack) mContext);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    protected void onDestroy() {
        // 要在此处进行解绑
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }
}
