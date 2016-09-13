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

    void setMusicList(in List<Music> musicList);
    void play();
    void pause();
    void load(in int index, in boolean preLoad);
    int getCurIndex();
    void registerClient(IMusicClient client);
    void unregisterClient(IMusicClient client);
}
