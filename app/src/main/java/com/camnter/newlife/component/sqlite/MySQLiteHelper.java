package com.camnter.newlife.component.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.camnter.newlife.bean.SQLiteData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description：MySQLiteHelper
 * Created by：CaMnter
 * Time：2015-11-04 12:01
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "camnter.db";
    private static final int VERSION = 1;

    private static final String TB_CAMNTER = "tb_camnter";
    public static final String TB_CAMNTER_SQL = "CREATE TABLE IF NOT EXISTS " + TB_CAMNTER +
            "(_id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            " content text)";

    public static MySQLiteHelper ourInstance;


    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    private MySQLiteHelper(Context context) {
        this(context, DB_NAME, null, VERSION);
    }


    public static MySQLiteHelper getInstance(Context context) {
        if (ourInstance == null) ourInstance = new MySQLiteHelper(context);
        return ourInstance;
    }


    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     * 首次创建数据库时调用。这就是创建表和表的初始种群。
     *
     * @param db The database.
     */
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_CAMNTER_SQL);
    }


    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * 数据库需要更新时调用，实现类应该使用这个方法删除表，添加表，或者做其他事情需要更新表到新模式版本
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * 如果你添加新的列你可以使用ALTER TABLE插入到现存的表里。如果你想重命名或移除列你可以使用ALTER TABLE重命名
     * 旧表，然后创建新表，接着填充新表与旧表的内容
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * 这个方法执行了一个事务。如果抛出一个异常，所有更改将自动回滚。
     * </p>
     *
     * @param db The database. 数据库
     * @param oldVersion The old database version. 旧数据库版本
     * @param newVersion The new database version. 新数据库版本
     */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            String sql = "ALTER TABLE " + TB_CAMNTER + " ADD " + "status" + " text default " + 0;
            db.execSQL(sql);
        }
    }


    /**
     * 删除表
     */
    public void deleteTable() {
        SQLiteDatabase delete = this.getWritableDatabase();
        delete.execSQL("DROP TABLE " + TB_CAMNTER);
    }


    /**
     * 保存数据
     */
    public void insert(String content) {
        SQLiteDatabase insert = this.getWritableDatabase();
        insert.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("content", content);
        insert.insert(TB_CAMNTER, "", values);

        /**
         * 设置事务处理成功，不设置会自动回滚不提交
         */
        insert.setTransactionSuccessful();
        /**
         * 处理完成
         */
        insert.endTransaction();
    }


    /**
     * 删除数据
     */
    public void deleteAll() {
        SQLiteDatabase deleteAll = this.getWritableDatabase();
        deleteAll.delete(TB_CAMNTER, null, null);
        deleteAll.close();
    }


    /**
     * 修改第一条数据
     */
    public void updateFirst() {
        List<SQLiteData> allData = this.queryAll();
        if (allData == null || allData.size() < 1) return;
        int firstId = allData.get(0).id;
        SQLiteDatabase update = this.getWritableDatabase();
        update.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("content", UUID.randomUUID().toString());
        update.update(TB_CAMNTER, values, "_id=?", new String[] { firstId + "" });
        update.setTransactionSuccessful();
        update.endTransaction();
    }


    /**
     * 查询数据
     *
     * @return List list
     */
    public List<SQLiteData> queryAll() {
        List<SQLiteData> allData = new ArrayList<>();
        SQLiteDatabase queryAll = this.getReadableDatabase();
        queryAll.beginTransaction();
        String sql = "select * from " + TB_CAMNTER;
        Cursor result = queryAll.rawQuery(sql, null);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            SQLiteData data = new SQLiteData();
            data.id = result.getInt(result.getColumnIndex("_id"));
            data.content = result.getString(result.getColumnIndex("content"));
            allData.add(data);
        }
        queryAll.setTransactionSuccessful();
        queryAll.endTransaction();
        result.close();
        return allData;
    }
}
