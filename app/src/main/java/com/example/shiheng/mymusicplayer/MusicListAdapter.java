package com.example.shiheng.mymusicplayer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_SELECTED = 1;
    private int curIndex = -1;
    private Context context;
    private List<Music> mData;

    public MusicListAdapter(Context context, List<Music> musicList) {
        this.context = context;
        mData = musicList;
    }


    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position == curIndex ? VIEW_TYPE_SELECTED : VIEW_TYPE_DEFAULT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_all_music, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.item_music_title);
            viewHolder.artistAndAlbum = (TextView) convertView.findViewById(R.id.item_music_artist_and_album);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Music music = mData.get(position);
        viewHolder.title.setText(music.getTitle());
        if (getItemViewType(position) == VIEW_TYPE_SELECTED) {
            viewHolder.title.setTextColor(Color.RED);

        }
        viewHolder.artistAndAlbum.setText(music.getArtist() + " - " + music.getAlbum());
        return convertView;
    }

    private class ViewHolder {
        TextView title;
        TextView artistAndAlbum;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
        notifyDataSetChanged();
    }

    public void setData(List<Music> data) {
        curIndex = -1;
        mData = data;
        notifyDataSetChanged();
    }
}
