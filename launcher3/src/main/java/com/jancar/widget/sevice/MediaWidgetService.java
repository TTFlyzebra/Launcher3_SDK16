package com.jancar.widget.sevice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.android.launcher3.R;
import com.jancar.media.JacMediaController;
import com.jancar.widget.MediaWidget;
import com.jancar.widget.utils.FlyLog;

import java.util.Set;

public class MediaWidgetService extends Service {
    private JacMediaController controller;
    private static final int SHOW_MUSIC = 0;
    private static final int SHOW_FM = 1;
    private int mSession = SHOW_MUSIC;
    private String mTitle = "";
    private long mCurrent;
    private long mDuration;
    private Bitmap mBitmap;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.d("onCreate");
        controller = new JacMediaController(getApplicationContext()) {
            @Override
            public void onSession(String page) {
                FlyLog.d("JacMediaController onSession=%s", page);
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.media_widget);
                switch (page) {
                    case "music":
                    case "a2dp":
                        mSession = SHOW_MUSIC;
                        break;
                    case "fm":
                        mSession = SHOW_FM;
                        break;
                    default:
                        mSession = SHOW_MUSIC;
                        break;

                }
                upWidgetView();
            }

            @Override
            public void onPlayUri(String uri) {
                FlyLog.d("JacMediaController onPlayUri=%s", uri);
            }

            @Override
            public void onPlayId(int currentId, int total) {
                FlyLog.d("JacMediaController onPlayId currentId=%d,total=%d", currentId, total);
            }

            @Override
            public void onPlayState(int state) {
                FlyLog.d("JacMediaController onPlayState=%s", state);
            }

            @Override
            public void onProgress(long current, long duration) {
                FlyLog.d("current=%d,duration=%d", current, duration);
                mCurrent = current;
                mDuration = duration;
                upWidgetView();
            }

            @Override
            public void onRepeat(int repeat) {
                FlyLog.d("JacMediaController repeat=%d", repeat);
            }

            @Override
            public void onFavor(boolean bFavor) {
                FlyLog.d("JacMediaController bFavor=" + bFavor);
            }

            @Override
            public void onID3(String title, String artist, String album, byte[] artWork) {
                FlyLog.d("onID3 title=%s,artist=%s,album=%s", title, artist, album);
                mTitle = title;
                if (artWork == null) {
                    mBitmap = null;
                } else {
                    mBitmap = BitmapFactory.decodeByteArray(artWork, 0, artWork.length);
                }
                upWidgetView();
            }

            @Override
            public void onMediaEvent(String action, Bundle extras) {
                try {
                    Set<String> keys = extras.keySet();
                    StringBuilder builder = new StringBuilder();
                    for (String key : keys) {
                        builder.append(key).append(":").append(extras.get(key));
                    }
//                    tvCustom.setText(builder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        controller.Connect();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FlyLog.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.DisConnect();
    }

    private void upWidgetView() {
        FlyLog.e("upWidgetView");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.media_widget);
        //更新是MUSIC -FM
        switch (mSession) {
            case SHOW_FM:
                remoteViews.setViewVisibility(R.id.wg_music, View.GONE);
                remoteViews.setViewVisibility(R.id.wg_radio, View.VISIBLE);
                break;
            case SHOW_MUSIC:
                remoteViews.setViewVisibility(R.id.wg_music, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.wg_radio, View.GONE);
                //更新标题
                remoteViews.setTextViewText(R.id.media_music_title, mTitle);
                //更新时间
                String str1 = generateTime(mCurrent);
                String str2 = generateTime(mDuration);
                remoteViews.setProgressBar(R.id.media_music_progressbar, (int) (mDuration / 1000), (int) (mCurrent / 1000), false);
                remoteViews.setTextViewText(R.id.media_music_starttime, str1);
                remoteViews.setTextViewText(R.id.media_music_endtime, str2);

                //更新图片
                if (mBitmap == null) {
                    remoteViews.setImageViewResource(R.id.media_id3img, R.drawable.media_music);
                } else {
                    remoteViews.setImageViewBitmap(R.id.media_id3img, mBitmap);
                }

                //设置点击事件
                break;
        }


        ComponentName componentName = new ComponentName(getApplicationContext(), MediaWidget.class);
        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(componentName, remoteViews);
        FlyLog.e("upWidgetView--end remouteView=" + remoteViews);
    }


    private String generateTime(long time) {
        time = Math.min(Math.max(time, 0), 359999000);
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
}
