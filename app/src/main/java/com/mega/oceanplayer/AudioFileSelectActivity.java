package com.mega.oceanplayer;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AudioFileSelectActivity extends MyBaseActivity {

    private String TAG = AudioFileSelectActivity.class.getSimpleName();

    private LinearLayout leftLayout;
    private LinearLayout rightLayout;

    private List<AudioFolder> audioFolderList;
    private List<AudioFile> audioFileList;

    private ListView lv ;
    private AudioFolderAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file_select);

        LogUtil.i(TAG, "onCreate");

        audioFolderList = new ArrayList<AudioFolder>();
        audioFileList = new ArrayList<AudioFile>();
        getAudioFolders();

        initAudioFolderListView();
        initAudioFileListView();

        leftLayout = findViewById(R.id.leftLayout);
        rightLayout = findViewById(R.id.rightLayout);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rightLayout.getLayoutParams();
        layoutParams.width = getScreenWidth();
        rightLayout.setLayoutParams(layoutParams);
    }

    public void initAudioFolderListView() {
        LogUtil.i(TAG, "initAudioFolderListView");
        lv = (ListView) findViewById(R.id.audioFolderListView);
        adaptor = new AudioFolderAdaptor(this, R.layout.view_item_select_audio_folder, audioFolderList);
        lv.setAdapter(adaptor);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView showAudioFileList = view.findViewById(R.id.showAudioFileList);
                showAudioFileList(showAudioFileList);
            }
        });
    }

    public void initAudioFileListView() {
        LogUtil.i(TAG, "initAudioFileListView");
        ListView lv = (ListView) findViewById(R.id.audioFileListView);
        AudioFileAdaptor audioFileAdaptor = new AudioFileAdaptor(this, R.layout.view_item_select_audio_file, audioFileList);
        lv.setAdapter(audioFileAdaptor);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                audioFileList.get(position).setIsChecked(!audioFileList.get(position).isChecked());
                adaptor.notifyDataSetChanged();
            }
        });
    }

    public void showAudioFileList(View v){
        LogUtil.d(TAG, "showAudioFileList: tag = " + v.getTag().toString());
        audioFileList.clear();

        AudioFolder audioFolder= audioFolderList.get((int)v.getTag());
        if(audioFolder != null) {
            int index = 0;
            for (String audioFilePath : audioFolder.getAudioFileList()) {
                AudioFile af = new AudioFile(index, audioFilePath);
                audioFileList.add(af);
                index++;
            }
        }
        else {
            LogUtil.w(TAG, "audio folder is null, not likely happens");
        }

        ListView lv = (ListView) findViewById(R.id.audioFileListView);
        AudioFileAdaptor adaptor = (AudioFileAdaptor)lv.getAdapter();
        adaptor.notifyDataSetChanged();
        startAnimate(true);
    }

    public void backToAudioFolderList(View v) {
        LogUtil.d(TAG, "backToAudioFolderList");
        startAnimate(false);
    }

    public void selectAllClicked(View v) {
        LogUtil.d(TAG, "selectAll");
        boolean all_checked = true;
        for(AudioFile af : audioFileList) {
            if(!af.isChecked()) {
                all_checked = false;
            }
        }
        for(AudioFile af : audioFileList) {
            af.setIsChecked(!all_checked);
        }
        ListView lv = (ListView) findViewById(R.id.audioFileListView);
        AudioFileAdaptor adaptor = (AudioFileAdaptor)lv.getAdapter();
        adaptor.notifyDataSetChanged();
    }

    public void cancelClicked(View v) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void okClicked(View v) {
        Intent intent = new Intent();
        List<String> selected_audio_file_list = new ArrayList<String>();
        for(AudioFile af : audioFileList) {
            if(af.isChecked()) {
                selected_audio_file_list.add(af.getFullPath());
            }
        }

        if(selected_audio_file_list.size()==0){
            Toast.makeText(this, getString(R.string.msg_select_at_least_one_file), Toast.LENGTH_SHORT).show();
            return;
        }

        if(audioFileList.size() > 0) {
            intent.putExtra("selected_audio_file_list", selected_audio_file_list.toArray(new String[audioFileList.size()]));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void getAudioFolders() {
        LogUtil.i(TAG, "getMusicFolders");
        ContentResolver mContentResolver;
        Cursor c = null;

        try {
            mContentResolver = this.getContentResolver();
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            while(c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                addAudioFile(path);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addAudioFile(String audioFileFullPath) {
        int pos = audioFileFullPath.lastIndexOf("/");

        String audioFolderPath = audioFileFullPath.substring(0, pos);

        for(AudioFolder audioFolder: audioFolderList) {
            if (audioFolder.getFolderFullPath().equals(audioFolderPath)) {
                audioFolder.addAudioFile(audioFileFullPath);
                return;
            }
        }

        LogUtil.d(TAG, "add new audio folder: " + audioFolderPath);
        AudioFolder audioFolder = new AudioFolder(audioFolderPath);
        audioFolder.addAudioFile(audioFileFullPath);
        audioFolderList.add(audioFolder);
    }

    public void startAnimate(boolean left) {
        LogUtil.i(TAG, "startAnimate: left=" + left);

        int offset = left? -getScreenWidth() : 0;

        int duration = 500;

        ObjectAnimator animation = ObjectAnimator.ofFloat(leftLayout, "translationX", offset);
        animation.setDuration(duration);
        animation.start();

        ObjectAnimator animation1 = ObjectAnimator.ofFloat(rightLayout, "translationX", offset);
        animation1.setDuration(duration);
        animation1.start();
    }
}
