package com.camnter.newlife.component.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Description：MessageSQLiteHelper
 * Created by：CaMnter
 * Time：2015-11-12 17:07
 */
public class MessageSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "MessageSQLiteHelper";

    private static final String DB_NAME = "message.db";
    private static final int VERSION = 1;

    public static final String TB_MESSAGE = "tb_message";
    public static final String TB_MESSAGE_SQL = "CREATE TABLE IF NOT EXISTS " + TB_MESSAGE +
            "(_id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            " content text)";

    private static MessageSQLiteHelper ourInstance;


    public MessageSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public static MessageSQLiteHelper getInstance(Context context) {
        if (ourInstance == null) ourInstance = new MessageSQLiteHelper(context);
        return ourInstance;
    }


    public MessageSQLiteHelper(Context context) {
        this(context, DB_NAME, null, VERSION);
    }


    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_MESSAGE_SQL);
    }


    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
    }
}
