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
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;

import java.util.List;

public class MusicListFragment extends Fragment implements MusicTask.onFinishListener,
        AdapterView.OnItemClickListener {
    private IMusicController mController;
    private ListView mListView;
    private List<Music> mMusicList;
    private MusicTask mMusicTask;


    public void setMusicController(IMusicController controller) {
        mController = controller;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, null);
        mListView = (ListView) view.findViewById(R.id.fragment_list_lv);
        mListView.setOnItemClickListener(this);
        traversalAllMusic();
        return view;
    }

    private void traversalAllMusic() {
        mMusicTask = new MusicTask(getContext(), mListView);
        mMusicTask.setOnFinishListener(this);
        mMusicTask.execute();
    }

    /**
     * MusicTask回调接口
     */
    @Override
    public void onFinish() {
        mMusicList = mMusicTask.getMusicList();
        mController.setMusicList(mMusicList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.load(position);
    }
}
