package com.dankstudio.android.fingerdj;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

/**
 * Created by admin on 2016/12/22.
 */
public class MusicService extends Service {
    //connect using binder
    private final IBinder binder = new MyBinder();
    class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private AudioHandler audioHandler = new AudioHandler();

    void setMainMusic(Uri fileUri){
        File music = new File(fileUri.getPath());
        audioHandler.setMainMusic(music);
    }

    void setBackMusic(Uri fileUri){
        File music = new File(fileUri.getPath());
        audioHandler.setBackMusic(music);
    }

    void start(){
        audioHandler.start();
    }

    void mainMusicPause(){
        audioHandler.mainMusicPause();
    }
}
