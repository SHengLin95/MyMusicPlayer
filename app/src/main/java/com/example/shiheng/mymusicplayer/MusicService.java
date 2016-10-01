package com.example.shiheng.mymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;
import com.example.shiheng.mymusicplayer.utils.DBHelper;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;

import java.io.IOException;
import java.util.List;
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

    /**
     * 通知栏相关变量
     */
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mBigRemoteViews;
    private RemoteViews mSmallRemoteViews;
    private NotificationManager mManager;
    private static final int NOTIFY_ID = 123;

    private BroadcastReceiver mReceiver;
    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mClients = new RemoteCallbackList<>();
        mDbHelper = new DBHelper(this, null);
        mRandom = new Random();
        mReceiver = new MusicBroadcastReceiver();
        IntentFilter filter = new IntentFilter(MusicBroadcastReceiver.MUSIC_FILTER);
        initNotification();
        registerReceiver(mReceiver, filter);
    }


    private void initNotification() {
        //初始化视图
        mBigRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_big_music);
        mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_album, R.drawable.placeholder_disk_play_song);

        mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_cancel, R.drawable.note_btn_close_white);
        mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_prev, R.drawable.note_btn_pre_white);
        mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_play, R.drawable.note_btn_play_white);
        mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_next, R.drawable.note_btn_next_white);

        mBigRemoteViews.setTextViewText(R.id.notification_big_tv_title, "title");
        mBigRemoteViews.setTextColor(R.id.notification_big_tv_title, Color.BLACK);
        mBigRemoteViews.setTextViewText(R.id.notification_big_tv_artist, "artist");
        mBigRemoteViews.setTextColor(R.id.notification_big_tv_artist, Color.BLACK);

        mSmallRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_music);
        mSmallRemoteViews.setImageViewResource(R.id.notification_iv_album, R.drawable.placeholder_disk_play_song);
        mSmallRemoteViews.setImageViewResource(R.id.notification_iv_cancel, R.drawable.note_btn_close_white);
        mSmallRemoteViews.setImageViewResource(R.id.notification_iv_prev, R.drawable.note_btn_pre_white);
        mSmallRemoteViews.setImageViewResource(R.id.notification_iv_play, R.drawable.note_btn_play_white);
        mSmallRemoteViews.setImageViewResource(R.id.notification_iv_next, R.drawable.note_btn_next_white);

        mSmallRemoteViews.setTextViewText(R.id.notification_tv_title, "title");
        mSmallRemoteViews.setTextColor(R.id.notification_tv_title, Color.BLACK);
        mSmallRemoteViews.setTextViewText(R.id.notification_tv_artist, "artist");
        mSmallRemoteViews.setTextColor(R.id.notification_tv_artist, Color.BLACK);

        //初始化按键事件
        Intent buttonIntent = new Intent(MusicBroadcastReceiver.MUSIC_FILTER);

        buttonIntent.putExtra(MusicBroadcastReceiver.MUSIC_ACTION_TAG, MusicBroadcastReceiver.MUSIC_ACTION_CANCEL);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBigRemoteViews.setOnClickPendingIntent(R.id.notification_big_iv_cancel, cancelIntent);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.notification_iv_cancel, cancelIntent);

        buttonIntent.putExtra(MusicBroadcastReceiver.MUSIC_ACTION_TAG, MusicBroadcastReceiver.MUSIC_ACTION_PREV);
        PendingIntent prevIntent = PendingIntent.getBroadcast(this, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBigRemoteViews.setOnClickPendingIntent(R.id.notification_big_iv_prev, prevIntent);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.notification_iv_prev, prevIntent);

        buttonIntent.putExtra(MusicBroadcastReceiver.MUSIC_ACTION_TAG, MusicBroadcastReceiver.MUSIC_ACTION_PLAY);
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBigRemoteViews.setOnClickPendingIntent(R.id.notification_big_iv_play, playIntent);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.notification_iv_play, playIntent);

        buttonIntent.putExtra(MusicBroadcastReceiver.MUSIC_ACTION_TAG, MusicBroadcastReceiver.MUSIC_ACTION_NEXT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBigRemoteViews.setOnClickPendingIntent(R.id.notification_big_iv_next, nextIntent);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.notification_iv_next, nextIntent);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher)
                .setContent(mSmallRemoteViews)
                .setCustomBigContentView(mBigRemoteViews)
                .setPriority(Notification.PRIORITY_HIGH);

        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = mBuilder.build();
//        mManager.notify(NOTIFY_ID, notification);
//        startForeground(NOTIFY_ID, notification);

    }

    private void updateNotification() {
        Music music = playList.get(currentIndex);

        mBigRemoteViews.setTextViewText(R.id.notification_big_tv_title, music.getTitle());
        mBigRemoteViews.setTextViewText(R.id.notification_big_tv_artist,
                music.getArtist() + " - " + music.getArtist());
        Bitmap bitmap = MediaUtil.getAlbumImage(this, music.getAlbumId(), 128, 128);
        if (bitmap != null) {
            mBigRemoteViews.setImageViewBitmap(R.id.notification_big_iv_album, bitmap);
        } else {
            mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_album, R.drawable.placeholder_disk_play_song);
        }
        if (isPlaying) {
            mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_play, R.drawable.note_btn_pause_white);
        } else {
            mBigRemoteViews.setImageViewResource(R.id.notification_big_iv_play, R.drawable.note_btn_play_white);

        }

        mSmallRemoteViews.setTextViewText(R.id.notification_tv_title, music.getTitle());
        mSmallRemoteViews.setTextViewText(R.id.notification_tv_artist,
                music.getArtist() + " - " + music.getArtist());
        Bitmap bitmap1 = MediaUtil.getAlbumImage(this, music.getAlbumId(), 64, 64);
        if (bitmap1 != null) {
            mSmallRemoteViews.setImageViewBitmap(R.id.notification_big_iv_album, bitmap1);
        } else {
            mSmallRemoteViews.setImageViewResource(R.id.notification_big_iv_album, R.drawable.placeholder_disk_play_song);
        }
        if (isPlaying) {
            mSmallRemoteViews.setImageViewResource(R.id.notification_iv_play, R.drawable.note_btn_pause_white);
        } else {
            mSmallRemoteViews.setImageViewResource(R.id.notification_iv_play, R.drawable.note_btn_play_white);
        }

        Notification notification = mBuilder.build();
        mManager.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
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
        unregisterReceiver(mReceiver);
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
        isPlaying = false;
        notifyDataChange();
    }

    private void notifyDataChange() {
//        if (mClients.size() == 0) {
//            return;
//        }
        updateNotification();
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

    public class MusicBroadcastReceiver extends BroadcastReceiver {
        public static final String MUSIC_FILTER = "com.example.shiheng.MusicBroadcastReceiver";
        public static final String MUSIC_ACTION_TAG = "MusicAction";
        public static final int MUSIC_ACTION_PREV = 0;
        public static final int MUSIC_ACTION_PLAY = 1;
        public static final int MUSIC_ACTION_NEXT = 2;
        public static final int MUSIC_ACTION_CANCEL = 3;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(MUSIC_ACTION_TAG, -1)) {
                case MUSIC_ACTION_PREV:
                    loadMusic(PREVIOUS_INDEX_MARK);
                    break;
                case MUSIC_ACTION_PLAY:
                    play();
                    break;
                case MUSIC_ACTION_NEXT:
                    loadMusic(NEXT_INDEX_MARK);
                    break;
                case MUSIC_ACTION_CANCEL:
                    cancel();
                    break;
            }
        }
    }

    private void cancel() {
        stop();
        stopForeground(true);
    }


}
