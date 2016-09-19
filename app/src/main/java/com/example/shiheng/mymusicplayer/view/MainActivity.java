package com.example.shiheng.mymusicplayer.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity
        implements IMusicController {
    private static final String TAG = "MainActivity";
    private static final int UI_UPDATE = 1;
    private static final int INIT_FRAGMENT = 2;


    private static final String IS_PLAYING = "MainActivity.isPlaying";
    private static final String MUSIC_INDEX = "MainActivity.MusicIndex";

    public static final String MUSIC_LIST = "MainActivity.MusicIndex";

    private FragmentManager mFragmentManager;
    private MusicControlFragment mControlFragment;
    private MusicListFragment mMusicListFragment;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;


    private UIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new UIHandler();
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);

        initView();

        mFragmentManager = getSupportFragmentManager();
        mControlFragment = (MusicControlFragment) mFragmentManager.findFragmentById(R.id.main_music_control);
        mControlFragment.setMusicController(this);

    }


    private void initView() {
        //初始化toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initFragment() {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        mMusicListFragment = new MusicListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MUSIC_LIST, (ArrayList) mMusicList);
        mMusicListFragment.setArguments(bundle);
        mMusicListFragment.setMusicController(this);
        transaction.replace(R.id.main_ll, mMusicListFragment);
        transaction.commit();
    }


    // ---------------------------------------------------------------------------------
    // 用于音乐控制的接口
    // ---------------------------------------------------------------------------------

    @Override
    public void next() {
        loadMusic(MusicService.NEXT_INDEX_MARK);
    }

    @Override
    public void previous() {
        loadMusic(MusicService.PREVIOUS_INDEX_MARK);
    }


    @Override
    public void setMusicList(List<Music> musicList) {
        mMusicList = musicList;
        //绑定服务

    }

    @Override
    public void play() {
        try {
            mService.play();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(int index) {
        loadMusic(index);
    }

    private void updateInformation() {
        try {
            int index = mService.getCurIndex();
            Music music = mMusicList.get(index);
            mControlFragment.updateInformation(music.getTitle(), music.getArtist(), mService.isPlaying());
            if (mMusicListFragment != null) {
                mMusicListFragment.updateList(index);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    // ---------------------------------------------------------------------------------
    // 与service通信相关的接口
    // ---------------------------------------------------------------------------------

    private void loadMusic(int index) {
        try {
            mService.load(index);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------
    // BaseActivity相关的接口
    // ---------------------------------------------------------------------------------

    @Override
    protected void onServiceConnected() {
        Message msg = Message.obtain();
        msg.what = INIT_FRAGMENT;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onDataChanged() {
        Message msg = Message.obtain();
        msg.what = UI_UPDATE;
        mHandler.sendMessage(msg);
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_FRAGMENT:
                    initFragment();

//                    break;
                case UI_UPDATE:
                    updateInformation();
                    break;

            }
        }
    }


}
