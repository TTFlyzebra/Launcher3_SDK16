package com.jancar.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.jancar.widget.sevice.MediaWidgetService;
import com.jancar.widget.utils.FlyLog;

/**
 * Implementation of App Widget functionality.
 */
public class MediaWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        FlyLog.d("onUpdate");
    }

    @Override
    public void onEnabled(Context context) {
        FlyLog.d("onEnabled");
        try {
            context.startService(new Intent(context, MediaWidgetService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisabled(Context context) {
        FlyLog.d("onDisabled");
        try{
            context.stopService(new Intent(context, MediaWidgetService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.d("onReceive intent="+intent);
        super.onReceive(context, intent);
    }
}

