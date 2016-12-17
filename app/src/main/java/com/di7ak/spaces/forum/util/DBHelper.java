package com.di7ak.spaces.forum.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    
    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table contacts ("
                + "id integer primary key autoincrement,"
                + "user_id integer,"
                + "talk_id integer,"
                + "contact_id integer," 
                + "name text,"
                + "avatar text"
                + ");");
        db.execSQL("create table messages ("
                   + "id integer primary key autoincrement,"
                   + "contact_id integer,"
                   + "user_id integer,"
                   + "msg_id integer,"
                   + "talk integer,"
                   + "not_read integer,"
                   + "type integer," 
                   + "date text,"
                   + "message text"
                   + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
