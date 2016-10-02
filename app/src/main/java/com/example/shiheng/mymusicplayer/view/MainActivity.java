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
import android.view.Gravity;
import android.view.MenuItem;

import com.example.shiheng.mymusicplayer.IMusicUpdater;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, IMusicUpdater {
    private static final String TAG = "MainActivity";
    private static final int UI_UPDATE = 1;
    private static final int INIT_FRAGMENT = 2;
    private static final int DATA_UPDATE = 3;


    public static final String MUSIC_LIST = "MainActivity.MusicIndex";
    private FragmentManager mFragmentManager;
    private MusicControlFragment mControlFragment;
    private MusicListFragment mMusicListFragment;
    private MusicGridFragment mGridFragment;
    private Stack<Integer> mFragmentStack;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private List<Music> allMusicList;

    private UIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new UIHandler();

        bindService();

        initView();

        mFragmentStack = new Stack<>();
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
        allMusicList = mMusicList;
        bundle.putParcelableArrayList(MUSIC_LIST, (ArrayList<Music>) mMusicList);
        mMusicListFragment.setArguments(bundle);
        mMusicListFragment.setMusicController(this);
        transaction.replace(R.id.main_fl, mMusicListFragment);
        transaction.commit();
        mFragmentStack.push(0);
    }


    private void updateInformation() {
        try {
            int index = mService.getCurIndex();
            if (index != -1) {
                Music music = mMusicList.get(index);

                mControlFragment.updateInformation(music, mService.isPlaying());
                if (mMusicListFragment != null) {
                    mMusicListFragment.updateList(index);
                }
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
        int currentFragmentIndex = mFragmentStack.peek();
        switch (item.getItemId()) {
            case R.id.nav_all_music:
                if (currentFragmentIndex != 0) {
                    showList();
                } else {
                    if (mMusicList.size() != allMusicList.size()) {
                        mMusicList = allMusicList;
                        showList();
                    }
                }
                break;
            case R.id.nav_artist:
                if (currentFragmentIndex != 1) {
                    setGridFragment(MusicGridFragment.ARTIST_FLAG);
                }
                break;
            case R.id.nav_album:
                if (currentFragmentIndex != 2) {
                    setGridFragment(MusicGridFragment.ALBUM_FLAG);
                }
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ---------------------------------------------------------------------------------
    // Fragment相关的操作
    // ---------------------------------------------------------------------------------

    private void showList() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        MusicShowListFragment fragment = new MusicShowListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MUSIC_LIST, (ArrayList<Music>) mMusicList);
        fragment.setArguments(bundle);
        fragment.setMusicController(this);
        transaction.replace(R.id.main_fl, fragment);

        mFragmentStack.push(3);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setGridFragment(int flag) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mGridFragment != null) {
            transaction.remove(mGridFragment);
        }
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            popFragment();
        }
        mGridFragment = new MusicGridFragment();
        mGridFragment.setMusicUpdater(this);
        Bundle bundle = new Bundle();
        bundle.putInt(MusicGridFragment.MUSIC_GRID_FRAGMENT_FLAG, flag);
        mGridFragment.setArguments(bundle);
        transaction.replace(R.id.main_fl, mGridFragment);

        transaction.addToBackStack(null);
        mFragmentStack.push(flag);
        transaction.commit();
    }

    private void popFragment() {
        mFragmentManager.popBackStack();
        mFragmentStack.pop();
    }


    @Override
    public void update(List<Music> musicList) {
        mMusicList = musicList;
        mHandler.sendEmptyMessage(DATA_UPDATE);
    }

    @Override
    public void updateMusicList(List<Music> musicList) {
        super.updateMusicList(musicList);
        resetFragment(musicList);
    }

    private void resetFragment(List<Music> musicList) {
        while (mFragmentStack.size() > 1) {
            mFragmentManager.popBackStackImmediate();
            mFragmentStack.pop();
        }
        mMusicListFragment.updateData(musicList);
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
                case DATA_UPDATE:
                    showList();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        } else {
            mFragmentStack.pop();
            super.onBackPressed();
        }
    }
}
