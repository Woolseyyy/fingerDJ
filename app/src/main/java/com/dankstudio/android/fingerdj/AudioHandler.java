package com.dankstudio.android.fingerdj;

/**
 * Created by admin on 2016/12/22.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;

public class AudioHandler {
    private final int EFFECT_NUM = 3;

    //media data
    //private File mainMusicFile = null;
    //private File backMusicFile = null;
    private File mainMusicFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/netease/cloudmusic/Music/1.wav");
    private File backMusicFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/netease/cloudmusic/Music/2.wav");
    private File[] effectSoundFile = new File[EFFECT_NUM];

    //control signal
    private boolean run = false;
    private boolean mainMusicPlaying = false;
    private boolean backMusicPlaying = false;
    private int effectNumber = -1;
    private float mainMusicVolum = 1;
    private float backMusicVolum = 1;
    private long position = 44;
    private long positionRecord = 44;

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
        effectNumber = -1;
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
        effectNumber = num;
    }

    public void setMainMusicVolum(int volum){
        mainMusicVolum = volum;
    }

    public void setBackMusicVolum(int volum){
        backMusicVolum = volum;
    }

    public void setPosition(long pos){
        position = pos;
    }

    public long getPosition(){
        return position;
    }

    public void markPosition(long pos){
        positionRecord = pos;
    }

    public void backToPosition(){
        position = positionRecord;
    }

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
            RandomAccessFile[] effectFile = null;

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
                /*effectFile = new RandomAccessFile[EFFECT_NUM];
                for(int i=0; i<EFFECT_NUM; i++){
                    effectFile[i] = new RandomAccessFile(effectSoundFile[i], "rw");
                }*/

                //start
                mainFile.seek(44);//skip the head of PCM
                backFile.seek(44);//skip the head of PCM

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
                                effectNumber = -1;
                                effectFile[effectNumber].seek(44);
                            }
                        }
                    }
                    catch (IOException e4){
                        e4.printStackTrace();
                    }
                    catch (NullPointerException e5){
                        e5.printStackTrace();
                    }
                    finally{

                        //volum handle
                        for(int i=0; i<512; i++){
                            mainBuffer[i] *= mainMusicVolum;
                            backBuffer[i] *= backMusicVolum;
                        }

                        //mix
                        byte[][] bufferArray = {mainBuffer, backBuffer, effectBuffer};
                        mixBuffer = MixAudio(bufferArray);

                        //play
                        audioTrack.write(mixBuffer, 0, mixBuffer.length);
                    }
                }
            }
            catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            catch (SecurityException e2) {
                e2.printStackTrace();
            }
            catch (IOException e3){
                e3.printStackTrace();
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
