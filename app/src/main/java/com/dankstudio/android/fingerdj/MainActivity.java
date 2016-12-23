package com.dankstudio.android.fingerdj;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    //final variable
    private final int PICK_MAIN_MUSIC_REQUEST = 1;
    private final int PICK_BACK_MUSIC_REQUEST = 2;
    private final RotateAnimation animation = new RotateAnimation(0f, 3600f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);

    //service
    private MusicService musicService;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };
    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    //UI
    private ImageButton setMainMusic;//添加主音乐
    private ImageButton setBackMusic;//添加背景音乐
    private ImageButton musicPlay;//播放音乐
    private ImageButton musicPause;//音乐暂停
    private ImageButton addRecord;//添加音乐节点
    private ImageButton mixStart;//开始混合
    private ImageButton mixStop;//结束混合
    private ImageButton backRecord;//返回音乐节点
    private ImageButton setEffectSound1;//音乐特效1
    private ImageButton setEffectSound2;//音乐特效2
    private ImageButton setEffectSound3;//音乐特效3
    private ImageButton setEffectSound4;//音乐特效4
    private ImageView musicPlayer;
    private SeekBar seekBar_mainVolume;
    private SeekBar seekBar_backVolume;
    private SeekBar seekBar_track;
    ObjectAnimator discAnimation;
    boolean isplaying =true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);//隐藏虚拟导航键
        setContentView(R.layout.activity_main);
        initViews();//initialize UI component
        setListener();
        bindServiceConnection();
    }

    //获得各组件ID
    private void initViews(){
        setMainMusic = (ImageButton) findViewById(R.id.setMainMusicBtn);
        setBackMusic = (ImageButton) findViewById(R.id.setBackMusicBtn);
        musicPlay = (ImageButton) findViewById(R.id.play);
        musicPause = (ImageButton) findViewById(R.id.pause);
        addRecord = (ImageButton) findViewById(R.id.addrecord);
        backRecord = (ImageButton) findViewById(R.id.backRecord);
        setEffectSound1 = (ImageButton) findViewById(R.id.setEffectSound1);
        setEffectSound2 = (ImageButton) findViewById(R.id.setEffectSound2);
        setEffectSound3 = (ImageButton) findViewById(R.id.setEffectSound3);
        setEffectSound4 = (ImageButton) findViewById(R.id.setEffectSound4);
        musicPlayer = (ImageView) findViewById(R.id.musicplayer);
        seekBar_mainVolume = (SeekBar) findViewById(R.id.SeekBar_mainVolume);
        seekBar_backVolume = (SeekBar) findViewById(R.id.SeekBar_backVolume);
        seekBar_track = (SeekBar) findViewById(R.id.SeekBar_track);
        mixStart = (ImageButton) findViewById(R.id.start);
        mixStop = (ImageButton) findViewById(R.id.stop);
        animation.setDuration(30000);// 设置动画持续时间
    }

    //add Listener
    private void setListener(){
        //监听主音频按钮，点击打开文件管理器，选取主音频
        setMainMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogProperties properties=new DialogProperties();

                //properties
                properties.selection_mode= DialogConfigs.SINGLE_MODE;
                properties.selection_type=DialogConfigs.FILE_SELECT;
                properties.root=new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions=null;

                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select a wav file as main music");

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is the array of the paths of files selected by the Application User.
                        //set main music
                        musicService.setMainMusic(files[0]);
                    }
                });

                dialog.show();
            }
        });

        //监听次音频按钮，点击打开文件管理器，选取次音频
        setBackMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogProperties properties=new DialogProperties();

                //properties
                properties.selection_mode= DialogConfigs.SINGLE_MODE;
                properties.selection_type=DialogConfigs.FILE_SELECT;
                properties.root=new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions=null;

                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select a wav file as back music");

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is the array of the paths of files selected by the Application User.
                        //set main music
                        musicService.setBackMusic(files[0]);
                    }
                });

                dialog.show();
            }
        });

        //start music mix
        mixStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //UI
                mixStop.setVisibility(View.VISIBLE);
                mixStart.setVisibility(View.GONE);

                //start the music
                musicService.start();

                //change the button of play/pause
                musicPause.setVisibility(View.VISIBLE);
                musicPlay.setVisibility(View.GONE);
                musicPlayer.setAnimation(animation);
                animation.startNow();
            }
        });

        //stop music mix
        mixStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mixStart.setVisibility(View.VISIBLE);
                mixStop.setVisibility(View.GONE);

                //start the music
                musicService.stop();

                //change the button play/pause
                musicPlay.setVisibility(View.VISIBLE);
                musicPause.setVisibility(View.GONE);
                animation.cancel();
            }
        });

        musicPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(musicService.mainMusicPlay()){//play
                    //点击播放按键时，播放键不显示，显示暂停键
                    musicPause.setVisibility(View.VISIBLE);
                    musicPlay.setVisibility(View.GONE);
                    musicPlayer.setAnimation(animation);
                    animation.startNow();
                }
            }
        });

        musicPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(musicService.mainMusicPause()){//pause
                    //点击播放按键时，暂停键不显示，显示播放键
                    musicPlay.setVisibility(View.VISIBLE);
                    musicPause.setVisibility(View.GONE);
                    animation.cancel();
                }
            }
        });

        addRecord.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //record a position
                musicService.addRecord();
            }
        });

        backRecord.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //back to position
                musicService.backRecord();
            }
        });

        setEffectSound1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set Effect
                musicService.setEffectSound(1);
            }
        });

        setEffectSound2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set Effect
                musicService.setEffectSound(2);
            }
        });

        setEffectSound3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set Effect
                musicService.setEffectSound(3);
            }
        });

        setEffectSound4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //set Effect
                musicService.setEffectSound(4);
            }
        });

        seekBar_backVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                musicService.setBackVolume(i, seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //donothing
            }
        });

        seekBar_mainVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                musicService.setMainVolume(i, seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //donothing
            }
        });

    }

    @Override
    protected void onResume() {
        //设置为进入应用后强制横屏
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sc!=null){
            unbindService(sc);
        }
    }
}
