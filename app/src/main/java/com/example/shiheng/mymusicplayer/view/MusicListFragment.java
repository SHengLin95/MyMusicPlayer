package com.example.shiheng.mymusicplayer.view;

import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public class MusicListFragment extends BaseListFragment {

    public void updateData(List<Music> data) {
        if (mAdapter != null) {
            mAdapter.setData(data);
        }
    }

    public void updateList(int index) {
        if (mAdapter != null) {
            mAdapter.setCurIndex(index);
        }
    }
}
