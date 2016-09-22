package com.example.shiheng.mymusicplayer.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;


public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Music.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "musics";

    private static final String DB_CREATE = "create table " + TABLE_NAME + " ( " +
            MediaStore.Audio.Media._ID + " integer primary key, " +
            MediaStore.Audio.Media.TITLE + " text not null, " +
            MediaStore.Audio.Media.ALBUM + " text not null, " +
            MediaStore.Audio.Media.ARTIST + " text not null, " +
            MediaStore.Audio.Media.DATA + " text not null, " +
            MediaStore.Audio.Media.DURATION + " integer not null, " +
            MediaStore.Audio.Media.SIZE + " integer not null, " +
            MediaStore.Audio.Media.ALBUM_ID + " integer not null, " +
            MediaStore.Audio.Media.ARTIST_ID + " integer not null);";

    public DBHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}
