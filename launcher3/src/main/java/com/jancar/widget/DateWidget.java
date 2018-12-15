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

    private AppWidgetManager mAppWidgerManager;
    private Context mContext;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable upTask = new Runnable() {
        @Override
        public void run() {
            String tmpTime = getCurrentDate(TIME_FORMAT);
            String tmpDate = getCurrentDate(DATE_FORMAT);
            String tmpWeek = getCurrentWeek();
            if (!(tmpTime.equals(time) && tmpDate.equals(date) && tmpWeek.equals(week))) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.date_widget);
                FlyLog.d("Timer schedule up time......");
                views.setTextViewText(R.id.tv_time, tmpTime);
                time = tmpTime;
                FlyLog.d("Timer schedule up date......");
                views.setTextViewText(R.id.tv_date, tmpDate);
                date = tmpDate;
                FlyLog.d("Timer schedule up week......");
                views.setTextViewText(R.id.tv_week, tmpWeek);
                week = tmpWeek;
                ComponentName componentName = new ComponentName(mContext, DateWidget.class);
                mAppWidgerManager.updateAppWidget(componentName, views);
            }
        }
    };

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        FlyLog.e("onEnabled");
        this.mAppWidgerManager = AppWidgetManager.getInstance(context.getApplicationContext());
        this.mContext = context;
        //定义计时器
        //启动周期性调度
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FlyLog.d("Timer schedule run......");
                //发送空消息，通知界面更新
                mHandler.post(upTask);
            }
        }, 0, 1000);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        FlyLog.e("onDisabled");
        mTimer.cancel();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.e("onReceive");
        super.onReceive(context, intent);
    }


    private String[] weeks = new String[7];
    private String time = "";
    private String date = "";
    private String week = "";
    private final String TIME_FORMAT = "HH:mm";
    private final String DATE_FORMAT = "yyyy-MM-dd";

    private static String getCurrentDate(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(date);
    }

    private String getCurrentWeek() {
        if (mContext == null) return "------";
        weeks = new String[]{mContext.getString(R.string.tv_str_sunday),
                mContext.getString(R.string.tv_str_monday),
                mContext.getString(R.string.tv_str_tuesday),
                mContext.getString(R.string.tv_str_wednesday),
                mContext.getString(R.string.tv_str_thursday),
                mContext.getString(R.string.tv_str_friday),
                mContext.getString(R.string.tv_str_saturday)};
        Date date = new Date(System.currentTimeMillis());
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);
        return weeks[mCalendar.get(Calendar.DAY_OF_WEEK) - 1];
    }
}

