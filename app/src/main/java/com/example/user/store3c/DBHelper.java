package com.example.user.store3c;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by user on 2016/9/18.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AccountDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME_USER = "UserList";
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PICTURE = "picture";

    private static final String TABLE_NAME_ORDER = "BuyList";
    private static final String KEY_ORDER_ID = "orderId";
    private static final String KEY_ORDER_INDEX = "orderIndex";
    private static final String KEY_ORDER_IMG = "orderImage";
    private static final String KEY_ORDER_PRODUCT = "orderProduct";
    private static final String KEY_ORDER_PRICE = "orderPrice";
    private static final String KEY_ORDER_INTRO = "orderIntro";

    private static final String TABLE_NAME_MEMO = "MemoList";
    private static final String KEY_MEMO_ID = "memoId";
    private static final String KEY_MEMO_INDEX = "memoIndex";
    private static final String KEY_MEMO_TEXT = "memoText";
    private static final String KEY_MEMO_PRICE = "memoPrice";

    private static final String TABLE_NAME_TASK_ID = "TaskIdList";
    private static final String KEY_TASK_ID = "taskId";
    private static final String KEY_RECENT_TASK_ID = "recentTaskId";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String DATABASE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_USER + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME + " TEXT, " +
                        KEY_EMAIL + " TEXT, " +
                        KEY_PASSWORD + " TEXT, " +
                        KEY_PICTURE + " BLOB );";

        final String DATABASE_CREATE_ORDER =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ORDER + " (" +
                        KEY_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ORDER_INDEX + " INTEGER, " +
                        KEY_ORDER_IMG + " BLOB, " +
                        KEY_ORDER_PRODUCT + " TEXT, " +
                        KEY_ORDER_PRICE + " TEXT, " +
                        KEY_ORDER_INTRO + " TEXT );";

        final String DATABASE_CREATE_MEMO =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MEMO + " (" +
                        KEY_MEMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_MEMO_INDEX + " INTEGER, " +
                        KEY_MEMO_TEXT + " TEXT, " +
                        KEY_MEMO_PRICE + " TEXT );";

        final String DATABASE_CREATE_TASK_ID =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TASK_ID + " (" +
                        KEY_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_RECENT_TASK_ID + " INTEGER);";

        try {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE_ORDER);
            db.execSQL(DATABASE_CREATE_MEMO);
            db.execSQL(DATABASE_CREATE_TASK_ID);
        }catch (SQLiteException e) {
            Log.i("db", "Creat:" + DATABASE_CREATE_ORDER);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MEMO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TASK_ID);
        onCreate(db);
    }

}
