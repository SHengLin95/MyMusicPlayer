package com.example.shiheng.mymusicplayer.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.shiheng.mymusicplayer.IMusicClient;
import com.example.shiheng.mymusicplayer.IMusicControl;
import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements IMusicController {
    private static final String TAG = "MainActivity";
    private static final int UI_UPDATE = 1;

    private static final String IS_PLAYING = "MainActivity.isPlaying";
    private static final String MUSIC_INDEX = "MainActivity.MusicIndex";

    private MusicControlFragment mControlFragment;
    private MusicListFragment mMusicListFragment;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    private List<Music> mMusicList;
    private IMusicControl mService;

    private UIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        mHandler = new UIHandler();
        initFragment();


        startService(new Intent(this, MusicService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mService != null) {
                mService.unregisterClient(mClient);
                unbindService(mConnection);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initFragment() {
        //初始化View
        FragmentManager fragmentManager = getSupportFragmentManager();
        mControlFragment = (MusicControlFragment) fragmentManager.findFragmentById(R.id.main_music_control);
        mControlFragment.setMusicController(this);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mMusicListFragment = new MusicListFragment();
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
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);
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

    private void updateInformation(int index, boolean isPlaying) {
        Music music = mMusicList.get(index);
        mControlFragment.updateInformation(music.getTitle(), music.getArtist(), isPlaying);
        if (mMusicListFragment != null)
            mMusicListFragment.updateList(index);
    }


    // ---------------------------------------------------------------------------------
    // 与service通信相关的接口
    // ---------------------------------------------------------------------------------


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnect", "onServiceConnected");
            mService = IMusicControl.Stub.asInterface(service);
            try {
                mService.registerClient(mClient);
                mService.setMusicList(mMusicList);
                loadMusic(0, true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unregisterClient(mClient);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private void loadMusic(int index) {
        loadMusic(index, false);
    }

    private void loadMusic(int index, boolean preLoad) {
        try {
            mService.load(index, preLoad);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private IMusicClient.Stub mClient = new IMusicClient.Stub() {
        @Override
        public void update(int index, boolean isPlaying) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = UI_UPDATE;
            Bundle bundle = msg.getData();
            bundle.putBoolean(IS_PLAYING, isPlaying);
            bundle.putInt(MUSIC_INDEX, index);
            mHandler.sendMessage(msg);
        }
    };

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_UPDATE:
                    Bundle bundle = msg.getData();
                    updateInformation(bundle.getInt(MUSIC_INDEX), bundle.getBoolean(IS_PLAYING));
                    break;
            }
        }
    }


}
