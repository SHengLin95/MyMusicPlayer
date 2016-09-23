package com.example.shiheng.mymusicplayer.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;

public class MusicActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "example.MusicActivity";
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private TextView mCurTimeTextView;
    private TextView mCountTimeTextView;
    private SeekBar mSeekBar;
    private UIHandler mHandler;

    private ImageView mPlayImageView;
    private ImageView mAlbumImageView;
    private static final int UI_UPDATE = 0;
    private static final int TIME_UPDATE = 1;
    private boolean isPlaying = false;
    private boolean isSeeking = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mHandler = new UIHandler();
        bindService();

        mTitleTextView = (TextView) findViewById(R.id.music_title);
        mArtistTextView = (TextView) findViewById(R.id.music_artist);
        mPlayImageView = (ImageView) findViewById(R.id.music_play_iv);
        mAlbumImageView = (ImageView) findViewById(R.id.music_album_iv);
        mCurTimeTextView = (TextView) findViewById(R.id.music_time_cur);
        mCountTimeTextView = (TextView) findViewById(R.id.music_time_count);
        mSeekBar = (SeekBar) findViewById(R.id.music_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
    }


    @Override
    protected void onServiceConnected() {
        mHandler.sendEmptyMessage(UI_UPDATE);
    }

    @Override
    protected void onDataChanged() {
        mHandler.sendEmptyMessage(UI_UPDATE);
    }

    public void onMusicItemClick(View view) {
        switch (view.getId()) {
            case R.id.music_back_button:
                finish();
                break;
            case R.id.music_favorite_iv:
                break;
            case R.id.music_previous_iv:
                previous();
                break;
            case R.id.music_play_iv:
                play();
                break;
            case R.id.music_next_iv:
                next();
                break;
            case R.id.music_mode_iv:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCurTimeTextView.setText(MediaUtil.formatTime(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mService.setCurMediaPosition(seekBar.getProgress());
            new Timer().run();
            isSeeking = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_UPDATE:
                    updateInformation();
                case TIME_UPDATE:
                    if (isPlaying && !isSeeking)
                        updateTime();
                    break;
            }
        }
    }

    private void updateTime() {
        int curTime = 0;
        try {
            curTime = mService.getCurMediaPosition();
            mSeekBar.setProgress(curTime);
            mCurTimeTextView.setText(MediaUtil.formatTime(curTime));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        new Timer().run();
    }

    private void updateInformation() {
        try {
            Music music = mMusicList.get(mService.getCurIndex());
            mTitleTextView.setText(music.getTitle());
            mArtistTextView.setText(music.getArtist());
            isPlaying = mService.isPlaying();
            togglePlayImage();

            long duration = music.getDuration();
            mCountTimeTextView.setText(MediaUtil.formatTime(duration));
            mSeekBar.setMax((int) duration);


            Bitmap album = MediaUtil.getAlbumImage(this, music.getAlbumId(),
                    mAlbumImageView.getWidth(), mAlbumImageView.getHeight());
            if (album != null) {
                mAlbumImageView.setImageBitmap(album);
            } else {
                mAlbumImageView.setImageResource(R.drawable.placeholder_disk_play_program);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void togglePlayImage() {

        if (isPlaying) {
            mPlayImageView.setImageResource(R.drawable.desk_pause);
        } else {
            mPlayImageView.setImageResource(R.drawable.desk_play);
        }

    }

    private class Timer extends Thread {
        @Override
        public void run() {
            //补偿时间误差
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.sendEmptyMessageAtTime(TIME_UPDATE, next);
        }
    }


}
