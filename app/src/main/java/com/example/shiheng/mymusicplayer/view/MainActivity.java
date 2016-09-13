package com.example.shiheng.mymusicplayer.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shiheng.mymusicplayer.IMusicClient;
import com.example.shiheng.mymusicplayer.IMusicControl;
import com.example.shiheng.mymusicplayer.IMusicController;
import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements IMusicController {
    private static final String TAG = "MainActivity";
    private static final int UI_UPDATE = 1;
    private static final int MY_EXTERNAL_STORAGE_PERMISSION_CODE = 123;
    private static final String IS_PLAYING = "MainActivity.isPlaying";
    private static final String MUSIC_INDEX = "MainActivity.MusicIndex";

    private MusicControlFragment mControlFragment;

    private List<Music> mMusicList;
    private IMusicControl mService;

    private UIHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mHandler = new UIHandler();

        //获取存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                initFragment();
            } else {
                showPermissionDialog();
            }
        } else {
            initFragment();
        }

        startService(new Intent(this, MusicService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mService != null) {
                mService.unregisterClient(mClient);
                unbindService(mConnection);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initFragment() {
        //初始化View
        FragmentManager fragmentManager = getSupportFragmentManager();
        mControlFragment = (MusicControlFragment) fragmentManager.findFragmentById(R.id.main_music_control);
        mControlFragment.setMusicController(this);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MusicListFragment fragment = new MusicListFragment();
        fragment.setMusicController(this);

        transaction.replace(R.id.main_ll, fragment);
        transaction.commit();
    }


    // ---------------------------------------------------------------------------------
    // 获取存储权限
    // ---------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initFragment();
            } else {
                Toast.makeText(this, "无法获得权限,请重试!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("请求权限")
                .setMessage("本应用需要存储权限,用于读取存储器上的音乐文件!")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_EXTERNAL_STORAGE_PERMISSION_CODE
                        );
                    }
                }).create();
        dialog.show();
    }

    // ---------------------------------------------------------------------------------
    // 用于音乐控制的接口
    // ---------------------------------------------------------------------------------

    @Override
    public void next() {
        loadMusic(MusicService.NEXT_INDEX_MARK);
    }

    @Override
    public void previous() {
        loadMusic(MusicService.PREVIOUS_INDEX_MARK);
    }


    @Override
    public void setMusicList(List<Music> musicList) {
        mMusicList = musicList;
        //绑定服务
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void play() {
        try {
            mService.play();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(int index) {
        loadMusic(index);
    }

    private void updateInformation(int index, boolean isPlaying) {
        Music music = mMusicList.get(index);
        mControlFragment.updateInformation(music.getTitle(), music.getArtist(), isPlaying);
    }


    // ---------------------------------------------------------------------------------
    // 与service通信相关的接口
    // ---------------------------------------------------------------------------------


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnect", "onServiceConnected");
            mService = IMusicControl.Stub.asInterface(service);
            try {
                mService.registerClient(mClient);
                mService.setMusicList(mMusicList);
                loadMusic(0, true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unregisterClient(mClient);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private void loadMusic(int index) {
        loadMusic(index, false);
    }

    private void loadMusic(int index, boolean preLoad) {
        try {
            mService.load(index, preLoad);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private IMusicClient.Stub mClient = new IMusicClient.Stub() {
        @Override
        public void update(int index, boolean isPlaying) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = UI_UPDATE;
            Bundle bundle = msg.getData();
            bundle.putBoolean(IS_PLAYING, isPlaying);
            bundle.putInt(MUSIC_INDEX, index);
            mHandler.sendMessage(msg);
        }
    };

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UI_UPDATE:
                    Bundle bundle = msg.getData();
                    updateInformation(bundle.getInt(MUSIC_INDEX), bundle.getBoolean(IS_PLAYING));
                    break;
            }
        }
    }
}
