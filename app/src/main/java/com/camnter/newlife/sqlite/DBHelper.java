package com.camnter.newlife.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Hashtable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "plank.db";
    private static final int VERSION = 2;
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    public static final String COLUMN_NAME_TIME_DURATION = "timeDuration";
    public static final String COLUMN_NAME_START_MILLIS = "startMillis";
    public static final String COLUMN_NAME_END_MILLIS = "endMillis";
    public static final String COLUMN_NAME_SYNC = "sync";
    public static final int COLUMN_INDEX_TIMESTAMP = 1;
    public static final int COLUMN_INDEX_TIME_DURATION = 2;

    public static final String TABLE_TRAIN = "train";
    public static final String TABLE_CHALLENGE = "challenge";
    public static final String TABLE_TRAIN_DETAIL = "t_train"; // train detail
    public static final String TABLE_CHALLENGE_DETAIL = "t_challenge"; // challenge detail

    public static final int STATUS_PENDING_SYNC = 0;
    public static final int STATUS_SYNCED = 1;

    private static final String CREATE_CHALLENGE_DETAIL_TABLE = "CREATE TABLE "
            + TABLE_CHALLENGE_DETAIL
            + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " " + COLUMN_NAME_START_MILLIS + " BIGINT, "
            + " " + COLUMN_NAME_END_MILLIS + " BIGINT, "
            + " " + COLUMN_NAME_SYNC + " INT DEFAULT " + STATUS_PENDING_SYNC
            + ")";

    private static final String CREATE_TRAIN_DETAIL_TABLE = "CREATE TABLE "
            + TABLE_TRAIN_DETAIL
            + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " " + COLUMN_NAME_START_MILLIS + " BIGINT, "
            + " " + COLUMN_NAME_END_MILLIS + " BIGINT, "
            + " " + COLUMN_NAME_SYNC + " INT DEFAULT " + STATUS_PENDING_SYNC
            + ")";

    private static DBHelper dbHelper;

    private DBHelper(Context context) {
        this(context, DB_NAME, null, VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    private DBHelper(Context context, String name, CursorFactory factory,
                     int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, TABLE_TRAIN);
        createTable(db, TABLE_CHALLENGE);
        db.execSQL(CREATE_CHALLENGE_DETAIL_TABLE);
        db.execSQL(CREATE_TRAIN_DETAIL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            //插入新字段sql语句：ALTER TABLE  表名   ADD  字段名  类型   default '默认值'
            String sql = "ALTER TABLE " + TABLE_TRAIN + " ADD " + COLUMN_NAME_SYNC + " INT default " + STATUS_PENDING_SYNC;
            db.execSQL(sql);
            sql = "ALTER TABLE " + TABLE_CHALLENGE + " ADD " + COLUMN_NAME_SYNC + " INT default " + STATUS_PENDING_SYNC;
            db.execSQL(sql);
            db.execSQL(CREATE_CHALLENGE_DETAIL_TABLE);
            db.execSQL(CREATE_TRAIN_DETAIL_TABLE);
        }

    }

    private void createTable(SQLiteDatabase db, String tableName) {
        String sql = "CREATE TABLE " + tableName + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " " + COLUMN_NAME_TIMESTAMP + " date, "
                + " " + COLUMN_NAME_TIME_DURATION + " LONG, "
                + " " + COLUMN_NAME_SYNC + " INT  default " + STATUS_PENDING_SYNC
                + ")";
        db.execSQL(sql);
    }


    //TODO http://stackoverflow.com/questions/3634984/insert-if-not-exists-else-update/3635038#3635038
    public void insertOrUpdate(String tableName, long timeDuration) {
        //train table, sum
        // challenge table, max
        SQLiteDatabase db = getWritableDatabase();
        String insertSQL = "insert into " + tableName + "( " + COLUMN_NAME_TIMESTAMP + ", " + COLUMN_NAME_TIME_DURATION + ", " + COLUMN_NAME_SYNC + ") "
                + "values ( date(), " + timeDuration + ", " + STATUS_PENDING_SYNC + " )";

        //TODO do not need to query the timestamp
        Cursor cursor = db.query(tableName, new String[]{COLUMN_NAME_TIMESTAMP, COLUMN_NAME_TIME_DURATION}, COLUMN_NAME_TIMESTAMP + "=date()", null, null, null, null, "1");
        if (cursor != null && cursor.getCount() > 0) {
            //attention: firstly we need to move the cursor to the first row
            cursor.moveToFirst();
            String dateString = cursor.getString(0);
            long duration = cursor.getLong(1);
            if (tableName.equals(TABLE_TRAIN)) {
                timeDuration += duration;
                String updateSQL = "update " + tableName + " set " + COLUMN_NAME_TIME_DURATION + "=" + timeDuration
                        + " , " + COLUMN_NAME_SYNC + "=" + STATUS_PENDING_SYNC
                        + " where " + COLUMN_NAME_TIMESTAMP + "=date()";
                db.execSQL(updateSQL);
            } else {
                if (duration < timeDuration) {
                    String updateSQL = "update " + tableName + " set " + COLUMN_NAME_TIME_DURATION + "=" + timeDuration
                            + " , " + COLUMN_NAME_SYNC + "=" + STATUS_PENDING_SYNC
                            + " where " + COLUMN_NAME_TIMESTAMP + "=date()";
                    db.execSQL(updateSQL);
                }
            }
        } else {
            db.execSQL(insertSQL);
        }

        if (cursor != null) {
            cursor.close();
        }

    }

    public void insertTrainDetail(long startMillis, long endMillis) {
        final String sql = "INSERT INTO "
                + TABLE_TRAIN_DETAIL + " ("
                + COLUMN_NAME_START_MILLIS + ", "
                + COLUMN_NAME_END_MILLIS + ") "
                + " VALUES ("
                + startMillis + ", "
                + endMillis
                + " )";
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public void insertChallengeDetail(long startMillis, long endMillis) {
        final String sql = "INSERT INTO "
                + TABLE_CHALLENGE_DETAIL + " ("
                + COLUMN_NAME_START_MILLIS + ", "
                + COLUMN_NAME_END_MILLIS + ") "
                + " VALUES ("
                + startMillis + ", "
                + endMillis
                + " )";
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public Hashtable<String, Long> queryLastestRecord(String tableName, int daysCount) {
        SQLiteDatabase db = getReadableDatabase();
        Hashtable<String, Long> hashtable = null;
        Cursor cursor = db.query(tableName, new String[]{COLUMN_NAME_TIMESTAMP, COLUMN_NAME_TIME_DURATION},
                COLUMN_NAME_TIMESTAMP + " > date('now', " + "'" + "-" + daysCount + " day') ", null, null, null, null, "" + daysCount);

        if (cursor != null && cursor.getCount() > 0) {
            hashtable = new Hashtable<String, Long>();
            cursor.moveToFirst();
            int count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                String dateString = cursor.getString(0);
                long duration = cursor.getLong(1);
                hashtable.put(dateString, Long.valueOf(duration));
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return hashtable;
    }


}
