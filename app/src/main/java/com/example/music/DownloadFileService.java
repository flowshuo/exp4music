package com.example.music;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadFileService extends IntentService {
    private static final String TAG = DownloadFileService.class.getSimpleName();
    public DownloadFileService() {
        super("com.test.download");
    }

    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        // 获得下载文件的url
        String downloadUrl = bundle.getString("url");
        String filename = bundle.getString("name")+".mp3";
        // 设置文件下载后的保存路径，保存在SD卡根目录的Download文件夹
        File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
        // 检查文件夹是否存在，不存在则创建
        if (!dirs.exists()) {
            dirs.mkdir();
        }
        File file = new File(dirs, filename);
        // 开始下载
        downloadFile(downloadUrl, file);
        // 广播出去，由广播接收器来处理下载完成的文件
        Intent sendIntent = new Intent("com.test.downloadComplete");
        // 把下载好的文件的保存地址加进Intent
        sendIntent.putExtra("downloadFile", file.getPath());
        sendBroadcast(sendIntent);
    }
    private int fileLength, downloadLength;
    private void downloadFile(String downloadUrl, File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "找不到保存下载文件的目录");
            e.printStackTrace();
        }
        InputStream ips = null;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            huc.setReadTimeout(10000);
            huc.setConnectTimeout(3000);
            fileLength = Integer.valueOf(huc.getHeaderField("Content-Length"));
            ips = huc.getInputStream();
            // 拿到服务器返回的响应码
            int hand = huc.getResponseCode();
            if (hand == 200) {
                // 开始检查下载进度
                // 建立一个byte数组作为缓冲区，等下把读取到的数据储存在这个数组
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = ips.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloadLength = downloadLength + len;
                }
            } else {
                Log.e(TAG, "服务器返回码" + hand);
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (ips != null) {
                    ips.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void onDestroy() {
        super.onDestroy();
    }

}