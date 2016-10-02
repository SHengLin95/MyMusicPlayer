package com.example.shiheng.mymusicplayer.view;


import android.view.View;
import android.widget.AdapterView;

public class MusicShowListFragment extends BaseListFragment {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.updateMusicList(mMusicList);
        super.onItemClick(parent, view, position, id);
    }
}
