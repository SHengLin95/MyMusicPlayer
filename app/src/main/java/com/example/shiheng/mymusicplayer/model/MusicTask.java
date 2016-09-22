package com.example.shiheng.mymusicplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ListView;

import com.example.shiheng.mymusicplayer.MusicAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicTask extends AsyncTask<Void, Void, List<Music>> {
    private Context context;
    private onFinishListener mListener;
//    private List<Music> musicList;
//    private MusicAdapter adapter;

    public MusicTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Music> doInBackground(Void... params) {
        List<Music> musicList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        int[] columnIndexes = new int[]{
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID),
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
        };
        while (cursor.moveToNext()) {
            if (cursor.getInt(columnIndexes[8]) != 1) {
                Music music = new Music();
                music.id = cursor.getInt(columnIndexes[0]);
                music.title = cursor.getString(columnIndexes[1]);
                music.album = cursor.getString(columnIndexes[2]);
                music.artist = cursor.getString(columnIndexes[3]);
                music.path = cursor.getString(columnIndexes[4]);
                music.duration = cursor.getInt(columnIndexes[5]);
                music.size = cursor.getInt(columnIndexes[6]);
                music.albumId = cursor.getInt(columnIndexes[7]);
                music.artistId = cursor.getInt(columnIndexes[8]);
                musicList.add(music);
            }
        }
        cursor.close();

        return musicList;
    }

    public void setOnFinishListener(onFinishListener listener) {
        mListener = listener;
    }

    @Override
    protected void onPostExecute(List<Music> musics) {
//        adapter = new MusicAdapter(context, musics);

        if (mListener != null) {
            mListener.onFinish(musics);
        }
    }


    public interface onFinishListener {
        void onFinish(List<Music> musics);
    }
}
