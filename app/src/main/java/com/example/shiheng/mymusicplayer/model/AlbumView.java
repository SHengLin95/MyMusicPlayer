package com.example.shiheng.mymusicplayer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.utils.MediaUtil;


public class AlbumView extends View {
    private Bitmap mDiskHaloBitmap;
    private Bitmap mDiskBitmap;
    private Bitmap mAlbumBitmap;
    private int mLeft;
    private int mTop;

    public AlbumView(Context context) {
        this(context, null);
    }

    public AlbumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDiskHaloBitmap = MediaUtil.getBitmapByResource(getResources(), R.drawable.play_disc_halo);
        mDiskBitmap = MediaUtil.getBitmapByResource(getResources(), R.drawable.play_disc);
        mAlbumBitmap = MediaUtil.getBitmapByResource(getResources(), R.drawable.placeholder_disk_play_song);
        mLeft = (mDiskBitmap.getWidth() - mAlbumBitmap.getWidth()) / 2;
        mTop = (mDiskBitmap.getHeight() - mAlbumBitmap.getHeight()) / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = mDiskHaloBitmap.getWidth() + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = mDiskHaloBitmap.getHeight() + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }

        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mAlbumBitmap, getPaddingLeft() + mLeft,
                getPaddingTop() + mTop, null);
        canvas.drawBitmap(mDiskHaloBitmap, getPaddingLeft(), getPaddingTop(), null);
        canvas.drawBitmap(mDiskBitmap, getPaddingLeft(), getPaddingTop(), null);
    }
}
