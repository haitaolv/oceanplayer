/*
 * class: MusicService
 * Description: provide the function to control the playing of audio
 * Author: Frank
 * Date: 2020/3/1
 */

package com.mega.oceanplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {

    private final String TAG = MusicService.class.getSimpleName();
    public  final int NOTIFICATION_ID = android.os.Process.myPid();

    String notificationId = "notification_id_ocean_player";
    String notificationName = "notification_name_ocean_player";

    private MediaPlayer player;
    private AudioFile audioFile;
    private String audioFileName;
    private boolean audioFileLoaded;
    private boolean isPlaying;

    private ServiceBinder mServiceBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(TAG, "onBind");
        return mServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "onCreate");

        mServiceBinder = new ServiceBinder();

        audioFileLoaded = false;
        isPlaying = false;
        audioFile = null;
        audioFileName = "";

        player = new MediaPlayer();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LogUtil.d(TAG,"GET media player Prepared event: duration=" + mp.getDuration());
                audioFileLoaded = true;
                if(isPlaying) {
                    mp.start();
                }
            }
        });

        startForeground(NOTIFICATION_ID, getNotificationForFGJob());
        //setForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtil.i(TAG, "onDestroy");
        player.stop();
        player.release();
        player = null;
        super.onDestroy();
    }

    public boolean isLoaded() {
        return audioFileLoaded;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public String getAudioFileName() {
        return this.audioFileName;
    }

    public void load(AudioFile audioFile) {
        if(audioFile == null) {
            LogUtil.e(TAG, "Given audioFile is null");
            return;
        }

        if(this.audioFile == audioFile) {
            LogUtil.e(TAG, "Current audioFile is already loaded: " + audioFile.getFullPath());
            return;
        }

        if(this.audioFile != null ) {
            this.audioFile.setIsPlaying(false);
            this.audioFile.setIsLoaded(false);
        }

        if(player == null) {
            LogUtil.e(TAG, "player is not created");
            return;
        }

        File f = new File(audioFile.getFullPath());
        if(!f.exists()) {
            LogUtil.e(TAG, "audio file doesn't exist: " + audioFile.getFullPath());
            return;
        }

        LogUtil.d(TAG, "Loading file from disk: " + audioFile.getFullPath());

        this.audioFileLoaded = false;
        this.isPlaying = false;
        this.audioFile = audioFile;
        this.audioFileName = this.audioFile.getAudioName();

        try {
            player.reset();
            player.setDataSource(audioFile.getFullPath());
            player.prepare();
            audioFileLoaded = true;
            this.audioFile.setIsLoaded(true);
            LogUtil.d(TAG, "Loading file successfully: " + audioFile.getFullPath() + ", duration=" + getDuration());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        notifyStatusChange();
    }

    //播放音乐
    public void play() {
        if(audioFileLoaded && player != null) {
            player.start();
            isPlaying = true;
            notifyStatusChange();
        }
    }

    //暂停播放音乐
    public void pausePlay() {
        if(audioFileLoaded && player != null) {
            player.pause();
            isPlaying = false;
            notifyStatusChange();
        }
    }

    //继续播放音乐
    public void continuePlay() {
        if(audioFileLoaded && player != null) {
            player.start();
            isPlaying = true;
            notifyStatusChange();
        }
    }

    //设置音乐的播放位置
    public void seekTo(int progress) {
        if (audioFileLoaded && player != null) {
            player.seekTo(progress);
        }
    }

    public Integer getDuration() {
        if(audioFileLoaded && player != null) {
            return player.getDuration();
        }
        return 0;
    }

    public Integer getPlayedTime() {
        if(audioFileLoaded && player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void notifyStatusChange() {
        sendNotification();
        audioFile.setIsPlaying(this.isPlaying);
        Bundle data = new Bundle();
        data.putString("action", isPlaying? "play":"pause");
        for(Handler h: MusicController.getInstance().getSubscriptionList()) {
            Message msg = h.obtainMessage();
            msg.setData(data);
            h.sendMessage(msg);
        }
    }

    public void setNotificationChannel(NotificationManager notificationManager) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification getNotificationForFGJob()
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel(notificationManager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "");
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setSmallIcon(R.mipmap.icon_128);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        return builder.build();
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener onErrorListener) {
        this.player.setOnErrorListener(onErrorListener);
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        this.player.setOnCompletionListener(onCompletionListener);
    }

    public void sendNotification() {
        LogUtil.d(TAG, "send user customized notification");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setNotificationChannel(notificationManager);

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.setAction("com.mega.oceanplayer.MusicController.PlayingActivity");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        android.widget.RemoteViews remoteView = new android.widget.RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteView.setTextViewText(R.id.txtAudioFileName, audioFileName);
        remoteView.setImageViewResource(R.id.imgPlayPause, isPlaying? R.drawable.ic_btn_pause : R.drawable.ic_btn_play);
        //remoteView.setProgressBar(R.id.progressBar, 100, 10, false);

        Intent intent1 = new Intent(this, MyBroadcastReceiver.class);
        intent1.setAction("com.mega.oceanplayer.MusicController.playPrevious");
        remoteView.setOnClickPendingIntent(R.id.imgPlayPrevious,
                PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent intent2 = new Intent(this, MyBroadcastReceiver.class);
        intent2.setAction("com.mega.oceanplayer.MusicController.playPause");
        remoteView.setOnClickPendingIntent(R.id.imgPlayPause,
                PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent intent3 = new Intent(this, MyBroadcastReceiver.class);
        intent3.setAction("com.mega.oceanplayer.MusicController.playNext");
        remoteView.setOnClickPendingIntent(R.id.imgPlayNext,
                PendingIntent.getBroadcast(this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "");
        builder.setSmallIcon(R.mipmap.icon_128);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setContent(remoteView);
        builder.setContentIntent(pendingIntent);
        //通知图标不会显示在状态栏里
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }

        Notification notification = builder.build();
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    class ServiceBinder extends Binder {
        MusicService getMusicService() {
            return MusicService.this;
        }
    }
}
