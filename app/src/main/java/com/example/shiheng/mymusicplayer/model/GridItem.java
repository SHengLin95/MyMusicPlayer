package com.example.shiheng.mymusicplayer.model;

import android.graphics.Bitmap;


public class GridItem {
    String title;
    Bitmap album;

    public Bitmap getAlbum() {
        return album;
    }

    public void setAlbum(Bitmap album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
