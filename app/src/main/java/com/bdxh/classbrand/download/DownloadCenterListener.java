package com.bdxh.classbrand.download;

/**
 * 监听器
 */
public abstract class DownloadCenterListener {

    public void onStart(ControlCallBack callBack) {

    }

    public void onSuccess(String url) {
    }

    public void onError(String url, Throwable e) {
    }

    public void onDeleted(String url) {

    }

    public void onProgress(String url, long bytesRead, long contentLength, boolean done) {

    }

    public void onPaused(String url) {

    }

}
