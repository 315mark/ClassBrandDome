package com.bdxh.classbrand.utils;

import android.text.TextUtils;
import android.webkit.WebView;

import com.blankj.utilcode.util.LogUtils;

public class LoadUrlUtils {

    private LoadUrlUtils(){}


    public static void loadUrl(final WebView web, final String url) {
        if (web == null || TextUtils.isEmpty(url)) {
            return;
        }
        LogUtils.d("loadurl:" + url);
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                if ("javascript:".startsWith(url)) {
                    try {
                        web.evaluateJavascript(url, null);
                        i = 1;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (i == 0) {
                    try {
                        web.loadUrl(url);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
