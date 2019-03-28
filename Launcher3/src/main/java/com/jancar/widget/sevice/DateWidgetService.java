package com.jancar.widget.sevice;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.android.launcher3.R;
import com.jancar.widget.DateWidget;
import com.jancar.widget.utils.FlyLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.provider.Settings.Global.AUTO_TIME;

public class DateWidgetService extends Service {
    private boolean bTime24 = true;
    private IntentFilter intentFilter;
    private TimeChangeReceiver timeChangeReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟变化
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//设置了系统时区
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);//设置了系统时间
            intentFilter.addAction(AUTO_TIME);
            timeChangeReceiver = new TimeChangeReceiver();
            registerReceiver(timeChangeReceiver, intentFilter);
            upView();
        } catch (Exception e) {
            FlyLog.d(e.toString());
        }

    }


    private void upView() {
        FlyLog.d("upView bTime24=" + bTime24);
        bTime24 = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24).equals("24");
        String tmpAmpm = getCurrentDate("a");
        String tmpTime = getCurrentDate(bTime24 ? "HH:mm" : "hh:mm");
        String tmpDate = getCurrentDate("yyyy-MM-dd");
        String tmpWeek = getCurrentWeek();
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.wg_date_widget);
        if(bTime24){
            views.setTextViewText(R.id.tv_ampm, "");
        }else{
            views.setTextViewText(R.id.tv_ampm, tmpAmpm);
        }
        views.setTextViewText(R.id.tv_time, tmpTime);
        views.setTextViewText(R.id.tv_date, tmpDate);
        views.setTextViewText(R.id.tv_week, tmpWeek);
        ComponentName componentName = new ComponentName(DateWidgetService.this, DateWidget.class);
        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(componentName, views);
    }


    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(timeChangeReceiver);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        super.onDestroy();
    }

    private String[] weeks = new String[7];

    private static String getCurrentDate(String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(date);
    }

    private String getCurrentWeek() {
        weeks = new String[]{getString(R.string.tv_str_sunday),
                getString(R.string.tv_str_monday),
                getString(R.string.tv_str_tuesday),
                getString(R.string.tv_str_wednesday),
                getString(R.string.tv_str_thursday),
                getString(R.string.tv_str_friday),
                getString(R.string.tv_str_saturday)};
        Date date = new Date(System.currentTimeMillis());
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);
        return weeks[mCalendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIMEZONE_CHANGED:
                    upView();
                    break;
            }
        }
    }
}
