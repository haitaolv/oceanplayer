package com.mega.oceanplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class PlayListManageActivity extends MyBaseActivity {

    private final String TAG = PlayListManageActivity.class.getSimpleName() ;
    private PlayList playList;
    private AudioFileAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_manage);

        Intent intent = getIntent();
        String playListUuid = intent.getStringExtra("play_list_uuid");
        playList = PlayListManager.getPlayListByUuid(playListUuid);

        initListView();
    }

    public void initListView() {
        adaptor = new AudioFileAdaptor(
                this, R.layout.view_item_select_audio_file, playList.getAudioFileList());

        ListView lv = findViewById(R.id.listViewPlayList);
        lv.setAdapter(adaptor);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playList.getAudioFileList().get(position).setIsChecked(!playList.getAudioFileList().get(position).isChecked());
                adaptor.notifyDataSetChanged();
            }
        });
    }

    public void selectAllClicked(View v) {
        LogUtil.d(TAG, "selectAll");
        boolean all_checked = true;
        for(AudioFile af : playList.getAudioFileList()) {
            if(!af.isChecked()) {
                all_checked = false;
                break;
            }
        }
        for(AudioFile af : playList.getAudioFileList()) {
            af.setIsChecked(!all_checked);
        }
        adaptor.notifyDataSetChanged();
    }

    public void backClicked(View v) {
        finish();
    }

    public void deleteSelectedAudioFiles(View v) {
        boolean playingDeleted = playList.deleteCheckedAudioFile();
        PlayListManager.savePlayListToFile();
        if(playingDeleted) {
            musicController.playNext();
        }
        adaptor.notifyDataSetChanged();
    }

    public void okClicked(View v) {
        finish();
    }
}
