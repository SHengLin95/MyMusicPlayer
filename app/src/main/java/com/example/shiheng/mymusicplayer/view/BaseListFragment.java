package com.example.shiheng.mymusicplayer.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.MusicListAdapter;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public class BaseListFragment extends Fragment implements AdapterView.OnItemClickListener {
    protected ListView mListView;
    protected List<Music> mMusicList;
    protected MusicListAdapter mAdapter;
    protected IMusicController mController;
    protected int curIndex;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mMusicList = bundle.getParcelableArrayList(MainActivity.MUSIC_LIST);
        curIndex = bundle.getInt(MainActivity.MUSIC_INDEX, -1);
    }


    public void setMusicController(IMusicController controller) {
        mController = controller;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, null);
        mListView = (ListView) view.findViewById(R.id.fragment_list_lv);
        mListView.setOnItemClickListener(this);

        mAdapter = new MusicListAdapter(getContext(), mMusicList);
        mAdapter.setCurIndex(curIndex);
        mListView.setAdapter(mAdapter);
        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.load(position);
    }
}
