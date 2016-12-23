package com.dankstudio.android.fingerdj;

/**
 * Created by admin on 2016/12/22.
 */

import java.io.*;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;

public class AudioHandler {
    private final int NO_EFFECT = -1;
    public final int EFFECT_NUM = 5;
    public final float MAX_VOLUME = 1;

    //media data
    private File mainMusicFile = null;
    private File backMusicFile = null;
    //private File mainMusicFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/netease/cloudmusic/Music/1.wav");
    //private File backMusicFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/netease/cloudmusic/Music/2.wav");
    private int effectResourceIDs[] = {R.raw.rub1, R.raw.drum, R.raw.check_it_out, R.raw.gong, R.raw.yeah1};
    private Resources res;
    private InputStream[] effectFile = null;

    //control signal
    private boolean run = false;
    private boolean mainMusicPlaying = false;
    private boolean backMusicPlaying = false;
    private int effectNumber = NO_EFFECT;
    private float mainMusicVolume = MAX_VOLUME;
    private float backMusicVolume = MAX_VOLUME;
    private long position = 44;

    //init
    public AudioHandler(Resources res){

        this.res = res;

    }

    //control interface
    //start audio
    public boolean setMainMusic(File music){
        if(!run){
            mainMusicFile = music;
            return true;
        }
        else{
            return false;
        }
    }
    public boolean setBackMusic(File music){
        if(!run){
            backMusicFile = music;
            return true;
        }
        else{
            return false;
        }
    }

    public boolean start(){
        if(mainMusicFile!=null && backMusicFile!=null){
            run = true;
            mainMusicPlaying = true;
            backMusicPlaying = true;
            PlayThread playThread = new PlayThread();
            playThread.start();
            return true;
        }else{
            return false;
        }
    }

    public void stop(){
        run = false;
        mainMusicPlaying = false;
        backMusicPlaying = false;
        effectNumber = NO_EFFECT;
    }

    public void mainMusicPause(){
        mainMusicPlaying = false;
    }

    public void mainMusicPlay(){
        mainMusicPlaying = true;
    }

    public void backMusicPause(){
        backMusicPlaying = false;
    }

    public void backMusicPlay(){
        backMusicPlaying = true;
    }

    public void setEffectSound(int num){
        if(num>-1 && num<EFFECT_NUM){

            if(effectNumber != NO_EFFECT){
                try {//reset the before effect
                    effectFile[effectNumber] = res.openRawResourceFd(effectResourceIDs[effectNumber]).createInputStream();
                    effectFile[effectNumber].skip(44);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            effectNumber = num;
        }
    }

    public void setMainMusicVolume(float volume){
        mainMusicVolume = volume;
    }

    public void setBackMusicVolume(float volume){
        backMusicVolume = volume;
    }

    public void setPosition(long pos){
        position = pos;
    }

    public long getPosition(){
        return position;
    }

    public boolean getState() { return run; }

    //thread to play media
    private class PlayThread extends Thread{
        @Override
        public void run(){
            //AudioTrack config
            int sampleRateInHz = 44100;
            int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            AudioTrack audioTrack = new  AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes, AudioTrack.MODE_STREAM);

            //file reader
            RandomAccessFile mainFile = null;
            RandomAccessFile backFile = null;

            //buffer to read file
            byte[] mainBuffer = new byte[512];//temple 512
            byte[] backBuffer = new byte[512];
            byte[] effectBuffer = new byte[512];
            byte[] mixBuffer;

            audioTrack.play();
            try {

                //file access initialize
                mainFile = new RandomAccessFile(mainMusicFile, "rw");
                backFile = new RandomAccessFile(backMusicFile, "rw");
                effectFile = new InputStream[EFFECT_NUM];
                for(int num=0; num<EFFECT_NUM; num++){
                    effectFile[num] = res.openRawResourceFd(effectResourceIDs[num]).createInputStream();
                }

                //start
                mainFile.seek(44);//skip the head of PCM
                backFile.seek(44);//skip the head of PCM
                //mark the head of the PCM
                for (InputStream soundInput : effectFile) {
                    soundInput.skip(44);
                }

                while(run){

                    //default signal
                    for(int i=0; i<512; i++){
                        mainBuffer[i] = 0;
                        backBuffer[i] = 0;
                        effectBuffer[i] = 0;
                    }

                    try{

                        //read data from file into buffer
                        if(mainMusicPlaying){
                            mainFile.seek(position);//position control
                            if(mainFile.read(mainBuffer)==-1){//read file and stop when ending
                                mainMusicPlaying = false;
                                break;
                            }
                            position = mainFile.getFilePointer();
                        }
                        if(backMusicPlaying){
                            backFile.read(backBuffer);
                        }
                        if(effectNumber>-1 && effectNumber<EFFECT_NUM){
                            if(effectFile[effectNumber].read(effectBuffer) == -1)//the effect sound has played over
                            {

                                effectFile[effectNumber] = res.openRawResourceFd(effectResourceIDs[effectNumber]).createInputStream();
                                effectFile[effectNumber].skip(44);
                                effectNumber = NO_EFFECT;//set effect number default
                            }
                        }
                    }
                    catch (IOException | NullPointerException e4){
                        e4.printStackTrace();
                        effectNumber = NO_EFFECT;//set effect number default
                    } finally{

                        //volum handle
                        for(int i=0; i<512; i++){
                            mainBuffer[i] *= mainMusicVolume;
                            backBuffer[i] *= backMusicVolume;
                        }

                        //mix
                        byte[][] bufferArray = {mainBuffer, backBuffer, effectBuffer};
                        mixBuffer = MixAudio(bufferArray);

                        //play
                        audioTrack.write(mixBuffer, 0, mixBuffer.length);
                    }
                }
                mainFile.close();
                backFile.close();
            } catch (SecurityException | IOException e2) {
                e2.printStackTrace();
            }
        }

        //mix
        private byte[] MixAudio(byte[][] bMulRoadAudioes){
            if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0)
                return null;

            byte[] realMixAudio = bMulRoadAudioes[0];

            if(bMulRoadAudioes.length == 1)
                return realMixAudio;

            for(int rw = 0 ; rw < bMulRoadAudioes.length ; ++rw){
                if(bMulRoadAudioes[rw].length != realMixAudio.length){
                    //Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
                    return null;
                }
            }

            int row = bMulRoadAudioes.length;
            int coloum = realMixAudio.length / 2;
            short[][] sMulRoadAudioes = new short[row][coloum];

            for (int r = 0; r < row; ++r) {
                for (int c = 0; c < coloum; ++c) {
                    sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
                }
            }

            short[] sMixAudio = new short[coloum];
            int mixVal;
            int sr = 0;
            for (int sc = 0; sc < coloum; ++sc) {
                mixVal = 0;
                sr = 0;
                for (; sr < row; ++sr) {
                    mixVal += sMulRoadAudioes[sr][sc];
                }
                sMixAudio[sc] = (short) (mixVal / row);
            }

            for (sr = 0; sr < coloum; ++sr) {
                realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
                realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
            }

            return realMixAudio;
        }
    }

}
