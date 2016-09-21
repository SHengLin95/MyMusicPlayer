package com.example.shiheng.mymusicplayer.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;

public class MusicActivity extends BaseActivity {
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private UIHandler mHandler;

    private ImageView mPlayImageView;
    private ImageView mAlbumImageView;
    private static final int UI_UPDATE = 0;

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

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            updateInformation();
        }
    }

    private void updateInformation() {
        try {
            Music music = mMusicList.get(mService.getCurIndex());
            mTitleTextView.setText(music.getTitle());
            mArtistTextView.setText(music.getArtist());
            togglePlayImage(mService.isPlaying());
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

    private void togglePlayImage(boolean isPlaying) {

        if (isPlaying) {
            mPlayImageView.setImageResource(R.drawable.desk_pause);
        } else {
            mPlayImageView.setImageResource(R.drawable.desk_play);
        }

    }


}
