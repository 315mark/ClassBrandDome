package com.bdxh.classbrand.api;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiService {

    String BASE_URL = "http://myxx.bdxhtx.com:9600";
    //测试接口
//    String BASE_URL = "http://81.71.124.21:1883";
//    String BASE_URL = "http://81.71.124.21:9600";

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);

    @Streaming
    @POST("/management/app/getMDeviceVersionInfo.do")
    Observable<ResponseBody> getVensionCode(@Body RequestBody requestBody);

    @Streaming
    @POST("/management/equipment/uploadSystemScreenshot.do")
    Observable<ResponseBody> uploadScreenshot(@Body RequestBody requestBody);

}
