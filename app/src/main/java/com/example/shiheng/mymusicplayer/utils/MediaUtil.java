package com.example.shiheng.mymusicplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MediaUtil {
    //获取专辑封面的Uri
    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");

    public static Bitmap getAlbumImage(Context context, int albumId, int reqWidth, int reqHeight) {
        Log.d("tag", "albumId: " + albumId);
        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = resolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            is = resolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is, null, options);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = height / 2;
            while (halfHeight / inSampleSize > reqHeight ||
                    halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

//    public static Bitmap decodeSampledBitmapFromResource(InputStream is,
//                                                         int reqWidth, int reqHeight) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(is, null, options);
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        options.inJustDecodeBounds = false;
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        return BitmapFactory.decodeStream(is, null, options);
//    }
}
