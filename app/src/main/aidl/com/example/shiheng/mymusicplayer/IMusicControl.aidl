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
    int getCurMediaPosition();
    void setCurMediaPosition(in int position);
    void updateMusicList(in List<Music> musicList, in int index);
    void play();
    void pause();
    void load(in int index);
    int getCurIndex();
    void registerClient(IMusicClient client);
    void unregisterClient(IMusicClient client);
    boolean isPlaying();
    int getMusicMode();
    void setMusicMode(in int mode);
}
