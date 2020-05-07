/*
 * class: PlayListManager
 * Description:  Util tool for playlist management, it provided the function to add/delete/edit
 *               play list and read/write the play list to disk.
 * Author: Frank
 * Date: 2020/3/1
 */


package com.mega.oceanplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class PlayListManager {

    static final int PLAY_MODE_REPEAT = 0;
    static final  int PLAY_MODE_REPEAT_ONE = 1;
    static final  int PLAY_MODE_SHUFFLE = 2;

    private static String TAG = PlayListManager.class.getSimpleName();

    final private static String play_list_file_name = "myplayer_playlist.txt";
    final private static String playing_info_file_name = "myplayer_playing_info.txt";

    final private static String PLAY_MODE_SP = "PLAY_MODE_SP";
    final private static String PLAY_MODE_SP_KEY = "PLAY_MODE_SP_KEY";

    private static List<PlayList> listOfPlayList = new ArrayList<> ();
    private static PlayList activePlayList = null;

    static boolean play_list_changed = false;

    private static File appDir = null;

    private static int playMode = 0;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private PlayListManager() {
    }

    public static void setContext(Context context) {
        PlayListManager.context = context;
    }

    static void setPlayMode() {
        playMode = (playMode+1)%3;
        SharedPreferences spLocal = context.getSharedPreferences(PLAY_MODE_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = spLocal.edit();
        edit.putInt(PLAY_MODE_SP_KEY, playMode);
        boolean ret = edit.commit();
        if(!ret) {
            LogUtil.e(TAG, "save play mode to SharedPreferences failed: " + spLocal.toString());
        }
    }

    static void readSavedPlayMode() {
        LogUtil.d(TAG, "read saved play mode");
        SharedPreferences spLocale = context.getSharedPreferences(PLAY_MODE_SP, Context.MODE_PRIVATE);
        try {
            playMode = spLocale.getInt(PLAY_MODE_SP_KEY, 0);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            playMode = 0;
        }
    }

    static int getPlayMode() {
        return playMode;
    }

    static void setAppDataDir(File appDataDir) {
        appDir = appDataDir;
        if(!appDir.exists()) {
            boolean ret = appDir.mkdirs();
            if(!ret) {
                LogUtil.e(TAG, "create directory failed: " + appDataDir.getPath());
            }
        }
    }

    static File getAppDataDir() {
        return appDir;
    }

    static boolean isPlayListExist(String playListName) {
        for(PlayList playList: listOfPlayList) {
            if(playList.getName().equals(playListName)) {
                return true;
            }
        }
        return false;
    }

    static void addToFavorite(AudioFile audioFile) {
        PlayList myFavorite = getFavoritePlayList();
        AudioFile af = new AudioFile(0, audioFile.getFullPath());
        myFavorite.addAudioFile(af);
        myFavorite.sort();
        myFavorite.savePlayListContentToFile();
        savePlayListToFile();
    }

    static void removeFromFavorite(AudioFile audioFile) {
        PlayList myFavorite = getFavoritePlayList();
        for(AudioFile af: myFavorite.getAudioFileList()) {
            if(af.getFullPath().equals(audioFile.getFullPath())) {
                myFavorite.removeAudioFile(af);
                myFavorite.sort();
                myFavorite.savePlayListContentToFile();
                savePlayListToFile();
                break;
            }
        }
    }

    static List<PlayList> getListOfPlayList() {
        return listOfPlayList;
    }

    static void addPlayList(String playListName) {
        for(PlayList pl : listOfPlayList) {
            if(pl.getName().equals(playListName)) {
                return;
            }
        }
        PlayList pl = new PlayList(0, playListName);
        listOfPlayList.add(pl);
        savePlayListToFile();
    }

    static void removePlayList(int pos) {
        if(pos >= listOfPlayList.size())
            return;
        String content_file_name = listOfPlayList.get(pos).getName() + ".pl";
        LogUtil.d(TAG, "Remove item: " + pos + ", " + content_file_name);
        listOfPlayList.remove(pos);
        File file = new File(appDir, content_file_name);
        if(!file.delete()) {
            LogUtil.e(TAG, "delete file failed: " + file.getPath());
        }
        savePlayListToFile();
    }

    static PlayList getPlayListByIndex(int pos) {
        if(pos >=0 && pos < listOfPlayList.size()) {
            return listOfPlayList.get(pos);
        }
        return new PlayList(0, "New Play List");
    }

    static PlayList getPlayListByUuid(String uuid) {
        for(PlayList pl : listOfPlayList) {
            if(pl.getUuid().equals(uuid)) {
                return pl;
            }
        }
        return new PlayList(0, "New Play List");
    }

    static PlayList getActivePlayList() {
        if(activePlayList == null) {
            activePlayList = listOfPlayList.get(0);
            activePlayList.setActiveIndex(0);
            activePlayList.setPlayedTime(0);
        }
        return activePlayList;
    }

    static void setActivePlayList(String uuid) {
        activePlayList = getPlayListByUuid(uuid);
    }

    static void loadPlayingInfoFromFile() {
        LogUtil.d(TAG, "Try to read playing info");
        File file = new File(appDir, playing_info_file_name);
        if(!file.exists()) {
            LogUtil.e(TAG, "play info file doesn't exist");
            return;
        }
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bfreader = new BufferedReader(reader);
            String line = bfreader.readLine();
            if(line != null) {
                LogUtil.d(TAG, "Reading playing info: " + line);
                String [] items = line.split("\\|");
                String uuid = items[0];
                Integer active_audio_file_index = Integer.valueOf(items[1]);
                Integer active_audio_file_played_time = Integer.valueOf(items[2]);
                activePlayList = getPlayListByUuid(uuid);
                activePlayList.setActiveIndex(active_audio_file_index);
                activePlayList.setPlayedTime(active_audio_file_played_time);
            }
            else {
                LogUtil.w(TAG, "play info file is empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    static void savePlayingInfoToFile() {
        PlayList activePlayList = getActivePlayList();
        if(activePlayList == null) {
            return;
        }
        File file = new File(appDir, playing_info_file_name);
        try {
            if(!file.exists()) {
                boolean ret = file.createNewFile();
                if(!ret) {
                    LogUtil.e(TAG, "create file failed: " + file.getPath());
                }
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "GBK");
            //LogUtil.e(TAG, "Saving playing info: " + String.format("%s|%d|%d|%s\r\n", activePlayList.getName(), activePlayList.getActiveIndex(), activePlayList.getPlayedTime(), true));
            writer.write(String.format("%s|%d|%d|%s\r\n", activePlayList.getUuid(), activePlayList.getActiveIndex(), activePlayList.getPlayedTime(), true));
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadPlayListFromFile() {
        LogUtil.d(TAG, "Load play lsit from file");
        listOfPlayList.clear();
        File file = new File(appDir, play_list_file_name);
        if(!file.exists()) {
            createFavoritePlayList();
            return;
        }
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bfreader = new BufferedReader(reader);
            String line;
            int id = 0;
            boolean is_my_favorite_exists = false;
            while((line = bfreader.readLine()) != null) {
                LogUtil.d(TAG, "Read play list: " + line);
                String [] items = line.split("\\|");
                PlayList playList = new PlayList(id, items[0]);
                if(items.length>1) {
                    playList.setFileCount(Integer.valueOf(items[1]));
                }
                if(items.length>2) {
                    playList.setActiveIndex(Integer.valueOf(items[2]));
                }
                if(items.length>3) {
                    playList.setPlayedTime(Integer.valueOf(items[3]));
                }
                if(items.length>4) {
                    playList.setUuid(items[4]);
                }
                listOfPlayList.add(playList);
                playList.loadPlayListContentFromFile();

                if(playList.getUuid().equals("my_favorite")) {
                    is_my_favorite_exists = true;
                }
                id ++;
            }

            if(!is_my_favorite_exists) {
                createFavoritePlayList();
            }
            listOfPlayList.get(0).setName(context.getString(R.string.my_favorite));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFavoritePlayList() {
        LogUtil.d(TAG, "create favorite play list");
        PlayList pl = new PlayList(0, context.getString(R.string.my_favorite));
        pl.setUuid("my_favorite");
        listOfPlayList.add(0, pl);
        savePlayListToFile();
    }

    static void savePlayListToFile() {

        File file = new File(appDir, play_list_file_name);
        try {
            if(!file.exists()) {
                boolean ret = file.createNewFile();
                if(!ret) {
                    LogUtil.e(TAG, "create file failed: " + file.getAbsolutePath());
                }
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "GBK");
            for (PlayList pl : listOfPlayList){
                @SuppressLint("DefaultLocale")
                String line = String.format("%s|%d|%d|%d|%s\r\n", pl.getName(), pl.getFileCount(),pl.getActiveIndex(), pl.getPlayedTime(), pl.getUuid());
                LogUtil.d(TAG, "Saving play list: " + line);
                writer.write(line);
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        play_list_changed = true;
    }

    static PlayList getFavoritePlayList() {
        if(listOfPlayList.size() == 0 || !listOfPlayList.get(0).getUuid().equals("my_favorite")) {
            createFavoritePlayList();
        }
        return listOfPlayList.get(0);
    }

    static boolean isFavoriteAudioFile(AudioFile audioFile) {
        if(audioFile == null) {
            return false;
        }
        for(AudioFile af: getFavoritePlayList().getAudioFileList()) {
            if(af.getFullPath().equals(audioFile.getFullPath())) {
                return true;
            }
        }
        return false;
    }
}
