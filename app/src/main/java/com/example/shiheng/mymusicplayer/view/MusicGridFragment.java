package com.example.shiheng.mymusicplayer.view;

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
import android.widget.GridView;

import com.example.shiheng.mymusicplayer.MusicGridAdapter;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.GridItem;
import com.example.shiheng.mymusicplayer.utils.DBHelper;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;

import java.util.ArrayList;
import java.util.List;


public class MusicGridFragment extends Fragment {
    private GridView mGridView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid, null);
        mGridView = (GridView) view.findViewById(R.id.fragment_grid_gv);
        new GridItemTask().execute();
        return view;
    }

    private class GridItemTask extends AsyncTask<Void, Void, List<GridItem>> {
        private static final String SQL_SEARCHER_ARTIST = "select distinct " + MediaStore.Audio.Media.ARTIST + ","
                + MediaStore.Audio.Media.ARTIST_ID + " from " + DBHelper.TABLE_NAME + ";";
        private DBHelper dbHelper;

        public GridItemTask() {
            dbHelper = new DBHelper(getContext(), null);
        }

        @Override
        protected List<GridItem> doInBackground(Void... params) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return searchArtist(db);
        }

        @Override
        protected void onPostExecute(List<GridItem> gridItems) {
            MusicGridAdapter adapter = new MusicGridAdapter(getContext(), gridItems);
            mGridView.setAdapter(adapter);
        }

        private List<GridItem> searchArtist(SQLiteDatabase db) {
            List<GridItem> items = new ArrayList<>();
            Cursor cursor = db.rawQuery(SQL_SEARCHER_ARTIST, null);
            while (cursor.moveToNext()) {
                GridItem gridItem = new GridItem();
                gridItem.setTitle(cursor.getString(0));
                gridItem.setAlbum(getImageByArtistID(db, cursor.getInt(1)));
                items.add(gridItem);
            }
            cursor.close();
            return items;
        }

        private Bitmap getImageByArtistID(SQLiteDatabase db, int artistId) {
            Cursor cursor = db.query(DBHelper.TABLE_NAME,
                    new String[]{MediaStore.Audio.Media.ALBUM_ID},
                    MediaStore.Audio.Media.ARTIST_ID + "=?",
                    new String[]{String.valueOf(artistId)}, null, null, null);
            if (cursor.moveToFirst()) {
                return MediaUtil.getAlbumImage(getContext(), cursor.getInt(0), 200, 200);
            }
            return null;
        }
    }
}
