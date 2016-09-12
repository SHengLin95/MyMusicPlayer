package com.example.shiheng.mymusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    public static final String MUSIC_START = "MyMusicPlayer.Start";
    public static final String MUSIC_STOP = "MyMusicPlayer.Stop";
    public static final String MUSIC_PAUSE = "MyMusicPlayer.Pause";
    public static final String MUSIC_LOAD = "MyMusicPlayer.Load";
    public static final String MUSIC_STOP_SERVICE = "MyMusicPlayer.StopService";

    public static final String MUSIC_PATH_KEY = "MyMusicPlayer.MusicPath";

    private boolean isFirstIn = true;
    private MediaPlayer mMediaPlayer;
//    private final Messenger messenger = new Messenger(new MusicHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("tag", "onStartCommand: " + intent.getAction());
        switch (intent.getAction()) {
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
                loadMusic(intent.getStringExtra(MUSIC_PATH_KEY));
                break;
            case MUSIC_STOP_SERVICE:
                stopSelf();
                break;
        }
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

//    public class MusicHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MUSIC_START:
//                    start();
//                    break;
//                case MUSIC_STOP:
//                    stop();
//                    break;
//                case MUSIC_PAUSE:
//                    pause();
//                    break;
//                case MUSIC_LOAD:
//                    loadMusic(msg.getData().getString(MUSIC_PATH_KEY));
//                    break;
//                case MUSIC_STOP_SERVICE:
//                    stopSelf();
//                    break;
//            }
//        }
//
//    }

}
