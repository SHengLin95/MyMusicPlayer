package com.example.shiheng.mymusicplayer.view;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.R;
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.model.MusicTask;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MusicControlFragment.onMusicControlListener, MusicTask.onFinishListener {
    private static final int MY_EXTERNAL_STORAGE_PERMISSION_CODE = 123;

    private MusicControlFragment mControlFragment;
    private ListView mListView;
    private MusicService mMusicService;
    private MusicTask mMusicTask;
    private List<Music> mMusicList;
    private boolean isPlaying = false;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化View
        FragmentManager fragmentManager = getSupportFragmentManager();
        mControlFragment = (MusicControlFragment) fragmentManager.findFragmentById(R.id.main_music_control);
        mControlFragment.setMusicControlListener(this);

        mListView = (ListView) findViewById(R.id.main_list);


        //获取存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                traversalAllMusic();
            } else {
                showPermissionDialog();
            }
        }
        //绑定播放服务
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false
        }
    }

    private void traversalAllMusic() {
        mMusicTask = new MusicTask(this, mListView);
        mMusicTask.setOnFinishListener(this);
        mMusicTask.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                traversalAllMusic();
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


    @Override
    public void next() {
        Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void previous() {
        Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void play() {
        if (isPlaying) {
            mMusicService.pause();
            isPlaying = false;
        } else {
            mMusicService.start();
            isPlaying = true;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnect", "onServiceConnected");
            mMusicService = ((MusicService.MyBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    public void onFinish() {
        mMusicList = mMusicTask.getMusicList();
        if (mMusicList.size() != 0) {
            loadMusic(mMusicList.get(0));
        } else {
            Toast.makeText(this, "无法读取音频文件!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMusic(Music music) {
        if (mBound) {
            mMusicService.loadMusic(music.getPath());
            mControlFragment.updateInformation(music.getTitle(), music.getArtist());
        }
    }

}
