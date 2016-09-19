package com.example.shiheng.mymusicplayer.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.shiheng.mymusicplayer.MusicService;
import com.example.shiheng.mymusicplayer.R;

public class LogoActivity extends BaseActivity {
    private static final int MY_EXTERNAL_STORAGE_PERMISSION_CODE = 123;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        mHandler = new Handler();
        //获取存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                showPermissionDialog();
            }
        } else {
            startService();
        }
    }

    // ---------------------------------------------------------------------------------
    // 获取存储权限
    // ---------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService();
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
                        ActivityCompat.requestPermissions(LogoActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_EXTERNAL_STORAGE_PERMISSION_CODE
                        );
                    }
                }).create();
        dialog.show();
    }

    private void startService() {
//        startService(new Intent(this, MusicService.class));
        bindService(new Intent(this, MusicService.class), mConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onServiceConnected() {

    }

    @Override
    protected void onDataChanged(int index, boolean isPlaying) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("LogoActivity", "run");
                    startActivity(new Intent(LogoActivity.this, MainActivity.class));
                    LogoActivity.this.finish();
                    mService.unregisterClient(mClient);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
