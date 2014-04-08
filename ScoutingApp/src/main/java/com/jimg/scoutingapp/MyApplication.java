package com.jimg.scoutingapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Jim on 3/15/14.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Context applicationContext = getApplicationContext();
        Ion.getDefault(applicationContext).configure().setLogging("MyLogs", Log.ERROR);

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(applicationContext)
                .defaultDisplayImageOptions(
                        new DisplayImageOptions.Builder()
                                .cacheInMemory(true)
                                .cacheOnDisc(true)
                                .build()
                )
                .build();

        ImageLoader.getInstance().init(config);
    }
}
