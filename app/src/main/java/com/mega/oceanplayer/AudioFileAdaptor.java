package com.mega.oceanplayer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class AudioFileAdaptor extends ArrayAdapter {

    private String TAG = AudioFileAdaptor.class.getSimpleName();

    private int resourceId;

    private int textDefaultColor = R.color.colorBlack;
    private int defaultImg = R.drawable.ic_play_blue_24dp;

    public AudioFileAdaptor(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public void setTextDefaultColor(int color) {
        textDefaultColor = color;
    }

    public void setDefaultImage(int img) {
        defaultImg = img;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AudioFile af = (AudioFile) getItem(position);
        View view;
        AudioFileAdaptor.ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.cbAudioFileIndex = view.findViewById(R.id.cbAudioFileIndex);
            viewHolder.txtIndex = view.findViewById(R.id.txtIndex);
            viewHolder.txtAudioName =view.findViewById(R.id.txtAudioFileName);
            viewHolder.imgBtnPlay = view.findViewById(R.id.imgBtnPlay);
            viewHolder.txtDelete = view.findViewById(R.id.txtDelete);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (AudioFileAdaptor.ViewHolder) view.getTag();
        }

        if(viewHolder.cbAudioFileIndex != null) {
            viewHolder.cbAudioFileIndex.setChecked(af.isChecked());
            viewHolder.cbAudioFileIndex.setTag(position);
            viewHolder.cbAudioFileIndex.setOnClickListener(this.mItemCheckBoxClickLister);
        }

        if(viewHolder.txtIndex != null)
            viewHolder.txtIndex.setText(af.getIndex().toString());

        if(viewHolder.txtAudioName != null)
            viewHolder.txtAudioName.setText(af.getAudioName());

        if(viewHolder.imgBtnPlay != null) {
            viewHolder.imgBtnPlay.setTag(position);
            viewHolder.imgBtnPlay.setOnClickListener(this.mPlayButtonClickLister);
        }
        if(viewHolder.txtDelete != null) {
            viewHolder.txtDelete.setTag(position);
            viewHolder.txtDelete.setOnClickListener(this.mItemDeleteClickLister);
        }

        if(this.resourceId == R.layout.view_item_audio_file) {

            if (af.isPlaying()) {
                if (viewHolder.imgBtnPlay != null) {
                    viewHolder.imgBtnPlay.setImageDrawable(getContext().getDrawable(R.drawable.ic_equalizer_green_24dp));
                    AnimationDrawable myAnimate = (AnimationDrawable) viewHolder.imgBtnPlay.getDrawable();
                    myAnimate.start();
                }
            }
            else {
                if (viewHolder.imgBtnPlay != null)
                    viewHolder.imgBtnPlay.setImageDrawable(getContext().getDrawable(defaultImg));
            }

            if(af.isLoaded()) {
                if (viewHolder.txtIndex != null)
                    viewHolder.txtIndex.setTextColor(getContext().getColor(R.color.colorGreen));
                if (viewHolder.txtAudioName != null)
                    viewHolder.txtAudioName.setTextColor(getContext().getColor(R.color.colorGreen));
            }
            else {
                if (viewHolder.txtAudioName != null)
                    viewHolder.txtAudioName.setTextColor(getContext().getColor(textDefaultColor));
                if (viewHolder.txtIndex != null)
                    viewHolder.txtIndex.setTextColor(getContext().getColor(textDefaultColor));
            }
        }

        return view;
    }

    private View.OnClickListener mPlayButtonClickLister = null;

    private View.OnClickListener mItemDeleteClickLister = null;

    private View.OnClickListener mItemCheckBoxClickLister = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            LogUtil.d(TAG, "mItemCheckBoxClickLister clicked on position: " + v.getTag());
            Integer pos = (Integer)v.getTag();
            AudioFile af = (AudioFile) getItem((int)pos);
            CheckBox cb = (CheckBox)v;
            if(af != null) {
                af.setIsChecked(cb.isChecked());
            }
            LogUtil.d(TAG, "mItemCheckBoxClickLister af index: " + af.getIndex());
        }
    };

    public void setOnPlayButtonClickListener(View.OnClickListener mPlayButtonClickLister) {
        this.mPlayButtonClickLister = mPlayButtonClickLister;
    }

    public void setOnItemDeleteClickListener(View.OnClickListener mItemDeleteClickLister) {
        this.mItemDeleteClickLister = mItemDeleteClickLister;
    }

    static class ViewHolder{
        CheckBox cbAudioFileIndex;
        TextView txtIndex;
        TextView txtAudioName;
        ImageView imgBtnPlay;
        TextView txtDelete;
    }
}
