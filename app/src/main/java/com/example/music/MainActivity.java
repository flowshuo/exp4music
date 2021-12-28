package com.example.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private ListView mList;
    //
    private MyAdapter mAdapter;
    BroadcastReceiver receiver;
    DB helper;
    private SQLiteDatabase db;
    Cursor cursor;
    boolean nowState=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper=new DB(this);
        db=helper.getReadableDatabase();
        mList=findViewById(R.id.list);

        //initMusic();
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cursor.moveToPosition(i);
                String link=cursor.getString(1);
                String uri=cursor.getString(2);
                if(isMusic(uri)){//如果存在则播放
                    //封装数据
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", i);
                    bundle.putString("name", uri);
                    //传递
                    Intent intent = new Intent(MainActivity.this, Music.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{//不存在则下载
                    Toast.makeText(getApplicationContext(), "不存在，开始下载", Toast.LENGTH_SHORT).show();
                    download(link,uri);
                }
            }
        });
    }
    public boolean isMusic(String strFile){
        try {
            File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/"+strFile+".mp3");
            if(!f.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    public void download(String link,String uri){
        Log.d("","进入下载");

        Intent downloadIntent = new Intent(this, DownloadFileService.class);
        Bundle bundle = new Bundle();
        bundle.putString("url", link);
        bundle.putString("name", uri);
        downloadIntent.putExtras(bundle);
        startService(downloadIntent);
        // 设置广播接收器，当新版本的apk下载完成后自动弹出安装界面
        IntentFilter intentFilter = new IntentFilter("com.test.downloadComplete");
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
                nowState=false;
            }
        };
        registerReceiver(receiver, intentFilter);
    }
    protected void onDestroy() {
        // 移除广播接收器
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
    public void insert(String link,String uri){
        ContentValues cv = new ContentValues();
        cv.put("link", link);
        cv.put("uri", uri);
        db.insert("music", null, cv);
    }
    public void initMusic(){
        insert("http://10.0.2.2:8765/Audiobinger-AnneFrank.mp3","music1");
        insert("http://10.0.2.2:8765/BumyGoldson-ABluePurification.mp3","music2");
        insert("http://10.0.2.2:8765/LoboLoco-PostmanJack.mp3","music3");
        insert("http://10.0.2.2:8765/TeaKPea-fallintoyou.mp3","music4");
        insert("http://10.0.2.2:8765/thetunefulquire-lydianlillies.mp3","music5");
    }
    public void selectDb() {
        cursor = db.query("music", null, null, null, null, null, null);
        mAdapter = new MyAdapter(this, cursor);
        mList.setAdapter(mAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        selectDb();
    }


}
