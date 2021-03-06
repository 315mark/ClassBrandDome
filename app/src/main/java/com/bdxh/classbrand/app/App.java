package com.bdxh.classbrand.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import com.bdxh.classbrand.provider.ImageProvider;
import com.bdxh.classbrand.utils.MyCacheEventListener;
import com.bdxh.classbrand.web.PreloadWebView;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.gson.Gson;
import com.jeremyliao.liveeventbus.LiveEventBus;

public class App extends Application {

    private static App app;
    private static Gson gson = new Gson();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        app = this;
    }

    public static App getApp() {
        return app;
    }


    public static Gson getGson() {
        return gson;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(this)
                .setCacheEventListener(MyCacheEventListener.getInstance()).build());

        Fresco.initialize(this,builder.build());
        ImageProvider.init();

        LiveEventBus.config()
                .autoClear(true)
                .lifecycleObserverAlwaysActive(true);

        preloadWebView();
    }

    private void preloadWebView() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                PreloadWebView.getInstance().preload();
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                PreloadWebView.getInstance().preload();
            }
        });
    }
}

