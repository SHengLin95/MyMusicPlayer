package com.example.shiheng.mymusicplayer.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.R;

public class MusicControlFragment extends Fragment implements View.OnClickListener {
    private IMusicController mController;
    private ImageView mAlbumImageView;
    private ImageView mPrevImageView;
    private ImageView mNextImageView;
    private ImageView mPlayImageView;

    private TextView mTitleTextView;
    private TextView mArtistTextView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_control, null);

        mAlbumImageView = (ImageView) view.findViewById(R.id.music_control_album);

        mPrevImageView = (ImageView) view.findViewById(R.id.music_control_prev);
        mPrevImageView.setOnClickListener(this);
        mNextImageView = (ImageView) view.findViewById(R.id.music_control_next);
        mNextImageView.setOnClickListener(this);
        mPlayImageView = (ImageView) view.findViewById(R.id.music_control_play);
        mPlayImageView.setOnClickListener(this);

        mTitleTextView = (TextView) view.findViewById(R.id.music_control_title);
        mArtistTextView = (TextView) view.findViewById(R.id.music_control_singer);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (mController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.music_control_prev:
                mController.previous();
                break;
            case R.id.music_control_next:
                mController.next();
                break;
            case R.id.music_control_play:
                mController.play();
                break;
        }
    }

    public void updateInformation(String title, String artist, boolean isPlaying) {
        updateInformation(title, artist, isPlaying, null);
    }

    public void updateInformation(String title, String artist, boolean isPlaying, Bitmap album) {
        mTitleTextView.setText(title);
        mArtistTextView.setText(artist);
        toggleImage(isPlaying);
        if (album != null) {
            mAlbumImageView.setImageBitmap(album);
        } else {
            mAlbumImageView.setImageResource(R.drawable.ic_launcher);
        }
    }

    private void toggleImage(boolean isPlaying) {
        if (!isPlaying) {
            mPlayImageView.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        } else {
            mPlayImageView.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        }
    }

    public void setMusicController(IMusicController controller) {
        mController = controller;
    }


}
