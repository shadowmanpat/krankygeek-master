package com.example.sm.restartapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by sm on 9/28/17.
 */

public class MyApplicationInstance extends Application{

    public  static MyApplicationInstance instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    public static MyApplicationInstance getInstance() {
        return instance;
    }
}

