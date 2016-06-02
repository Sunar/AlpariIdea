package com.alpari.avia_kos.alpidea;

import android.app.Application;
import android.content.Context;

/**
 * Created by Avia-Kos on 02.06.16.
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
