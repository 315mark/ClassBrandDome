package com.bdxh.classbrand.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.blankj.utilcode.util.LogUtils;
import com.bdxh.classbrand.MainActivity;
import com.bdxh.classbrand.app.App;
import com.bdxh.classbrand.utils.AppRunningUtil;

public class BootBroadcastRecriver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i("BootBroadcastReceiver --- onReceive: " + intent.getAction());
        if (ACTION.equals(intent.getAction())) {
            //要启动的Activity
            Intent bootIntent = new Intent(context, MainActivity.class);
            bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(bootIntent);
        }

        if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
          //表示按了home键,程序到了后台
            AppRunningUtil.isRunBackground(App.getApp());
        }
    }
}
