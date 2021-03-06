package com.bdxh.classbrand.utils;

import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public Utils() {
    }

    public static void setValueToProp(String key, String val) {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getDeclaredMethod("set", String.class, String.class);
            method.invoke(classType, key, val);
        } catch (ClassNotFoundException var4) {
            var4.printStackTrace();
        } catch (NoSuchMethodException var5) {
            var5.printStackTrace();
        } catch (InvocationTargetException var6) {
            var6.printStackTrace();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    public static String getValueFromProp(String key) {
        String value = "";

        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            value = (String)getMethod.invoke(classType, key);
        } catch (Exception var4) {
        }

        return value;
    }

    public static boolean validate(int year, int month, int day, int hourOfDay, int minute) {
        if (year > 2099 && year < 2017) {
            return false;
        } else if (month >= 0 && month <= 11) {
            int[] monthLengths = new int[]{31, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30};
            if (isLeapYear(year)) {
                monthLengths[2] = 29;
            } else {
                monthLengths[2] = 28;
            }

            int monthLength = monthLengths[month];
            if (day >= 1 && day <= monthLength) {
                if (hourOfDay >= 0 && hourOfDay <= 23) {
                    return minute >= 0 && minute <= 59;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }


    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @return
     */
    public static String getVerifyLastYearDate(Date dqrq){
        int year=Integer.parseInt(new  SimpleDateFormat("yyyy").format(dqrq));
        if(new SimpleDateFormat("MM-dd").format(dqrq).equals("12-31")){
            return year+"-12-31";
        }else{
            return (year-1)+"-12-31";
        }
    }

    public static void reboot(){
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out =  process.getOutputStream();
            String cmd = "reboot ";
            out.write(cmd.getBytes());
            out.flush();
            out.close();

            InputStream fis=process.getInputStream();
            //??????????????????????????????
            InputStreamReader isr=new InputStreamReader(fis);
            //??????????????????
            BufferedReader br=new BufferedReader(isr);
            String line=null;
            //?????????????????? ????????????????????????????????????????????????????????????
            while((line=br.readLine())!=null)
            {
                LogUtils.e(line);
            }
            process =null;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
