package com.example.shiheng.mymusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shiheng.mymusicplayer.model.GridItem;

import java.util.List;


public class MusicGridAdapter extends BaseAdapter {
    List<GridItem> mData;
    LayoutInflater mInflater;

    public MusicGridAdapter(Context context, List<GridItem> mData) {
        mInflater = LayoutInflater.from(context);
        this.mData = mData;
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
            convertView = mInflater.inflate(R.layout.item_grid, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_grid_iv);
            viewHolder.title = (TextView) convertView.findViewById(R.id.item_grid_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        GridItem item = mData.get(position);
        if (item.getAlbum() != null) {
            viewHolder.imageView.setImageBitmap(item.getAlbum());
        }
        viewHolder.title.setText(item.getTitle());

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView title;
    }

}
