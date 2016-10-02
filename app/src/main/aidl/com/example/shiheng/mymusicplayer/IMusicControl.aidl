// IMusicControl.aidl
package com.example.shiheng.mymusicplayer;

// Declare any non-default types here with import statements
import com.example.shiheng.mymusicplayer.model.Music;
import com.example.shiheng.mymusicplayer.IMusicClient;
interface IMusicControl {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    List<Music> getMusicList();
    List<Music> getAllMusic();
    int getCurMediaPosition();
    void setCurMediaPosition(in int position);
    void updateMusicList(in List<Music> musicList);
    void play();
    void pause();
    void load(in int index);
    int getCurIndex();
    void registerClient(IMusicClient client);
    void unregisterClient(IMusicClient client);
    boolean isPlaying();
    boolean isRunning();
    int getMusicMode();
    void setMusicMode(in int mode);
}
