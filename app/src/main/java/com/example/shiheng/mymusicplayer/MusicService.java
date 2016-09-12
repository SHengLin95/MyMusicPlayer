package com.example.shiheng.mymusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    public static final int MUSIC_START = 0;
    public static final int MUSIC_STOP = 1;
    public static final int MUSIC_PAUSE = 2;
    public static final int MUSIC_LOAD = 3;

    public static final String MUSIC_PATH_KEY = "MyMusicPlayer.MusicPath";

    private boolean isFirstIn = true;
    private MediaPlayer mMediaPlayer;
    private final Messenger messenger = new Messenger(new MusicHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }


    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
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
        if (isFirstIn) {
            isFirstIn = false;
        } else {
            mMediaPlayer.start();
        }
    }

    public void loadMusic(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public class MusicHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MUSIC_START:
                    start();
                    break;
                case MUSIC_STOP:
                    stop();
                    break;
                case MUSIC_PAUSE:
                    pause();
                    break;
                case MUSIC_LOAD:
                    loadMusic(msg.getData().getString(MUSIC_PATH_KEY));
                    break;
            }
        }
    }
}
