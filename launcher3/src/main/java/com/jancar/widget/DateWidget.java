package com.jancar.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import com.android.launcher3.R;
import com.jancar.widget.sevice.DateWidgetService;
import com.jancar.widget.sevice.MediaWidgetService;
import com.jancar.widget.utils.FlyLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of App Widget functionality.
 */
public class DateWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
    }

    @Override
    public void onEnabled(Context context) {
        FlyLog.d("onEnabled");
        try {
            context.startService(new Intent(context, DateWidgetService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisabled(Context context) {
        FlyLog.d("onDisabled");
        try {
            context.stopService(new Intent(context, DateWidgetService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.d("onReceive");
        super.onReceive(context, intent);
    }

}

