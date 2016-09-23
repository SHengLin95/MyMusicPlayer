package com.example.shiheng.mymusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    int id;
    String title;
    String album;
    String artist;
    String path;
    long duration;
    int size;
    int albumId;
    int artistId;


    protected Music(Parcel in) {
        readFromParcel(in);
    }


    public Music() {

    }

    public int getArtistId() {
        return artistId;
    }
    public int getAlbumId() {
        return albumId;
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

    public long getDuration() {
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
        dest.writeLong(duration);
        dest.writeInt(size);
        dest.writeInt(albumId);
        dest.writeInt(artistId);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        path = in.readString();
        duration = in.readLong();
        size = in.readInt();
        albumId = in.readInt();
        artistId = in.readInt();
    }
}
