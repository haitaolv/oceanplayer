/*
 * class: AudioFile
 * Description:  definition of the AudioFile
 * Author: Frank
 * Date: 2020/3/1
 */


package com.mega.oceanplayer;

public class AudioFile {
    private Integer index;
    private String audioName;
    private String fullPath;
    private boolean isPlaying;
    private boolean isChecked;
    private boolean isLoaded;

    public AudioFile(Integer index, String fullPath) {
        this.index = index;
        this.fullPath = fullPath;
        audioName = fullPath.replaceAll(".*/", "");
        isPlaying = false;
        isChecked = false;
        isLoaded = false;
    }

    Integer getIndex() {
        return this.index;
    }

    void setIndex(Integer index) {
        this.index = index;
    }

    String getAudioName() {
        return this.audioName;
    }

    String getFullPath() {
        return this.fullPath;
    }

    public void setIsPlaying(boolean isPlaying) {
        //LogUtil.e(TAG, String.format("set playing=%s for %s", isPlaying, audioName));
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    boolean isChecked() {
        return this.isChecked;
    }

    void setIsLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    boolean isLoaded() {
        return this.isLoaded;
    }
}
