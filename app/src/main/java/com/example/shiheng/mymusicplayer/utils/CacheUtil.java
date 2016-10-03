package com.example.shiheng.mymusicplayer.utils;


import android.graphics.Bitmap;
import android.util.LruCache;

public class CacheUtil {
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;
    private static LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }
    };


    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mLruCache.get(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemoryCache(String key) {
        return mLruCache.get(key);
    }


}
