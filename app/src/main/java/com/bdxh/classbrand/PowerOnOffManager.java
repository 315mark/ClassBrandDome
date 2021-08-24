package com.bdxh.classbrand;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bdxh.classbrand.utils.Utils;

import java.util.Arrays;

public class PowerOnOffManager {

    private static final String TAG = "PowerOnOffManager";
    public static final String INTENT_ACTION_POWERONTIME = "android.intent.PowerOnTime";
    public static final String INTENT_ACTION_CLEARONTIME = "android.intent.ClearOnOffTime";
    public static final String INTENT_ACTION_POWERONOFF = "android.intent.action.setpoweronoff";
    public static final String PERSIST_SYS_POWERONOFF = "persist.sys.poweronoff";
    public static final String PERSIST_SYS_POWERONOFFBAK = "persist.sys.poweronoffbak";
    public static final String SHUTDOWN_ACTION = "android.intent.action.shutdown";
    public static final String PERSIST_SYS_POWERONMODE = "persist.sys.poweronmode";
    public static final String PERSIST_SYS_POWERONTIME = "persist.sys.powerontime";
    public static final String PERSIST_SYS_POWEROFFTIME = "persist.sys.powerofftime";
    public static final String PERSIST_SYS_POWERONTIMEPER = "persist.sys.powerontimeper";
    public static final String PERSIST_SYS_POWEROFFTIMEPER = "persist.sys.powerofftimeper";
    private static PowerOnOffManager powerOnOffManager;
    private Context mContext;

    private PowerOnOffManager(Context context) {
        this.mContext = context;
    }

    public static synchronized PowerOnOffManager getInstance(Context context) {
        if (powerOnOffManager == null) {
            powerOnOffManager = new PowerOnOffManager(context);
        }
        return powerOnOffManager;
    }

    public void shutdown() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.shutdown");
        this.mContext.sendBroadcast(intent);
    }

    public void clearPowerOnOffTime() {
        Intent intent = new Intent("android.intent.ClearOnOffTime");
        this.mContext.sendBroadcast(intent);
    }

    public boolean isSetPowerOnTime() {
        return !"off:on:".equals(this.getPowerOnTime());
    }

    public String getPowerOnMode() {
        return Utils.getValueFromProp("persist.sys.poweronmode");
    }

    public String getPowerOnTime() {

        return Utils.getValueFromProp("persist.sys.powerontime");
    }

    public String getPowerOffTime() {
        return Utils.getValueFromProp("persist.sys.powerofftime");
    }

    public String getLastestPowerOnTime() {
        return Utils.getValueFromProp("persist.sys.powerontimeper");
    }

    public String getLastestPowerOffTime() {
        return Utils.getValueFromProp("persist.sys.powerofftimeper");
    }

    public void setPowerOnOff(int[] powerOnTime, int[] powerOffTime) {
        Intent intent = new Intent("android.intent.action.setpoweronoff");
        intent.putExtra("timeon", powerOnTime);
        intent.putExtra("timeoff", powerOffTime);
        intent.putExtra("enable", true);
        this.mContext.sendBroadcast(intent);
        Log.d("PowerOnOffManager", "poweron:" + Arrays.toString(powerOnTime) + "/ poweroff:" + Arrays.toString(powerOffTime));
    }

    public void setPowerOnOffWithWeekly(int[] powerOnTime, int[] powerOffTime, int[] weekdays) {
        Intent intent = new Intent("android.intent.action.setyspoweronoff");
        intent.putExtra("timeon", powerOnTime);
        intent.putExtra("timeoff", powerOffTime);
        intent.putExtra("wkdays", weekdays);
        intent.putExtra("enable", true);
        this.mContext.sendBroadcast(intent);
        Log.d("PowerOnOffManager", "poweron:" + Arrays.toString(powerOnTime) + "/ poweroff:" + Arrays.toString(powerOffTime) + "/weekday:" + Arrays.toString(weekdays));
    }

    public void cancelPowerOnOff(int[] powerOnTime, int[] powerOffTime) {
        Intent intent = new Intent("android.intent.action.setpoweronoff");
        intent.putExtra("timeon", powerOnTime);
        intent.putExtra("timeoff", powerOffTime);
        intent.putExtra("enable", false);
        this.mContext.sendBroadcast(intent);
        Log.d("PowerOnOffManager", "poweron:" + Arrays.toString(powerOnTime) + "/ poweroff:" + Arrays.toString(powerOffTime));
    }
}
