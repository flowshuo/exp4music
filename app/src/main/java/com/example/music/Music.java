package com.example.music;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class Music extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    int position;
    Cursor cursor;
    DB helper;
    private SQLiteDatabase db;
    //部件
    private TextView viewName;
    private SeekBar seekBar;
    private Button btnPlay;
    //
    String musicPath;
    MediaPlayer player;
    //
    private Thread thread;
    //记录播放位置
    private int time;
    //记录是否暂停
    private boolean flage = false;
    private boolean isChanging = false;

    public void init(){
        helper=new DB(this);
        db=helper.getReadableDatabase();
        //
        Bundle bundle=this.getIntent().getExtras();
        position=bundle.getInt("id");
        //
        viewName=this.findViewById(R.id.name);
        viewName.setText(bundle.getString("name"));
        //
        btnPlay=findViewById(R.id.play);
        //
        seekBar = findViewById(R.id.seekBar);
        //
        player=new MediaPlayer();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        init();
        cursor = db.query("music",null, null, null, null, null, null);
        cursor.moveToPosition(position);
        musicPath= Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/"+viewName.getText().toString()+".mp3";
        try {
            player.setDataSource(musicPath);
            player.prepare();    //准备
            player.start();  //播放
            seekBar.setMax(player.getDuration());//设置SeekBar的长度
            seekBar.setOnSeekBarChangeListener(this);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        // 启动线程
        thread = new Thread(new SeekBarThread());
        thread.start();
    }
    public void playOrPause(View v){
        if (player.isPlaying()) {//正在播放
            //m.getCurrentPosition();获取当前播放位置
            time = player.getCurrentPosition();
            //如果正在播放，则暂停
            player.pause();
            btnPlay.setText("播放");
            flage = true;//flage 标记为 ture
        } else if (flage) {//现在在暂停
            player.start();//先开始播放
            player.seekTo(time);//设置从哪里开始播放
            btnPlay.setText("播放");
            flage = false;
        } else {
            player.reset();//恢复到未初始化的状态
            try {
                player.setDataSource(musicPath);
                seekBar.setMax(player.getDuration());//设置SeekBar的长度
                player.prepare();    //准备
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
            player.start();  //播放
            btnPlay.setText("暂停");
        }
        thread = new Thread(new SeekBarThread());
        thread.start();
    }
    public void back(View v){
        if(position<=0){
            return;
        }
        cursor.moveToPrevious();
        position--;
        reStratIntent();
    }
    public void next(View v){
        if(position>=4){
            return;
        }
        cursor.moveToNext();
        position++;
        reStratIntent();
    }
    public void reStratIntent(){
        //封装数据
        Bundle bundle = new Bundle();
        bundle.putInt("id", position);
        bundle.putString("name", cursor.getString(2));

        Intent intent=getIntent();
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    //Activity从后台重新回到前台时被调用
    @Override
    protected void onRestart() {
        super.onRestart();
        if (player != null) {
            if (player.isPlaying()) {
                player.start();
            }
        }
    }
    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            }
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            if (!player.isPlaying()) {
                player.start();
            }
        }
    }
    protected void onDestroy() {
        if (player.isPlaying()) {
            player.stop();//停止音频的播放
        }
        player.release();//释放资源
        super.onDestroy();
    }

    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (!isChanging && player.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
                seekBar.setProgress(player.getCurrentPosition());
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(100);
                    //播放进度
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            seekBar.setProgress(0);
        }
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //防止在拖动进度条进行进度设置时与Thread更新播放进度条冲突
        isChanging = true;
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //将media进度设置为当前seekbar的进度
        player.seekTo(seekBar.getProgress());
        isChanging = false;
        thread = new Thread(new SeekBarThread());
        // 启动线程
        thread.start();
    }

}
