package com.example.shiheng.mymusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.shiheng.mymusicplayer.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "MusicService";

    public static final String MUSIC_INIT = "MyMusicPlayer.Init";
    public static final String MUSIC_PLAY = "MyMusicPlayer.Play";
    public static final String MUSIC_STOP = "MyMusicPlayer.Stop";
    public static final String MUSIC_LOAD = "MyMusicPlayer.Load";
    public static final String MUSIC_STOP_SERVICE = "MyMusicPlayer.StopService";

    public static final String MUSIC_INDEX_KEY = "MyMusicPlayer.MusicIndex";

    private int currentIndex = -1;
    private boolean isPreLoad = true;
    private MediaPlayer mMediaPlayer;
    private List<Music> playList;
    private boolean isPlaying = false;

    private List<IMusicClient> mClients;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mClients = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!isPreLoad) {
            mMediaPlayer.start();
            isPlaying = true;
        }
        notifyDataChange();
    }

    public void loadMusic(int index) {
        if (index == -1) {
            return;
        }
        if (playList == null || playList.size() == 0) {
            return;
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playList.get(index).getPath());
            mMediaPlayer.prepareAsync();
            currentIndex = index;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (isPlaying) {
            pause();
            isPlaying = false;
        } else {
            start();
            isPlaying = true;
        }
        notifyDataChange();
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    private void notifyDataChange() {
        if (mClients.size() == 0) {
            return;
        }
        try {
            for (IMusicClient client : mClients) {
                client.update(currentIndex, isPlaying);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final IMusicControl.Stub mBinder = new IMusicControl.Stub() {
        @Override
        public void setMusicList(List<Music> musicList) throws RemoteException {
            playList = musicList;
        }

        @Override
        public void play() throws RemoteException {
            MusicService.this.play();
        }

        @Override
        public void pause() throws RemoteException {
            MusicService.this.pause();
        }

        @Override
        public void load(int index, boolean preLoad) throws RemoteException {
            if (currentIndex == index) {
                MusicService.this.play();
            } else {
                MusicService.this.loadMusic(index);
                isPreLoad = preLoad;
            }
        }

        @Override
        public int getCurIndex(int index) throws RemoteException {
            return index;
        }

        @Override
        public void registerClient(IMusicClient client) throws RemoteException {
            mClients.add(client);
        }

        @Override
        public void unregisterClient(IMusicClient client) throws RemoteException {
            mClients.remove(client);
        }
    };
}
