package com.mega.oceanplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicController {

    private final String TAG = MusicController.class.getSimpleName();

    private static MusicController musicController = null;

    private MusicService musicService;
    private PlayList playList;

    private List<Handler> subscription_list;

    private MusicController() {
        musicService = null;
        playList = null;
        subscription_list = new ArrayList<>();
    }

    static MusicController getInstance() {
        if(musicController== null)  musicController = new MusicController();
        return musicController;
    }

    void subscribeHandler(Handler handler) {
        LogUtil.d(TAG, "subscribeHandler: " + handler.toString());
        if(!subscription_list.contains(handler)) subscription_list.add(handler);
        LogUtil.d(TAG, "subscription list: " + subscription_list.size());
    }

    void unsubscribeHandler(Handler handler) {
        LogUtil.d(TAG, "unsubscribeHandler: " + handler.toString());
        subscription_list.remove(handler);
        LogUtil.d(TAG, "subscription list: " + subscription_list.size());
    }

    List<Handler> getSubscriptionList() {
        return this.subscription_list;
    }

    void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    void playNext() {
        int next_index = PlayListManager.getPlayMode()<2? playList.getActiveIndex() + 1: (new Random()).nextInt(playList.getFileCount());
        playList.setActiveIndex(next_index);
        playList.setPlayedTime(0);
        load(playList.getActiveAudioFile());
        play();
    }

    void playPrevious() {
        int next_index = PlayListManager.getPlayMode()<2? playList.getActiveIndex() - 1: (new Random()).nextInt(playList.getFileCount());
        playList.setActiveIndex(next_index);
        playList.setPlayedTime(0);
        load(playList.getActiveAudioFile());
        play();
    }

    private void repeatCurrent() {
        playList.setPlayedTime(0);
        seekTo(0);
        play();
    }

    void loadFileOnly() {
        load(playList.getActiveAudioFile());
        seekTo(playList.getPlayedTime());
    }

    void loadAndPlay() {
        load(playList.getActiveAudioFile());
        seekTo(playList.getPlayedTime());
        play();
    }

    void setMusicService(MusicService musicService) {
        this.musicService = musicService;
        this.musicService.setOnCompletionListener(getOnCompletionListener());
        this.musicService.setOnErrorListener(getOnErrorListener());
    }

    String getAudioFileName(Context context) {
        return (musicService == null || musicService.getAudioFileName().equals(""))? context.getString(R.string.no_audio_file_loaded): musicService.getAudioFileName();
    }

    boolean isLoaded() {
        return (musicService != null) && musicService.isLoaded();
    }

    public boolean isPlaying() {
        return (musicService != null) && musicService.isPlaying();
    }

    Integer getDuration() {
        return (musicService == null)? 0: musicService.getDuration();
    }

    Integer getPlayedTime() {
        return (musicService == null)? 0: musicService.getPlayedTime();
    }

    private void load(AudioFile audioFile) {
        if(musicService != null) musicService.load(audioFile);
    }

    void play() {
        if(musicService != null) musicService.play();
    }

    void pausePlay() {
        if(musicService != null) musicService.pausePlay();
    }

    void continuePlay() {
        if(musicService != null) musicService.continuePlay();
    }

    void seekTo(int progress) {
        if(musicService != null)  musicService.seekTo(progress);
    }

    private MediaPlayer.OnErrorListener getOnErrorListener() {
        return new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogUtil.e(TAG, "MyServiceConn: OnError - Error text: " + LogUtil.getMusicServerErrorTxtByCode(what) + " Extra Text: " + LogUtil.getMusicServerExtraTextByCode(extra));
                    musicController.playNext();
                    return false;
                }
            };
    }

    private MediaPlayer.OnCompletionListener getOnCompletionListener() {
        return new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    LogUtil.i(TAG, "MyServiceConn: get MediaPlayer completion event");
                    if (PlayListManager.getPlayMode() == PlayListManager.PLAY_MODE_REPEAT_ONE) {
                        musicController.repeatCurrent();
                    } else {
                        musicController.playNext();
                    }
                }
            };
    }
}
