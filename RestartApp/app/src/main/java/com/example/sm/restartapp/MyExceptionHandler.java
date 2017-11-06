package com.example.sm.restartapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sm on 9/28/17.
 */

public class MyExceptionHandler implements  Thread.UncaughtExceptionHandler {
    private Activity activity;
    int count;

    public  MyExceptionHandler(Activity a, int count){
        activity=a;
        this.count=count;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if(count<3){
            Intent intent = new Intent(activity, MainActivity.class);

            intent.putExtra("crash", true);
            intent.putExtra("count", count+1);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyApplicationInstance.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) MyApplicationInstance.getInstance().getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
            activity.finish();
            System.exit(2);
        }

    }
}
