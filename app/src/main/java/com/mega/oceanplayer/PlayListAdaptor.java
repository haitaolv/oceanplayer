package com.mega.oceanplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class PlayListAdaptor extends ArrayAdapter{

    private int resourceId;

    public PlayListAdaptor(@NonNull Context context, int resource, List<PlayList> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前项的Fruit实例
        PlayList playList = (PlayList) getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null){
            // inflate出子项布局，实例化其中的图片控件和文本控件
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.SongListName);
            viewHolder.number = view.findViewById(R.id.SongNumber);
            viewHolder.imgBtnEdit = view.findViewById(R.id.imgBtnEdit);
            viewHolder.txtDelete = view.findViewById(R.id.txtDelete);
            viewHolder.layoutDelete = view.findViewById(R.id.layoutDelete);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if(playList != null) {
            viewHolder.name.setText(playList.getName());
            viewHolder.number.setText(String.format(getContext().getString(R.string.total_file_number), playList.getFileCount()));
        }

        if(position == 0) {
            viewHolder.imgBtnEdit.setVisibility(View.INVISIBLE);
            viewHolder.layoutDelete.setVisibility(View.INVISIBLE);
        }
        else {
            viewHolder.imgBtnEdit.setOnClickListener(this.mOnItemEditListener);
            viewHolder.imgBtnEdit.setTag(position);
            viewHolder.txtDelete.setOnClickListener(this.mOnItemDeleteListener);
            viewHolder.txtDelete.setTag(position);
        }

        return view;
    }

    private View.OnClickListener mOnItemDeleteListener;
    private View.OnClickListener mOnItemEditListener;

    public void setOnItemDeleteClickListener(View.OnClickListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    public void setOnItemEditClickListener(View.OnClickListener mOnItemEditListener) {
        this.mOnItemEditListener = mOnItemEditListener;
    }

    static class ViewHolder{
        TextView name;
        TextView number;
        ImageView imgBtnEdit;
        TextView txtDelete;
        LinearLayout layoutDelete;
    }
}
