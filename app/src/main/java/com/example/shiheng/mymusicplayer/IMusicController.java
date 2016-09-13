package com.example.shiheng.mymusicplayer;

import com.example.shiheng.mymusicplayer.model.Music;

import java.util.List;

public interface IMusicController {
    void next();

    void previous();

    void play();


    void setMusicList(List<Music> musicList);

    void load(int index);
}
