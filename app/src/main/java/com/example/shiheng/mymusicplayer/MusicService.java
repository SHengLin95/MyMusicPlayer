package com.example.shiheng.mymusicplayer;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;
import com.example.shiheng.mymusicplayer.utils.DBHelper;
import com.example.shiheng.mymusicplayer.view.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MusicTask.onFinishListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "MusicService";

    public static final int PREVIOUS_INDEX_MARK = -3;
    public static final int NEXT_INDEX_MARK = -4;

    private int currentIndex = -1;
    //预加载标志位,一般在首次打开应用时作用
    private boolean isPreLoad = true;
    private MediaPlayer mMediaPlayer;
    private List<Music> playList;
    private boolean isPlaying = false;
    private RemoteCallbackList<IMusicClient> mClients;

    private DBHelper mDbHelper;

    private Random mRandom;
    public int mMusicMode = ORDER_PLAY;
    public static final int ORDER_PLAY = 0;
    public static final int RANDOM = 1;
    public static final int SINGLE_CYCLE = 2;
    public static final int[] MUSIC_MODE = {
            ORDER_PLAY, RANDOM, SINGLE_CYCLE
    };


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mClients = new RemoteCallbackList<>();
        mDbHelper = new DBHelper(this, null);
        mRandom = new Random();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MusicTask musicTask = new MusicTask(this);
        musicTask.setOnFinishListener(this);
        musicTask.execute();
        return mBinder;
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
        } else {
            isPreLoad = false;
        }
        notifyDataChange();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mMusicMode == SINGLE_CYCLE) {
            mp.stop();
            mp.prepareAsync();
        } else {
            loadMusic(NEXT_INDEX_MARK);
        }
    }

    public void loadMusic(int index) {
        if (index == -1) {
            return;
        }
        if (playList == null || playList.size() == 0) {
            return;
        }

        switch (index) {
            case PREVIOUS_INDEX_MARK:
                if (mMusicMode != RANDOM) {
                    index = currentIndex;
                    if (--index < 0) {
                        index = playList.size() - 1;
                    }
                    break;
                }
            case NEXT_INDEX_MARK:
                if (mMusicMode != RANDOM) {
                    index = currentIndex;
                    if (++index == playList.size()) {
                        index = 0;
                    }
                } else {
                    index = mRandom.nextInt(playList.size());
                }
                break;
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
//        if (mClients.size() == 0) {
//            return;
//        }

        int len = mClients.beginBroadcast();
        try {
            for (int i = 0; i < len; i++) {
                mClients.getBroadcastItem(i).update();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mClients.finishBroadcast();


    }

    private final IMusicControl.Stub mBinder = new IMusicControl.Stub() {
        @Override
        public List<Music> getMusicList() throws RemoteException {
            return playList;
        }

        @Override
        public int getCurMediaPosition() throws RemoteException {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public void setCurMediaPosition(int position) throws RemoteException {
            mMediaPlayer.seekTo(position);
        }

        @Override
        public void updateMusicList(List<Music> musicList, int index) throws RemoteException {
            playList = musicList;
            currentIndex = -1;
            load(index);
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
        public void load(int index) throws RemoteException {
            if (currentIndex == index) {
                MusicService.this.play();
            } else {
                MusicService.this.loadMusic(index);
            }
        }

        @Override
        public int getCurIndex() throws RemoteException {
            return currentIndex;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return isPlaying;
        }

        @Override
        public int getMusicMode() throws RemoteException {
            return mMusicMode;
        }

        @Override
        public void setMusicMode(int mode) throws RemoteException {
            if (mode < 3) {
                mMusicMode = mode;
            }
        }

        @Override
        public void registerClient(IMusicClient client) throws RemoteException {
            mClients.register(client);
//            Log.d(TAG, "registerClient: " + mClients.indexOf(client));
        }

        @Override
        public void unregisterClient(IMusicClient client) throws RemoteException {
            mClients.unregister(client);
        }

    };


    @Override
    public void onFinish(List<Music> musics) {
        playList = musics;
        loadMusic(0);
        notifyDataChange();
        new DBThread().run();
    }

    private class DBThread extends Thread {

        @Override
        public void run() {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.rawQuery("delete from " + DBHelper.TABLE_NAME, null);
            for (int i = 0; i < playList.size(); i++) {
                db.insert(DBHelper.TABLE_NAME, null, music2ContentValues(playList.get(i)));
            }
        }

        private ContentValues music2ContentValues(Music music) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media._ID, music.getId());
            contentValues.put(MediaStore.Audio.Media.TITLE, music.getTitle());
            contentValues.put(MediaStore.Audio.Media.ALBUM, music.getAlbum());
            contentValues.put(MediaStore.Audio.Media.ARTIST, music.getArtist());
            contentValues.put(MediaStore.Audio.Media.DATA, music.getPath());
            contentValues.put(MediaStore.Audio.Media.DURATION, music.getDuration());
            contentValues.put(MediaStore.Audio.Media.SIZE, music.getSize());
            contentValues.put(MediaStore.Audio.Media.ALBUM_ID, music.getAlbumId());
            contentValues.put(MediaStore.Audio.Media.ARTIST_ID, music.getArtistId());
            return contentValues;
        }
    }


}
