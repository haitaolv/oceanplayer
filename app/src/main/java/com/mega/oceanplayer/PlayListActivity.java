/*
 * class: PlayListActitity
 * Description:  UI for user to edit the playlist, e.g. add and delete audio file to the play list
 * Author: Frank
 * Date: 2020/3/1
 */


package com.mega.oceanplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class PlayListActivity extends MyBaseActivity {

    private String TAG = PlayListActivity.class.getSimpleName();

    private PlayList playList;
    private MusicControlBar controlBar;
    public MyHandler handler = new MyHandler(this);

    private SideSlippingListView lv ;
    private AudioFileAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSlideInAnimation();

        setContentView(R.layout.activity_play_list);
        LogUtil.i(TAG, "onCreate(): " + this.toString());

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String playListUuid = intent.getStringExtra("play_list_uuid");
        playList = PlayListManager.getPlayListByUuid(playListUuid);

        updatePlayListInfo();

        initAudioFileListView();

        controlBar = findViewById(R.id.controlBar);
        musicController.subscribeHandler(handler);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.i(TAG, "onStart()");
        adaptor.notifyDataSetChanged();
        controlBar.updateAudioFileName();
        controlBar.changePausePlayIcon();
        controlBar.rotateImage(MusicController.getInstance().isPlaying());
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume()");
        updatePlayListInfo();
        adaptor.notifyDataSetChanged();
        controlBar.updateAudioFileName();
        controlBar.changePausePlayIcon();
        controlBar.rotateImage(MusicController.getInstance().isPlaying());
    }

    @Override
    public void onDestroy() {
        musicController.unsubscribeHandler(handler);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAudioFolderSelection();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 99 && resultCode == RESULT_OK) {
            Object[] selected_audio_file_list = data.getStringArrayExtra("selected_audio_file_list");
            if(selected_audio_file_list != null) {
                int count_old = playList.getFileCount();
                for (Object audioFilePath : selected_audio_file_list) {
                    AudioFile af = new AudioFile(0, (String)audioFilePath);
                    playList.addAudioFile(af);
                }
                playList.sort();
                adaptor.notifyDataSetChanged();

                playList.savePlayListContentToFile();
                PlayListManager.savePlayListToFile();

                updatePlayListInfo();
                int added_file_count = playList.getFileCount() - count_old;
                Toast.makeText(this,
                        String.format(getString(R.string.msg_add_files), added_file_count),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setSlideInAnimation() {
        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        //Slide slide = new Slide();
        //slide.setSlideEdge(Gravity.TOP);
        //slide.setDuration(1000);
        //slide.excludeTarget(android.R.id.navigationBarBackground, true);    // 排除导航栏
        //slide.excludeTarget(android.R.id.statusBarBackground,true);	        // 排除状态栏
        //getWindow().setReenterTransition(slide);
        //getWindow().setExitTransition(slide);
    }

    private void initAudioFileListView() {
        adaptor = new AudioFileAdaptor(
                this, R.layout.view_item_audio_file, playList.getAudioFileList());

        adaptor.setOnPlayButtonClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playSelectedAudio(v);
            }
        });

        adaptor.setOnItemDeleteClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                deleteAudioFile(v);
            }
        });

        lv = findViewById(R.id.viewAudioFileList);
        lv.setAdapter(adaptor);
        //lv.setNestedScrollingEnabled(true);
    }

    public void btnPlayAllClick(View v) {
        playList.setActiveIndex(0);
        playList.setPlayedTime(0);
        PlayListManager.setActivePlayList(playList.getUuid());

        musicController.setPlayList(playList);
        musicController.loadAndPlay();

        Intent intent = new Intent(PlayListActivity.this, PlayingActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void btnEditPlayListClick(View v) {
        Intent intent = new Intent(this, PlayListManageActivity.class);
        intent.putExtra("play_list_uuid", playList.getUuid());
        startActivity(intent);
    }

    public void btnAddFileToPlayListClick(View v) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        Intent intent = new Intent(this, AudioFileSelectActivity.class);
        startActivityForResult(intent, 99);
    }

    public void playSelectedAudio(View v) {
        LogUtil.d(TAG, "listView item clicked");

        Integer index = (Integer)v.getTag();
        AudioFile af = playList.getAudioFileByIndex(index);

        if (af != null && !af.isPlaying()) {
            playList.setActiveIndex(index);
            playList.setPlayedTime(0);
            PlayListManager.setActivePlayList(playList.getUuid());
            musicController.setPlayList(playList);
            musicController.loadAndPlay();
        }

        Intent intent1 = new Intent(PlayListActivity.this, PlayingActivity.class);
        startActivity(intent1, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void deleteAudioFile(View v) {
        LogUtil.d(TAG, "delete listView item clicked");

        lv.turnNormal();

        Integer index = (Integer)v.getTag();
        AudioFile audioFile = playList.getAudioFileByIndex(index);
        if(audioFile.isPlaying()) {
            musicController.playNext();
        }
        playList.removeAudioFile(index);

        playList.savePlayListContentToFile();
        PlayListManager.savePlayListToFile();

        adaptor.notifyDataSetChanged();
        updatePlayListInfo();

        Toast.makeText(PlayListActivity.this,getString(R.string.msg_delete_successfully), Toast.LENGTH_SHORT).show();
    }

    public void showAudioFolderSelection(){
        Intent intent = new Intent(this, AudioFileSelectActivity.class);
        startActivityForResult(intent, 99);
    }

    public void updatePlayListInfo() {
        TextView txtPlayLisName = findViewById(R.id.playListName);
        TextView txtPlayListFileCount = findViewById(R.id.playListFileCount);

        txtPlayLisName.setText(playList.getName());
        txtPlayListFileCount.setText(String.format(getString(R.string.total_file_number), playList.getFileCount()));
    }

    static class MyHandler extends Handler {

        private WeakReference<MyBaseActivity> activity;

        MyHandler(MyBaseActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LogUtil.d("PlayListHandler", "Get notification from the Music service");
            //获取从子线程发送过来的音乐播放的进度
            //Bundle bundle = msg.getData();
            //int duration = bundle.getInt("duration");
            //int currentPosition = bundle.getInt("currentPosition");
            PlayListActivity activity1 = (PlayListActivity) activity.get();
            if(activity1 ==  null) {
                return;
            }
            if(activity1.adaptor != null) {
                activity1.adaptor.notifyDataSetChanged();
            }
            if(activity1.controlBar != null) {
                activity1.controlBar.updateAudioFileName();
                activity1.controlBar.changePausePlayIcon();
                activity1.controlBar.rotateImage(MusicController.getInstance().isPlaying());
            }
        }
    }
}
