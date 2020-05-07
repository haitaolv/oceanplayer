package com.mega.oceanplayer;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyApplication extends Application {
    private final String TAG = MyApplication.class.getSimpleName();

    public MusicServiceConn musicServiceConn = null;
    private Intent musicServiceIntent;

    public static Activity topActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.init();
        LogUtil.i(TAG, "onCreate");

        languageWork();
        initPlayListManager();
        startMusicService();
        startSavePlayInfoTimer();

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                topActivity = activity;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                topActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    @Override
    public void onTerminate() {
        LogUtil.w(TAG, "onDestroy");
        LogUtil.close();
        PlayListManager.savePlayingInfoToFile();
        terminateMusicService();
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        LogUtil.i(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        languageWork();
    }

    public void languageWork() {
        LogUtil.w(TAG, "languageWork");
        Locale locale = LanguageUtil.readSavedLocale(this);
        LanguageUtil.updateLocale(this, locale);
        LogUtil.w(TAG, "languageWork done");
    }

    public void initPlayListManager() {
        LogUtil.d(TAG,"init play list manager");
        PlayListManager.setContext(this.getApplicationContext());
        PlayListManager.setAppDataDir(getFilesDir());
        PlayListManager.readSavedPlayMode();
        PlayListManager.loadPlayListFromFile();
        PlayListManager.loadPlayingInfoFromFile();
    }

    public void startSavePlayInfoTimer() {
        LogUtil.d(TAG,"Start save playing info timer");
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                MusicController musicController = MusicController.getInstance();
                if(musicController.isPlaying()) {
                    PlayListManager.getActivePlayList().setPlayedTime(musicController.getPlayedTime());
                }
                PlayListManager.savePlayingInfoToFile();
            }
        };
        timer.schedule(timerTask, 5000, 5000);
    }

    // Start the music service
    public void startMusicService() {
        LogUtil.i(TAG, "bindMusicService");
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        getApplicationContext().startService(musicServiceIntent);
        //startForegroundService(serviceIntent);
        musicServiceConn = new MusicServiceConn();
        getApplicationContext().bindService(musicServiceIntent, musicServiceConn, BIND_AUTO_CREATE);
    }

    public void terminateMusicService() {
        //解绑服务
        getApplicationContext().unbindService(musicServiceConn);
        //停止服务
        getApplicationContext().stopService(musicServiceIntent);
    }

    class MusicServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            LogUtil.i(TAG,"MyServiceConn: onServiceConnected");
            MusicController musicController = MusicController.getInstance();
            musicController.setMusicService(((MusicService.ServiceBinder)iBinder).getMusicService());
            musicController.setPlayList(PlayListManager.getActivePlayList());
            musicController.loadFileOnly();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i(TAG,"MyServiceConn: onServiceDisconnected");
        }
    }
}
