package com.dankstudio.www.fingerdj;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    ImageButton setMainMusic;//添加主音乐
    ImageButton setBackMusic;//添加背景音乐
    ImageButton musicPlay;//播放音乐
    ImageButton musicPause;//音乐暂停
    ImageButton addRecord;//添加音乐节点
    ImageButton back;//返回音乐节点
    ImageButton setEffectSound1;//音乐特效1
    ImageButton setEffectSound2;//音乐特效2
    ImageButton setEffectSound3;//音乐特效3
    ImageButton setEffectSound4;//音乐特效4
    ImageView musicPlayer;
    SeekBar seekBar_mainVolume;
    SeekBar seekBar_backVolume;
    SeekBar seekBar_track;
    ObjectAnimator discAnimation;
    boolean isplaying =true;
    //全局判断播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);
        initViews();
//        setAnimations();
//        转盘转动
    }

//    private void setAnimations(){
//        discAnimation = ObjectAnimator.ofFloat(musicPlayer, "rotation", 0, 360);
//        discAnimation.setDuration(20000);
//        //旋转时长
//        discAnimation.setInterpolator(new LinearInterpolator());
//        discAnimation.setRepeatCount(ValueAnimator.INFINITE);
//    }


    private void initViews(){
        setMainMusic = (ImageButton) findViewById(R.id.setMainMusicBtn);
        setBackMusic = (ImageButton) findViewById(R.id.setBackMusicBtn);
        musicPlay = (ImageButton) findViewById(R.id.play);
        addRecord = (ImageButton) findViewById(R.id.addrecord);
        back = (ImageButton) findViewById(R.id.back);
        setEffectSound1 = (ImageButton) findViewById(R.id.setEffectSound1);
        setEffectSound2 = (ImageButton) findViewById(R.id.setEffectSound2);
        setEffectSound3 = (ImageButton) findViewById(R.id.setEffectSound3);
        setEffectSound4 = (ImageButton) findViewById(R.id.setEffectSound4);
        musicPlayer = (ImageView) findViewById(R.id.musicplayer);
        seekBar_mainVolume = (SeekBar) findViewById(R.id.SeekBar_mainVolume);
        seekBar_backVolume = (SeekBar) findViewById(R.id.SeekBar_backVolume);
        seekBar_track = (SeekBar) findViewById(R.id.SeekBar_track);

//        musicPlay.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                if (isplaying) {
//                    playing();
//                } else if (discAnimation != null && discAnimation.isRunning()) {
//                    discAnimation.cancel();
//                    float valueAvatar = (float) discAnimation.getAnimatedValue();
//                    discAnimation.setFloatValues(valueAvatar, 360f + valueAvatar);
//                }
//                musicPlay.setImageResource(R.drawable.isplaying);
//                isplaying = true;
//            }
//        });

//        setMainMusic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("audio/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 1);
//            }
//        });
//
//        setBackMusic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("audio/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, 1);
//            }
//        });
//        读取音乐
    }


//        private void playing(){
//             discAnimation.start();
//             musicPlay.setImageResource(R.drawable.isplaying);
//             isplaying = false;
//             //点击播放切换为暂停键按钮
//        }


//        musicPlay.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if( isplaying == true ){
//                    musicPlay.setBackgroundResource(R.drawable.pausing);
//                    isplaying = false;
//                }else if( isplaying == false ){
//                    musicPlay.setBackgroundResource(R.drawable.isplaying);
//                    isplaying = true;
//                }
//            }
//        })
//
//    暂停播放按键的切换。
//    http://bbs.9ria.com/thread-234849-1-1.html
//    音乐播放器播放暂停停止
//    }
//
//    http://blog.csdn.net/izard999/article/details/6728306
//    //进度值改变事件监听
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//    }
//
//    //开始拖动事件监听
//    public void onStartTrackingTouch(SeekBar seekBar) {
//    }
//
//    //停止拖动事件监听
//    public void onStopTrackingTouch(SeekBar seekBar) {
//
//    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }
    //设置为横屏


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            File file = new File(img_path);
            Toast.makeText(MainActivity.this, file.toString(), Toast.LENGTH_SHORT).show();
        }
    }


}
