/*
 * class: AudioFolder
 * Description:  definition of the AudioFolder
 * Author: Frank
 * Date: 2020/3/1
 */

package com.mega.oceanplayer;

import java.util.ArrayList;
import java.util.List;

class AudioFolder {
    private String fullPath;
    private String folderName;
    private List<String> audioFileList;

    AudioFolder(String fullPath) {
        this.fullPath = fullPath;
        this.folderName = fullPath.replaceAll(".*/", "");
        audioFileList = new ArrayList<>();
    }

    void addAudioFile(String audioFilePath) {
        this.audioFileList.add(audioFilePath);
    }

    List<String> getAudioFileList() {
        return this.audioFileList;
    }

    String getFolderFullPath() {
        return this.fullPath;
    }

    String getFolderName() {
        return this.folderName;
    }

    Integer getAudioFileCount() {
        return this.audioFileList.size();
    }
}
