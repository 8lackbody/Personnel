package com.zht.personnel;

import android.app.Application;
import android.content.Context;

public class ContextApplication extends Application {

    public final static String VERSION = "2.1.0";

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        ContextApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return ContextApplication.context;
    }
}
