package com.example.shiheng.mymusicplayer.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.shiheng.mymusicplayer.IMusicClient;
import com.example.shiheng.mymusicplayer.IMusicControl;
import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements IMusicController {
    protected List<Music> mMusicList;
    protected IMusicControl mService;

    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnect", "onServiceConnected");
            mService = IMusicControl.Stub.asInterface(service);
            try {
                mService.registerClient(mClient);
                mMusicList = mService.getMusicList();
                BaseActivity.this.onServiceConnected();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unregisterClient(mClient);
                mService = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    protected abstract void onServiceConnected();

    protected IMusicClient.Stub mClient = new IMusicClient.Stub() {
        @Override
        public void update() throws RemoteException {
            onDataChanged();
        }
    };

    protected abstract void onDataChanged();

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

    protected void bindService() {
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);
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

    @Override
    public void load(int index) {
        loadMusic(index);
    }

}
