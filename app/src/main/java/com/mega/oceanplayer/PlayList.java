/*
 * class: PlayList
 * Description:  definition of the playlist
 * Author: Frank
 * Date: 2020/3/1
 */


package com.mega.oceanplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PlayList {
    private String TAG = PlayList.class.getSimpleName();

    private Integer Id;
    private String Name;
    private Integer fileCount;
    private List<AudioFile> audioFiles;
    private Integer activeIndex;
    private Integer playedTime;
    private String uuid;

    PlayList(Integer id, String name) {
        this.Name = name;
        this.fileCount = 0;
        this.Id = id;
        this.audioFiles = new ArrayList<>();
        this.playedTime = 0;
        this.activeIndex = 0;
        this.uuid = UUID.randomUUID().toString();
    }

    public Integer getId() {
        return this.Id;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    String getUuid() {
        return this.uuid;
    }
    void setUuid(String uuid) {
        this.uuid = uuid;
    }

    Integer getFileCount() {
        return this.fileCount;
    }

    void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    void addAudioFile(AudioFile audioFile) {
        AudioFile af = getAudioFileByPath(audioFile.getFullPath());
        if(af == null) {
            this.audioFiles.add(audioFile);
            this.fileCount = this.audioFiles.size();
        }
        else {
            LogUtil.w(TAG, "audio file already in list: " + audioFile.getFullPath());
        }
    }

    void removeAudioFile(int pos) {
        if(pos >= 0 && pos < audioFiles.size()) {
            audioFiles.remove(pos);
            this.sort();
        }
        this.fileCount = this.audioFiles.size();
    }

    void removeAudioFile(AudioFile audioFile) {
        audioFiles.remove(audioFile);
        this.fileCount = this.audioFiles.size();
    }

    List<AudioFile> getAudioFileList() {
        return this.audioFiles;
    }

    AudioFile getAudioFileByIndex(int index) {
        if(index < this.audioFiles.size()) {
            return this.audioFiles.get(index);
        }
        return null;
    }

    private AudioFile getAudioFileByPath(String audioFilePath) {
        for (AudioFile af :this.audioFiles) {
            if(af.getFullPath().equals(audioFilePath)) {
                return af;
            }
        }
        return null;
    }

    void setActiveIndex(Integer activeIndex) {
        if(activeIndex < 0) {
            this.activeIndex = audioFiles.size()-1;
        }
        else if(activeIndex >= audioFiles.size()) {
            this.activeIndex = 0;
        }
        else {
            this.activeIndex = activeIndex;
        }

        for(AudioFile af: audioFiles) {
            if(!af.getIndex().equals(this.activeIndex-1)) {
                //LogUtil.e(TAG, String.format("set flag: %d-%s: %s", af.getIndex(), af.getAudioName(), af.isPlaying()));
                af.setIsPlaying(false);
            }
        }
    }

    Integer getActiveIndex() {
        return this.activeIndex;
    }

    void setPlayedTime(Integer playedTime) {
        this.playedTime = playedTime;
    }

    Integer getPlayedTime() {
        return this.playedTime;
    }

    AudioFile getActiveAudioFile() {
        return getAudioFileByIndex(this.activeIndex);
    }

    void sort() {
        Collections.sort(audioFiles, new Comparator<AudioFile>() {
            @Override
            public int compare(AudioFile o1, AudioFile o2) {
                return o1.getAudioName().compareTo(o2.getAudioName());
            }
        });

        Integer index = 1;
        for(AudioFile af : audioFiles) {
            af.setIndex(index);
            index++;
        }
    }

    void loadPlayListContentFromFile() {
        audioFiles.clear();
        File file = new File(PlayListManager.getAppDataDir(), uuid + ".pl");
        if(!file.exists()) {
            return;
        }
        try {
            LogUtil.d(TAG, "Read play list content from file");
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bfreader = new BufferedReader(reader);
            String line;
            while((line = bfreader.readLine()) != null) {
                //LogUtil.e(TAG, "Read from file: " + line);
                AudioFile af = new AudioFile(0, line);
                addAudioFile(af);
            }
            sort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void savePlayListContentToFile() {
        File file = new File(PlayListManager.getAppDataDir(), uuid + ".pl");
        try {
            LogUtil.d(TAG, "Save play list content to file");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "GBK");
            for (AudioFile af : audioFiles){
                //LogUtil.e(TAG, "Write to file: " + af.getFullPath());
                writer.write(af.getFullPath()  + "\r\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean deleteCheckedAudioFile() {
        Iterator<AudioFile > it = audioFiles.iterator();
        boolean playingDeleted = false;
        while(it.hasNext()){
            AudioFile af = it.next();
            if(af.isChecked()) {
                LogUtil.d(TAG, "Remove audio file: " + af.getFullPath());
                if(af.isPlaying()) {
                    playingDeleted = true;
                }
                it.remove();
            }
        }
        fileCount = audioFiles.size();
        sort();
        savePlayListContentToFile();
        return playingDeleted;
    }
}
