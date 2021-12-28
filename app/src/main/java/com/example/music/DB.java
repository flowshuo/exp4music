package com.example.music;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {

    public DB(Context context) {
        super(context, "player", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table music(id integer primary key AUTOINCREMENT,link text,uri text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
