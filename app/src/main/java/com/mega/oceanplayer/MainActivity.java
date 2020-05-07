/*
 * class: MainActivity
 * Description: main UI of the app, it provided a listview of the user created play list
 * Author: Frank
 * Date: 2020/3/1
 */

package com.mega.oceanplayer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import java.lang.ref.WeakReference;

public class MainActivity extends MyBaseActivity {

    private String TAG = MainActivity.class.getSimpleName();

    public MyHandler handler = new MyHandler(this);

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.w(TAG, "onCreate(), task id is: " + getTaskId() + ", " + this.toString());

        setSlideInAnimation();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PlayListManager.getFavoritePlayList().setName(getString(R.string.my_favorite));

        initPlayListView();

        musicController.subscribeHandler(handler);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
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

    private void initPlayListView() {
        PlayListAdaptor adaptor = new PlayListAdaptor(
                MainActivity.this,
                R.layout.view_item_play_list,
                PlayListManager.getListOfPlayList());

        adaptor.setOnItemDeleteClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "playlist view item delete button clicked: " + v.getTag());
                Integer pos = (Integer)v.getTag();
                SideSlippingListView lv = findViewById(R.id.viewListOfPlayList);
                lv.turnNormal();
                removePlayList(pos);
            }
        });

        adaptor.setOnItemEditClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "playlist view item edit button clicked");
                Integer pos = (Integer)v.getTag();
                modifyPlayListName(pos);
            }
        });

        SideSlippingListView lv = findViewById(R.id.viewListOfPlayList);
        lv.setAdapter(adaptor);
        lv.setNestedScrollingEnabled(true);
        lv.setFixFirstRow(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtil.d(TAG, "playlist view item clicked");
                startPlayListActivity(i);
            }
        });
    }

    public void startPlayListActivity(int i) {
        Intent intent= new Intent(MainActivity.this, PlayListActivity.class);
        intent.putExtra("play_list_uuid", PlayListManager.getListOfPlayList().get(i).getUuid());
        startActivity(intent, null);
    }

    public void createNewPlayList(View view) {
        View v = View.inflate(this, R.layout.layout_playlist_name, null);
        final EditText et = v.findViewById(R.id.txtPlayListName);
        et.setText("");
        et.addTextChangedListener(new TextMaxLengthWatcher(et, 24));

        final AlertDialog alertDialog1 = new AlertDialog.Builder(view.getContext())
                .setTitle(getString(R.string.create_play_list_title))
                .setView(v)
                .setIcon(R.mipmap.icon_128)
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setPositiveButton(getString(R.string.btn_save), null)
                .create();

        alertDialog1.show();

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validatePlayListName(et.getText().toString())){
                    addPlayList(et.getText().toString());
                    alertDialog1.dismiss();
                }
            }
        });
    }

    public void modifyPlayListName(final Integer pos) {
        View v = View.inflate(this, R.layout.layout_playlist_name, null);
        final EditText et = v.findViewById(R.id.txtPlayListName);
        et.setText(PlayListManager.getPlayListByIndex(pos).getName());
        et.addTextChangedListener(new TextMaxLengthWatcher(et, 24));

        final AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.modify_play_list_title))
                .setView(v)
                .setIcon(R.mipmap.icon_128)
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setPositiveButton(getString(R.string.btn_save), null)
                .create();

        alertDialog1.show();

        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validatePlayListName(et.getText().toString())){
                    updatePlayListName(pos, et.getText().toString());
                    alertDialog1.dismiss();
                }
            }
        });
    }

    public boolean validatePlayListName(String playListName) {
        if (playListName.equals("")) {
            Toast.makeText(MainActivity.this, R.string.msg_play_lst_name_can_not_empty, Toast.LENGTH_SHORT).show();
            return false;
        } else if(playListName.length()> 32) {
            Toast.makeText(MainActivity.this, R.string.msg_play_lst_name_too_long, Toast.LENGTH_SHORT).show();
            return false;
        } else if (PlayListManager.isPlayListExist(playListName)) {
            Toast.makeText(MainActivity.this, R.string.msg_play_lst_name_already_exist, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void recreate() {
        super.recreate();
        LogUtil.i(TAG,"recreate()");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        LogUtil.i(TAG,"onRestart()");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.i(TAG,"onStart()");
        updateControlBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG,"onResume()");

        if(PlayListManager.play_list_changed) {
            refreshPlayListView();
            PlayListManager.play_list_changed = false;
        }
        updateControlBar();
    }

    @Override
    public void onPause() {
        LogUtil.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtil.i(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtil.w(TAG,"onDestroy");
        musicController.unsubscribeHandler(handler);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            LogUtil.d(TAG,"gets back event");
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keycode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogUtil.d(TAG,"onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.d(TAG,"onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final String []languages = getResources().getStringArray(R.array.language_array);
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.icon_128)
                    .setTitle(getString(R.string.action_settings))
                    .setItems(languages,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setLanguage(which);
                        }
                    }).show();
            return true;
        }
        else if(id == R.id.action_about) {
            @SuppressLint("InflateParams") View aboutDetail = getLayoutInflater().inflate(R.layout.layout_about, null);
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.icon_128)
                    .setTitle(getString(R.string.action_about))
                    .setView(aboutDetail)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateControlBar() {
        MusicControlBar controlBar = findViewById(R.id.controlBar);
        controlBar.updateAudioFileName();
        controlBar.changePausePlayIcon();
        controlBar.rotateImage(MusicController.getInstance().isPlaying());
    }

    public void refreshPlayListView() {
        ListView lv = findViewById(R.id.viewListOfPlayList);
        PlayListAdaptor adaptor = (PlayListAdaptor) lv.getAdapter();
        adaptor.notifyDataSetChanged();
    }

    public void addPlayList(String name) {
        PlayListManager.addPlayList(name);
        refreshPlayListView();
        Toast.makeText(MainActivity.this,getString(R.string.msg_save_successfully), Toast.LENGTH_SHORT).show();
    }

    public void removePlayList(int pos) {
        PlayListManager.removePlayList(pos);
        refreshPlayListView();
        Toast.makeText(MainActivity.this,getString(R.string.msg_delete_successfully), Toast.LENGTH_SHORT).show();
    }

    public void updatePlayListName(int pos, String name) {
        PlayList playList = PlayListManager.getPlayListByIndex(pos);
        //playList.renamePlayListContentFile(name);
        playList.setName(name);

        PlayListManager.savePlayListToFile();
        refreshPlayListView();
        Toast.makeText(MainActivity.this,getString(R.string.msg_save_successfully), Toast.LENGTH_SHORT).show();
    }

    public void setLanguage(int language) {
        LogUtil.d(TAG, "Set language to " + LanguageUtil.getLanguageById(language));
        if(LanguageUtil.updateLocale(this, LanguageUtil.getLanguageById(language))) {
            LanguageUtil.saveLocale(this, language);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    static class MyHandler extends Handler {

        private WeakReference<MyBaseActivity> activity;

        MyHandler(MyBaseActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            //获取从子线程发送过来的音乐播放的进度
            //Bundle bundle = msg.getData();
            //int duration = bundle.getInt("duration");
            //int currentPosition = bundle.getInt("currentPosition");
            MyBaseActivity activity1 = activity.get();
            if(activity1 == null) {
                return;
            }

            MusicControlBar controlBar = activity1.findViewById(R.id.controlBar);
            if(controlBar != null) {
                controlBar.updateAudioFileName();
                controlBar.changePausePlayIcon();
                controlBar.rotateImage(MusicController.getInstance().isPlaying());
            }
        }
    }
}
