package com.jancar.widget.sevice;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.android.launcher3.R;
import com.jancar.JancarManager;
import com.jancar.media.JacMediaController;
import com.jancar.source.Page;
import com.jancar.widget.MediaWidget;
import com.jancar.widget.utils.FlyLog;

import java.util.Locale;

public class MediaWidgetService extends Service {
    private JacMediaController controller;
    private String mSession = Page.PAGE_FM;
    private String mLastSession = Page.PAGE_FM;
    private String mTitle = "";
    private long mCurrent;
    private long mDuration;
    private Bitmap mBitmap;
    private int playstate = 0;

    private String fmText = "FM1";
    private String fmName = "87.5";
    private String fmKz = "MHz";

    private static final String ACTION_NEXT = "intent.action.widget.ACTION_NEXT";
    private static final String ACTION_PREV = "intent.action.widget.ACTION_PREV";
    private static final String ACTION_PLAYPAUSE = "intent.action.widget.ACTION_PLAYPAUSE";
    private static final String ACTION_OPENAPP = "intent.action.widget.ACTION_OPENAPP";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.d("onCreate");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);
        intentFilter.addAction(ACTION_PLAYPAUSE);
        intentFilter.addAction(ACTION_OPENAPP);
        registerReceiver(mReceiver, intentFilter);
        controller = new JacMediaController(getApplicationContext()) {
            @Override
            public void onSession(String page) {
                FlyLog.d("onSession page=%s", page);
                mSession = page;
                if (mSession.equals(Page.PAGE_MUSIC) || mSession.equals(Page.PAGE_MUSIC) || mSession.equals(Page.PAGE_MUSIC)) {
                    mLastSession = page;
                    upWidgetView();
                }
            }

            @Override
            public void onPlayUri(String uri) {
                FlyLog.d("onPlayUri=%s", uri);
            }

            @Override
            public void onPlayId(int currentId, int total) {
                FlyLog.d("onPlayId currentId=%d,total=%d", currentId, total);
            }

            @Override
            public void onPlayState(int state) {
                FlyLog.d("onPlayState=%s", state);
                if (playstate != state) {
                    playstate = state;
                    upWidgetView();
                }
            }

            @Override
            public void onProgress(long current, long duration) {
//                FlyLog.d("onProgress current=%d,duration=%d", current, duration);
                mCurrent = current;
                mDuration = duration;
                upWidgetView();
            }

            @Override
            public void onRepeat(int repeat) {
                FlyLog.d("onRepeat repeat=%d", repeat);
            }

            @Override
            public void onFavor(boolean bFavor) {
                FlyLog.d("onFavor bFavor=" + bFavor);
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
                FlyLog.d("onMediaEvent action=%s,extras=" + extras, action);
                try {
                    int fmType = extras.getInt("Band");
                    fmText = fmType == 0 ? "FM1" : fmType == 1 ? "FM2" : fmType == 2 ? "FM3" : fmType == 3 ? "AM1" : "AM2";
                    fmKz = fmType < 3 ? "MHz" : "KHz";
                    fmName = extras.getString("name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                upWidgetView();
            }
        };
        controller.Connect();
    }

    private void playNext() {
        try {
            controller.requestNext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playPrev() {
        try {
            controller.requestPrev();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playPasue() {
        try {
            controller.requestPPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        unregisterReceiver(mReceiver);
    }

    private void upWidgetView() {
//        FlyLog.d("upWidgetView");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.wg_media_widget);
        //更新是MUSIC -FM
        switch (mSession) {
            case Page.PAGE_FM:
            default:
                remoteViews.setViewVisibility(R.id.wg_music, View.GONE);
                remoteViews.setViewVisibility(R.id.wg_radio, View.VISIBLE);
                remoteViews.setTextViewText(R.id.wg_fm_tv01, fmText);
                remoteViews.setTextViewText(R.id.wg_fm_tv02, fmName);
                remoteViews.setTextViewText(R.id.wg_fm_tv03, fmKz);
                remoteViews.setOnClickPendingIntent(R.id.wg_fm_next,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NEXT), 0));
                remoteViews.setOnClickPendingIntent(R.id.wg_fm_prev,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PREV), 0));
                remoteViews.setOnClickPendingIntent(R.id.wg_radio_img,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPENAPP), 0));
                remoteViews.setOnClickPendingIntent(R.id.wg_fm_tv02,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPENAPP), 0));
                FlyLog.d("upWidgetView fm");
                break;
            case Page.PAGE_MUSIC:
            case Page.PAGE_A2DP:
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
                remoteViews.setImageViewResource(R.id.wg_music_play_img, playstate == 1 ? R.drawable.media_pause : R.drawable.media_play);

                //更新图片
                if (mBitmap == null) {
                    remoteViews.setImageViewResource(R.id.media_id3img,
                            Page.PAGE_MUSIC.endsWith(mSession) ? R.drawable.mediainfo_music_default : R.drawable.mediainfo_bt_default);
                } else {
                    remoteViews.setImageViewBitmap(R.id.media_id3img, mBitmap);
                }

                //设置点击事件
                remoteViews.setOnClickPendingIntent(R.id.wg_music_next,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NEXT), 0));
                remoteViews.setOnClickPendingIntent(R.id.wg_music_play,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PLAYPAUSE), 0));
                remoteViews.setOnClickPendingIntent(R.id.wg_music_prev,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PREV), 0));
                remoteViews.setOnClickPendingIntent(R.id.media_id3img,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPENAPP), 0));
                remoteViews.setOnClickPendingIntent(R.id.media_music_title,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPENAPP), 0));
                remoteViews.setOnClickPendingIntent(R.id.media_music_progressbar,
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPENAPP), 0));
                FlyLog.d("upWidgetView music");
                break;
        }


        ComponentName componentName = new ComponentName(getApplicationContext(), MediaWidget.class);
        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(componentName, remoteViews);
//        FlyLog.d("upWidgetView--end remouteView=" + remoteViews);
    }


    private String generateTime(long time) {
        time = Math.min(Math.max(time, 0), 359999000);
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FlyLog.d("onReceive intent=" + intent);
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_NEXT:
                        if (!isJancarSession()) {
                            startAppByLastSession();
                        } else {
                            playNext();
                        }
                        break;
                    case ACTION_PREV:
                        if (!isJancarSession()) {
                            startAppByLastSession();
                        } else {
                            playPrev();
                        }
                        break;
                    case ACTION_PLAYPAUSE:
                        if (!isJancarSession()) {
                            startAppByLastSession();
                        } else {
                            playPasue();
                        }
                        break;
                    case ACTION_OPENAPP:
                        startAppByLastSession();
                        break;
                }
            }
        }
    };

    private boolean isJancarSession() {
        return Page.PAGE_FM.equals(mSession) || Page.PAGE_MUSIC.equals(mSession) || Page.PAGE_A2DP.equals(mSession);
    }

    @SuppressLint("WrongConstant")
    private void startAppByLastSession() {
        FlyLog.d("start app mSession=" + mLastSession);
        ((JancarManager) getSystemService(JancarManager.JAC_SERVICE)).requestPage(mLastSession);
    }
}
