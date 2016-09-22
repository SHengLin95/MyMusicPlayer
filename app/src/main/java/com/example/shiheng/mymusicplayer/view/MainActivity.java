package com.example.shiheng.mymusicplayer.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int UI_UPDATE = 1;
    private static final int INIT_FRAGMENT = 2;


    public static final String MUSIC_LIST = "MainActivity.MusicIndex";
    private int mCurrentFragmentIndex = 0;
    private FragmentManager mFragmentManager;
    private MusicControlFragment mControlFragment;
    private MusicListFragment mMusicListFragment;
    private MusicGridFragment mGridFragment;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;


    private UIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new UIHandler();

        bindService();

        initView();

        mFragmentManager = getSupportFragmentManager();
        mControlFragment = (MusicControlFragment) mFragmentManager.findFragmentById(R.id.main_music_control);
        mControlFragment.setMusicController(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void initView() {
        //初始化toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initFragment() {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        mMusicListFragment = new MusicListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MUSIC_LIST, (ArrayList) mMusicList);
        mMusicListFragment.setArguments(bundle);
        mMusicListFragment.setMusicController(this);
        transaction.replace(R.id.main_ll, mMusicListFragment);
        transaction.commit();

    }


    private void updateInformation() {
        try {
            int index = mService.getCurIndex();
            Music music = mMusicList.get(index);

            mControlFragment.updateInformation(music, mService.isPlaying());
            if (mMusicListFragment != null) {
                mMusicListFragment.updateList(index);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------------------------
    // BaseActivity相关的接口
    // ---------------------------------------------------------------------------------

    @Override
    protected void onServiceConnected() {
        Message msg = Message.obtain();
        msg.what = INIT_FRAGMENT;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onDataChanged() {
        Message msg = Message.obtain();
        msg.what = UI_UPDATE;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_all_music:
                if (mCurrentFragmentIndex != 0) {
                    loadListFragment();
                    mCurrentFragmentIndex = 0;
                }
                break;
            case R.id.nav_artist:
                if (mCurrentFragmentIndex != 1) {
                    setGridFragment(MusicGridFragment.ARTIST_FLAG);
                    mCurrentFragmentIndex = 1;
                }
                break;
            case R.id.nav_album:
                if (mCurrentFragmentIndex != 2) {
                    setGridFragment(MusicGridFragment.ALBUM_FLAG);
                    mCurrentFragmentIndex = 2;
                }
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadListFragment() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.show(mMusicListFragment);
        transaction.remove(mGridFragment);
        mGridFragment = null;
        transaction.commit();
    }

    private void setGridFragment(int flag) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mGridFragment != null) {
            transaction.remove(mGridFragment);
        }
        mGridFragment = new MusicGridFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(MusicGridFragment.MUSIC_GRID_FRAGMENT_FLAG, flag);
        mGridFragment.setArguments(bundle);
        transaction.hide(mMusicListFragment);
        transaction.add(R.id.main_ll, mGridFragment);
        transaction.commit();
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_FRAGMENT:
                    initFragment();
//                    break;
                case UI_UPDATE:
                    updateInformation();
                    break;

            }
        }
    }


}
