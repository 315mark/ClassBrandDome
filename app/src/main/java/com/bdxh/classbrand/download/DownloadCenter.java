package com.bdxh.classbrand.download;


import com.blankj.utilcode.util.LogUtils;
import com.bdxh.classbrand.api.ApiService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 下载中心
 */
public class DownloadCenter{

    private static DownloadCenter instance;

    private static HttpPostListener mHttpListener;
    private static Retrofit retrofit;

    private List<ControlCallBack> callBackList = new ArrayList<>();
    private Set<DownloadCenterListener> listeners = new HashSet<>();

    private DownloadCenter() {
        init();
    }

    public static DownloadCenter getInstance(){
        if (instance == null) {
            synchronized (DownloadCenter.class) {
                if (instance == null) {
                    instance = new DownloadCenter();
                }
            }
        }
        return instance;
    }


    public void download(final String downUrl, File targetFile, final int downloadBytePerMs) {
        ControlCallBack callBack = null;
        for (ControlCallBack c : callBackList){
            if (c.getUrl().equals(downUrl)){
                callBack = c;
                break;
            }
        }
        if (callBack == null) {
            callBack = new ControlCallBack(downUrl, targetFile, downloadBytePerMs) {
                @Override
                public void onSuccess(String url) {

                    tellDownloadSuccess(url);
                }

                @Override
                public void onPaused(String url) {
                    tellDownloadPaused(url);
                }

                @Override
                public void onError(String url, Throwable e){
                    tellDownloadError(url, e);
                }

                @Override
                public void onDelete(String url) {
                    for (ControlCallBack c : callBackList) {
                        if (url.equals(c.getUrl())) {
                            callBackList.remove(c);
                            break;
                        }
                    }
                    tellDownloadDelete(url);
                }
            };
            callBackList.add(callBack);
        }
        if (callBack.isDownloading()){
            LogUtils.d( "downloading this task.");
            return;
        }

        tellDownloadStart(callBack);
        //init 初始化baseurl 并没有调用ApiService 里的接口 所以没有用到baseurl
        final ControlCallBack finalCallBack = callBack;
        retrofit.create(ApiService.class)
                .download(downUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(finalCallBack::saveFile)
                .doOnError(throwable ->{
                    LogUtils.e( "accept on error: " + downUrl ,throwable);
                    finalCallBack.onError(finalCallBack.getUrl(),throwable);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>(){
                    @Override
                    public void onSubscribe(Disposable d){

                    }

                    @Override
                    public void onNext(ResponseBody responseBody){

                    }

                    @Override
                    public void onError(Throwable e) {
                        finalCallBack.setState(DownloadTaskState.ERROR);
                        tellDownloadError(downUrl, e);
                    }

                    @Override
                    public void onComplete(){

                    }
                });
    }

    public void getVsersionCode(final RequestBody body) {
        retrofit.create(ApiService.class)
                .getVensionCode(body)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(ResultInfo ->{
                    mHttpListener.success(ResultInfo.string());
                })
                .doOnError(throwable ->{
                    throwable.printStackTrace();
                    mHttpListener.fail();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>(){
                    @Override
                    public void onSubscribe(Disposable d){

                    }

                    @Override
                    public void onNext(ResponseBody responseBody){

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete(){

                    }
                });
    }

    public void uploadScreenshot(final RequestBody body) {
        retrofit.create(ApiService.class)
                .uploadScreenshot(body)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(ResultInfo ->{
                    mHttpListener.success(ResultInfo.string());
                })
                .doOnError(throwable ->{
                    throwable.printStackTrace();
                    mHttpListener.fail();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>(){
                    @Override
                    public void onSubscribe(Disposable d){

                    }

                    @Override
                    public void onNext(ResponseBody responseBody){

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete(){

                    }
                });
    }


    private void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .writeTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(10L, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)//错误重联
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ApiService.BASE_URL)
                .build();
    }

    public void addListener(DownloadCenterListener l){
        listeners.add(l);
    }

    public void removeListener(DownloadCenterListener l){
        listeners.remove(l);
    }

    private void tellDownloadSuccess(String url){
        for (DownloadCenterListener l : listeners){
            l.onSuccess(url);
        }
    }

    private void tellDownloadPaused(String url){
        for (DownloadCenterListener l : listeners){
            l.onPaused(url);
        }
    }

    private void tellDownloadError(String url, Throwable e){
        for (DownloadCenterListener l : listeners) {
            l.onError(url, e);
        }
    }

    private void tellProgress(String url, long bytesRead, long contentLength, boolean done) {
        for (DownloadCenterListener l : listeners) {
            l.onProgress(url, bytesRead, contentLength, done);
        }
    }

    private void tellDownloadDelete(String url){
        for (DownloadCenterListener l : listeners){
            l.onDeleted(url);
        }
    }

    private void tellDownloadStart(ControlCallBack callBack){
        for (DownloadCenterListener l : listeners){
            l.onStart(callBack);
        }
    }

    //通过方法传递接口
    public void resultInfo(HttpPostListener httpListeners){
        mHttpListener = httpListeners;
    }

    public interface HttpPostListener{
        void success(String data);
        void fail();
    }
}

