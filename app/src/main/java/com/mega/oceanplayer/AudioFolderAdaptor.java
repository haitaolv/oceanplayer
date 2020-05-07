package com.mega.oceanplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class AudioFolderAdaptor extends ArrayAdapter{

    private int resourceId;

    public AudioFolderAdaptor(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 获取当前项的AudioFolder实例
        AudioFolder af = (AudioFolder) getItem(position);
        View view;
        AudioFolderAdaptor.ViewHolder viewHolder;

        if (convertView == null){
            // inflate出子项布局，实例化其中的图片控件和文本控件
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.audioFolderName = view.findViewById(R.id.audioFolderName);
            viewHolder.audioFileCount = view.findViewById(R.id.audioFileCount);
            viewHolder.audioFolderFullPath = view.findViewById(R.id.audioFolderFullPath);
            viewHolder.showAudioFileList = view.findViewById(R.id.showAudioFileList);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (AudioFolderAdaptor.ViewHolder) view.getTag();
        }

        if(af != null) {
            viewHolder.audioFolderName.setText(af.getFolderName());
            viewHolder.audioFileCount.setText(String.format(getContext().getString(R.string.total_file_number), af.getAudioFileCount()));
            viewHolder.audioFolderFullPath.setText(af.getFolderFullPath());
        }
        viewHolder.showAudioFileList.setTag(position);

        return view;
    }

    static class ViewHolder{
        TextView audioFolderName;
        TextView audioFileCount;
        TextView audioFolderFullPath;
        ImageView showAudioFileList;
    }
}
