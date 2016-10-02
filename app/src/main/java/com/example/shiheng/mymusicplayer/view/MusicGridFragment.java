package com.example.shiheng.mymusicplayer.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.shiheng.mymusicplayer.IMusicUpdater;
import com.example.shiheng.mymusicplayer.MusicGridAdapter;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.GridItem;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;
import com.example.shiheng.mymusicplayer.utils.DBHelper;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;

import java.util.ArrayList;
import java.util.List;


public class MusicGridFragment extends Fragment implements AdapterView.OnItemClickListener, MusicTask.onFinishListener {
    private GridView mGridView;
    public static final String MUSIC_GRID_FRAGMENT_FLAG = "MusicGridFlag";
    public static final int ARTIST_FLAG = 1;
    public static final int ALBUM_FLAG = 2;
    private int flag = -1;
    private Context mContext;
    private List<GridItem> mItems;
    private IMusicUpdater mMusicUpdater;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flag = getArguments().getInt(MUSIC_GRID_FRAGMENT_FLAG);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid, null);
        mGridView = (GridView) view.findViewById(R.id.fragment_grid_gv);
        mGridView.setOnItemClickListener(this);
        new GridItemTask().execute();
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GridItem item = mItems.get(position);
        String selection;
        if (flag == ARTIST_FLAG) {
            selection = MediaStore.Audio.Media.ARTIST_ID;
        } else {
            selection = MediaStore.Audio.Media.ALBUM_ID;
        }
        MusicTask musicTask = new MusicTask(mContext, selection + "=?",
                new String[]{String.valueOf(item.getId())});
        musicTask.setOnFinishListener(this);
        musicTask.execute();
    }

    @Override
    public void onFinish(List<Music> musics) {
        if (mMusicUpdater != null) {
            mMusicUpdater.update(musics);
        }
    }

    public void setMusicUpdater(IMusicUpdater updater) {
        mMusicUpdater = updater;
    }

    private class GridItemTask extends AsyncTask<Void, Void, List<GridItem>> {
        private DBHelper dbHelper;
        private static final int reqWidth = 100;
        private static final int reqHeight = 100;

        public GridItemTask() {
            dbHelper = new DBHelper(mContext, null);
        }

        @Override
        protected List<GridItem> doInBackground(Void... params) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            if (flag == ARTIST_FLAG) {
                return searchArtist(db);
            } else if (flag == ALBUM_FLAG) {
                return searchAlbum(db);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<GridItem> gridItems) {
            MusicGridAdapter adapter = new MusicGridAdapter(mContext, gridItems);
            mGridView.setAdapter(adapter);
        }

        private List<GridItem> searchAlbum(SQLiteDatabase db) {
            List<GridItem> items = new ArrayList<>();

            Cursor cursor = db.query(true, DBHelper.TABLE_NAME,
                    new String[]{MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID},
                    null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                GridItem gridItem = new GridItem();
                gridItem.setTitle(cursor.getString(0));
                gridItem.setId(cursor.getInt(1));
                gridItem.setAlbum(MediaUtil.getAlbumImage(mContext,
                        gridItem.getId(), reqWidth, reqHeight));
                items.add(gridItem);
            }

            cursor.close();
            mItems = items;
            return items;
        }

        private List<GridItem> searchArtist(SQLiteDatabase db) {
            List<GridItem> items = new ArrayList<>();

            Cursor cursor = db.query(true, DBHelper.TABLE_NAME,
                    new String[]{MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ARTIST_ID},
                    null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                GridItem gridItem = new GridItem();
                gridItem.setTitle(cursor.getString(0));
                gridItem.setId(cursor.getInt(1));
                gridItem.setAlbum(getImageByArtistID(db, gridItem.getId()));
                items.add(gridItem);
            }

            cursor.close();
            mItems = items;
            return items;
        }

        private Bitmap getImageByArtistID(SQLiteDatabase db, int artistId) {
            Cursor cursor = db.query(DBHelper.TABLE_NAME,
                    new String[]{MediaStore.Audio.Media.ALBUM_ID},
                    MediaStore.Audio.Media.ARTIST_ID + "=?",
                    new String[]{String.valueOf(artistId)}, null, null, null);
            Bitmap bitmap = null;
            while (cursor.moveToNext()) {
                bitmap = MediaUtil.getAlbumImage(mContext, cursor.getInt(0), reqWidth, reqHeight);
                if (bitmap != null) {
                    break;
                }
            }
            cursor.close();
            return bitmap;
        }
    }
}
