package com.example.shiheng.mymusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    int id;
    String title;
    String album;
    String artist;
    String path;
    int duration;
    int size;

    protected Music(Parcel in) {
        readFromParcel(in);
    }

    public Music() {

    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public int getDuration() {
        return duration;
    }

    public int getSize() {
        return size;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(path);
        dest.writeInt(duration);
        dest.writeInt(size);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        path = in.readString();
        duration = in.readInt();
        size = in.readInt();
    }
}
