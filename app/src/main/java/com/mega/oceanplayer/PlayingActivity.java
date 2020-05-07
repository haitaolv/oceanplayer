package com.mega.oceanplayer;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingActivity extends MyBaseActivity {

    protected String TAG = PlayingActivity.class.getSimpleName();

    private SeekBar sb;
    private Timer progressUpdateTimer = null;
    private ObjectAnimator imageRotateAnimator = null;
    private MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideInAnimation();
        setContentView(R.layout.activity_playing);
        LogUtil.i(TAG, "onCreate() " + this.toString());

        musicController.subscribeHandler(handler);

        if(!musicController.isLoaded()) {
            musicController.loadFileOnly();
        }
        sb = findViewById(R.id.seekBar);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogUtil.d(TAG, "OnSeekBarChangeListener: onStopTrackingTouch");
                int progress = seekBar.getProgress();
                musicController.seekTo(progress);
                PlayListManager.getActivePlayList().setPlayedTime(progress);
                TextView txtPlayedTime = findViewById(R.id.txtPlayedTime);
                txtPlayedTime.setText(formatTime(sb.getProgress()));
            }
        });

        LinearLayout layoutBottom = findViewById(R.id.playListLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutBottom.getLayoutParams();
        params.height = getScreenHeight();
        layoutBottom.setLayoutParams(params);

        initPlayListView();

        startProgressUpdateTimer();
        createImageRotateAnimator();

        updateAudioInfo();
        updatePlayingProgress();
        updatePausePlayBackground();
        updatePlayModeImage();
        updateFavoriteIcon();
        rotateImage();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            LogUtil.d(TAG, "gets back key event");
            LinearLayout layout = findViewById(R.id.playListLayout);
            if(layout.getTranslationY() != 0) {
                hidePlayList(null);
            }
            else {
                finishAfterTransition();
            }
        }
        return super.onKeyDown(keycode, event);
    }

    @Override
    public void onStart() {
        LogUtil.i(TAG, "onStart");
        super.onStart();
    }


    @Override
    public void onResume() {
        LogUtil.i(TAG, "onResume");
        super.onResume();
        updatePlayingProgress();
        updatePausePlayBackground();
        rotateImage();
    }


    @Override
    public void onPause() {
        LogUtil.i(TAG, "onPause");
        super.onPause();
    }


    @Override
    public void onStop() {
        LogUtil.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtil.i(TAG, "onDestroyed");
        stopProgressUpdateTimer();
        super.onDestroy();
    }

    public void initPlayListView() {
        final PlayList activePlayList = PlayListManager.getActivePlayList();

        ListView lv = findViewById(R.id.listViewPlayList);

        final AudioFileAdaptor adaptor = new AudioFileAdaptor(this, R.layout.view_item_audio_file, activePlayList.getAudioFileList());
        adaptor.setTextDefaultColor(R.color.colorWhite);
        adaptor.setDefaultImage(R.drawable.ic_play_white_24dp);
        adaptor.setOnPlayButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "list view item play button clicked");
                int position = (Integer)v.getTag();
                playSelectedAudio(position);
            }
        });

        lv.setAdapter(adaptor);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.d(TAG, "list view item clicked");
                playSelectedAudio(position);
            }
        });

        lv.setSelection(activePlayList.getActiveIndex());
    }

    public void playSelectedAudio(int position) {
        LogUtil.d(TAG, "call playSelectedAudio()");

        final PlayList activePlayList = PlayListManager.getActivePlayList();

        ListView lv = findViewById(R.id.listViewPlayList);
        //lv.setSelection(position);
        AudioFileAdaptor adaptor = (AudioFileAdaptor)lv.getAdapter();

        if(activePlayList.getActiveIndex() != position) {
            LogUtil.d(TAG, "call playSelectedAudio(): playIndex changed: " + position);
            activePlayList.setActiveIndex(position);
            activePlayList.setPlayedTime(0);
            musicController.loadAndPlay();
            adaptor.notifyDataSetChanged();
            updatePausePlayBackground();
        }
        else if(!activePlayList.getActiveAudioFile().isPlaying()){
            LogUtil.d(TAG, "call playSelectedAudio(): playIndex not changed, but current one not playing()");
            musicController.continuePlay();
            adaptor.notifyDataSetChanged();
            updatePausePlayBackground();
        }
        //LogUtil.d(TAG, "scroll the list view to active index: " + activePlayList.getActiveIndex() + ", offset=" + lv.getHeight()/2);
        lv.smoothScrollToPositionFromTop(activePlayList.getActiveIndex(), lv.getHeight()/2);
    }

    public void setSlideInAnimation() {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(400);
        slide.excludeTarget(android.R.id.navigationBarBackground, true);	// 	排除导航栏
        slide.excludeTarget(android.R.id.statusBarBackground,true);
        getWindow().setEnterTransition(slide);
        getWindow().setReturnTransition(slide);
    }

    public void updateFavoriteIcon() {
        ImageView imageView = findViewById(R.id.imgFavorite);
        PlayList playList = PlayListManager.getActivePlayList();
        AudioFile af = playList.getActiveAudioFile();
        if(PlayListManager.isFavoriteAudioFile(af)) {
            imageView.setImageResource(R.drawable.ic_star_yellow_24dp);
        }
        else {
            imageView.setImageResource(R.drawable.ic_star_border_gray_24dp);
        }
    }

    public void updateAudioInfo() {
        TextView txtPlayListName = findViewById(R.id.txtPlayListName);
        TextView txtAudioFileName = findViewById(R.id.txtAudioFileName);

        txtPlayListName.setText(PlayListManager.getActivePlayList().getName());
        txtAudioFileName.setText(musicController.getAudioFileName(this));
    }

    public void updatePausePlayBackground() {
        ImageView imgPlayPause = findViewById(R.id.imgPlayPause);
        if (musicController.isPlaying()) {
            imgPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_pause, null));
        } else {
            imgPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_play, null));
        }
    }

    public void updatePlayingProgress() {
        //设置滑块的进度
        sb.setMax(musicController.getDuration());
        sb.setProgress(musicController.getPlayedTime());

        //更新时间进度显示
        TextView txtPlayedTime = findViewById(R.id.txtPlayedTime);
        txtPlayedTime.setText(formatTime(musicController.getPlayedTime()));

        TextView txtTotalTime = findViewById(R.id.txtTotalTime);
        txtTotalTime.setText(formatTime(musicController.getDuration()));
    }

    public void updatePlayModeImage() {
        ImageView imgView = findViewById(R.id.imgPlayMode);
        switch(PlayListManager.getPlayMode()) {
            case PlayListManager.PLAY_MODE_REPEAT:
                imgView.setImageResource(R.drawable.ic_play_repeat_24dp);
                break;
            case PlayListManager.PLAY_MODE_REPEAT_ONE:
                imgView.setImageResource(R.drawable.ic_play_repeat_one_24dp);
                break;
            case PlayListManager.PLAY_MODE_SHUFFLE:
                imgView.setImageResource(R.drawable.ic_play_shuffle_24dp);
                break;
        }
    }

    public void startProgressUpdateTimer() {
        LogUtil.d(TAG, "Start play progress update timer");
        final Handler h = new Handler();
        if (progressUpdateTimer == null) {
            progressUpdateTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(musicController.isPlaying()) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                updatePlayingProgress();
                            }
                        });
                    }
                }
            };
            progressUpdateTimer.schedule(timerTask, 0, 1000);
        }
    }

    public void stopProgressUpdateTimer() {
        LogUtil.d(TAG, "Playing Activity: Stop play progress update timer");
        if (progressUpdateTimer != null) {
            try {
                progressUpdateTimer.cancel();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            progressUpdateTimer = null;
        }
    }

    public void finishSelf(View v) {
        LogUtil.i(TAG, "imgHide: onClick");
        LinearLayout layout = findViewById(R.id.playListLayout);
        if(layout.getTranslationY() != 0) {
            hidePlayList(null);
        }
        else {
            finishAfterTransition();
        }
    }

    public void btnPlayPreviousClick(View v) {
        LogUtil.d(TAG, "btnPlayPreviousClick event");
        musicController.playPrevious();
        updateAudioInfo();
        updateFavoriteIcon();
        updatePausePlayBackground();
        updatePlayingProgress();
        rotateImage();
    }

    public void btnPlayNextClick(View v) {
        LogUtil.d(TAG, "btnPlayNextClick event");
        musicController.playNext();
        updateAudioInfo();
        updateFavoriteIcon();
        updatePausePlayBackground();
        updatePlayingProgress();
        rotateImage();
    }

    public void btnPauseAndPlayClick(View v) {
        LogUtil.d(TAG, "btnPauseAndPlayClick event");
        if (musicController.isPlaying()) {
            musicController.pausePlay();
        }
        else {
            musicController.continuePlay();
        }
        updatePausePlayBackground();
        rotateImage();
    }

    public void btnSetPlayMode(View view) {
        PlayListManager.setPlayMode();
        updatePlayModeImage();
    }

    public void showPlayList(View view) {

        PlayList activePlayList = PlayListManager.getActivePlayList();

        ListView lv = findViewById(R.id.listViewPlayList);

        //滚动列表，将当前播放的条目滚动到列表的中间位置
        //lv.setSelection(activePlayList.getActiveIndex());
        lv.smoothScrollToPositionFromTop(activePlayList.getActiveIndex(), lv.getHeight()/2);

        AudioFileAdaptor adaptor = (AudioFileAdaptor) lv.getAdapter();
        adaptor.notifyDataSetChanged();

        LinearLayout layout = findViewById(R.id.playListLayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();

        int offset = params.height;
        long duration = 500;
        ObjectAnimator animation = ObjectAnimator.ofFloat(layout, "translationY", -offset);
        animation.setDuration(duration);
        animation.start();
    }

    public void hidePlayList(View view) {
        LinearLayout layout = findViewById(R.id.playListLayout);
        long duration = 500;
        ObjectAnimator animation = ObjectAnimator.ofFloat(layout, "translationY", 0);
        animation.setDuration(duration);
        animation.start();
    }

    private void updatePlayList() {
        LinearLayout layout = findViewById(R.id.playListLayout);
        float offset = layout.getTranslationY();
        if(offset < 0) {
            PlayList activePlayList = PlayListManager.getActivePlayList();
            ListView lv = findViewById(R.id.listViewPlayList);
            lv.smoothScrollToPositionFromTop(activePlayList.getActiveIndex(), lv.getHeight()/2);
            AudioFileAdaptor adaptor = (AudioFileAdaptor) lv.getAdapter();
            adaptor.notifyDataSetChanged();
        }
    }

    public void rotateImage() {
        if(musicController.isPlaying())
            imageRotateAnimator.resume();
        else
            imageRotateAnimator.pause();
    }

    public void createImageRotateAnimator() {
        LogUtil.d(TAG, "startImageRotation");
        imageRotateAnimator = ObjectAnimator.ofFloat(findViewById(R.id.imgCD), "rotation", 0.0f, 359.9f);
        imageRotateAnimator.setDuration(20000);
        imageRotateAnimator.setRepeatCount(Animation.INFINITE);//设定无限循环
        imageRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);// 循环模式
        imageRotateAnimator.setInterpolator(new LinearInterpolator());// 匀速
        imageRotateAnimator.start();
        imageRotateAnimator.pause();
    }

    @SuppressLint("DefaultLocale")
    public String formatTime(Integer t) {
        int minute = t / 1000 / 60;
        int second = t / 1000 % 60;
        return String.format("%02d:%02d", minute, second);
    }

    static class MyHandler extends Handler {

        private static String TAG = MyHandler.class.getSimpleName();
        private WeakReference<MyBaseActivity> activity;

        MyHandler(MyBaseActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle data = msg.getData();
            LogUtil.d(TAG, "receive message from music service: action=" + data.getString("action"));

            PlayingActivity activity1 = (PlayingActivity) activity.get();
            if(activity1 != null) {
                activity1.updateAudioInfo();
                activity1.updatePlayingProgress();
                activity1.rotateImage();
                activity1.updatePausePlayBackground();
                activity1.updatePlayList();
            }
        }
    }

    public void addToMyFavorite(View v) {
        PlayList activePlayList = PlayListManager.getActivePlayList();
        AudioFile activeAudioFile = activePlayList.getActiveAudioFile();
        if(PlayListManager.isFavoriteAudioFile(activeAudioFile)) {
            PlayListManager.removeFromFavorite(activeAudioFile);
        }
        else {
            PlayListManager.addToFavorite(activeAudioFile);
            Toast.makeText(this, getString(R.string.msg_added_to_favorite), Toast.LENGTH_SHORT).show();
        }
        updateFavoriteIcon();
    }
}
