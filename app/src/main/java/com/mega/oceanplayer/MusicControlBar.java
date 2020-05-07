package com.mega.oceanplayer;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MusicControlBar extends LinearLayout implements View.OnTouchListener  {

    private String TAG = MusicControlBar.class.getSimpleName();

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;

    private GestureDetector mGestureDetector;
    private ObjectAnimator imageRotateAnimator = null;

    private MusicController musicController = MusicController.getInstance();

    public MusicControlBar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_control_bar, this);
        onCreate();
    }

    public void onCreate() {
        LogUtil.i(TAG, ": construct function");

        ImageView imgCD = findViewById(R.id.imgCD);

        enableFlingActionOnAudioFileName();

        createImageRotateAnimate();

        findViewById(R.id.imgCD).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayingActivity(v);
            }
        });

        findViewById(R.id.txtAudioFileName).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayingActivity(v);
            }
        });

        findViewById(R.id.btnPausePlay).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPausePlayClick(v);
            }
        });
    }

    public MusicControlBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R.layout.layout_control_bar, this);
        LogUtil.i(TAG, ": construct function2");
        onCreate();
    }

    public MusicControlBar(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        LayoutInflater.from(context).inflate(R.layout.layout_control_bar, this);
        LogUtil.d(TAG, ": construct function3");
        onCreate();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void enableFlingActionOnAudioFileName() {
        mGestureDetector = new GestureDetector(getContext(), myGestureListener);
        TextView txtAudioFileName = findViewById(R.id.txtAudioFileName);
        txtAudioFileName.setOnTouchListener(this);
        txtAudioFileName.setLongClickable(true);
    }

    public void btnPausePlayClick(View v) {
        LogUtil.d(TAG,"btnPausePlay clicked");
        if(musicController.isPlaying()) {
            musicController.pausePlay();
        }
        else {
            musicController.play();
        }
        changePausePlayIcon();
    }

    public void startPlayingActivity(View v) {
        LogUtil.d(TAG,"txtAudioFileName clicked");
        Intent intent= new Intent(getContext(), PlayingActivity.class);
        getContext().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) getContext()).toBundle());
    }

    public void updateAudioFileName() {
        TextView txtAudioFileName = findViewById(R.id.txtAudioFileName);
        txtAudioFileName.setText(musicController.getAudioFileName(getContext()));
    }

    public void changePausePlayIcon() {
        ImageView btnPausePlay = findViewById(R.id.btnPausePlay);
        if (musicController.isPlaying()) {
            btnPausePlay.setImageResource(R.drawable.ic_btn_pause);
        } else {
            btnPausePlay.setImageResource(R.drawable.ic_btn_play);
        }
    }

    public void createImageRotateAnimate() {
        if(imageRotateAnimator == null) {
            imageRotateAnimator = ObjectAnimator.ofFloat(findViewById(R.id.imgCD), "rotation", 0.0f, 359.9f);
            imageRotateAnimator.setDuration(20000);
            imageRotateAnimator.setRepeatCount(Animation.INFINITE);//设定无限循环
            imageRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);// 循环模式
            imageRotateAnimator.setInterpolator(new LinearInterpolator());// 匀速
            imageRotateAnimator.start();
            imageRotateAnimator.pause();
        }
    }

    public void rotateImage(boolean isPlaying) {
        if(isPlaying) {
            imageRotateAnimator.resume();
        }
        else {
            imageRotateAnimator.pause();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LogUtil.d(TAG, "开始滑动");
            float x = e1.getX()-e2.getX();
            float x2 = e2.getX()-e1.getX();
            if(x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY){
                musicController.playNext();
            }
            else if(x2 > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY){
                musicController.playPrevious();
            }
            else {
                return false;
            }
            return true;
        }
    };
}
