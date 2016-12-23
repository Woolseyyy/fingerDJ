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

    final long NULL_POSITION_RECORD = -1;
    private AudioHandler audioHandler;
    private long positionRecorder = NULL_POSITION_RECORD;

    @Override
    public void onCreate(){
        audioHandler = new AudioHandler(this.getResources());
        super.onCreate();
    }

    void setMainMusic(Uri fileUri){
        File music = new File(fileUri.getPath());
        //File music = new File(FileUtils.getPath(this, fileUri));
        audioHandler.setMainMusic(music);
    }

    void setBackMusic(Uri fileUri){
        File music = new File(fileUri.getPath());
        //File music = new File(FileUtils.getPath(this, fileUri));
        audioHandler.setBackMusic(music);
    }

    void start(){
        //init
        positionRecorder = NULL_POSITION_RECORD;

        //start playing
        audioHandler.start();
    }

    void stop(){
        audioHandler.stop();
    }

    boolean mainMusicPlay(){
        if(audioHandler.getState()){//if is playing
            audioHandler.mainMusicPlay();
            return true;
        }
        else {
            return false;
        }
    }

    boolean mainMusicPause(){
        if(audioHandler.getState()){//if is playing
            audioHandler.mainMusicPause();
            return true;
        }
        else {
            return false;
        }
    }

    void addRecord(){
        positionRecorder = audioHandler.getPosition();
    }

    void backRecord(){
        if(positionRecorder != NULL_POSITION_RECORD){
            audioHandler.setPosition(positionRecorder);
        }
    }

    void setEffectSound(int num){
        if(num>-1 && num<audioHandler.EFFECT_NUM){
            audioHandler.setEffectSound(num);
        }
    }

    void setMainVolume(int progress, int max){
        float volume = (float) progress / max * audioHandler.MAX_VOLUME;
        audioHandler.setMainMusicVolume(volume);
    }

    void setBackVolume(int progress, int max){
        float volume = (float) progress / max * audioHandler.MAX_VOLUME;
        audioHandler.setBackMusicVolume(volume);
    }
}
