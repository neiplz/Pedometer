package com.github.neiplz.pedometer.persistence;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.github.neiplz.pedometer.models.Pref;
import com.github.neiplz.pedometer.models.Step;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "pedometer.db";
    private final static int DATABASE_VERSION = 1;
    private static final String TABLE_STEP = "step";
    private static final String TABLE_SHARED_PREFERENCE = "pref";

    private static DatabaseHelper instance;

    /**
     * 创建step表
     */
    public static final String SQL_CREATE_STEP = "create table step("
            + "_id integer primary key autoincrement,"
            + "step_count integer,"
            + "date integer,"
            + "email text,"
            + "sync integer)";

    /**
     * 创建pref表
     */
    public static final String SQL_CREATE_PREF = "create table pref("
            + "_id integer primary key autoincrement,"
            + "email text,"
            + "stride integer,"
            + "goal integer,"
            + "sensitivity real,"
            + "sync integer)";

    /**
     * 创建user表
     */
    public static final String SQL_CREATE_USER = "create table user("
            + "_id integer primary key autoincrement,"
            + "email text ,"
            + "name text,"
            + "gender real,"
            + "weight real,"
            + "step_length integer,"
            + "sensitivity integer,"
            + "today_step integer)";

    // Solution： Leak foundCaused by: java.lang.IllegalStateException: SQLiteDatabase created and never closed
//    private AtomicInteger mCount = new AtomicInteger();

    private DatabaseHelper(final Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(final Context context){
        if(null == instance){
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STEP);
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_PREF);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 数据库升级时使用
    }


    /**
     * 增加step表里的数据
     *
     * @param step
     */
    public void saveStep(Step step) {
        if (null != step) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("step_count", step.getStepCount());
            values.put("date", step.getDate());
            values.put("email", step.getEmail());
            values.put("sync", step.getSync());
            db.insert(TABLE_STEP, null, values);
        }
    }


    /**
     * 升级step表里的数据
     *
     * @param step
     */
    public void updateStep(Step step) {
        if (null != step) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("step_count", step.getStepCount());
            values.put("sync", step.getSync());
            db.update(TABLE_STEP, values, "email = ? and date = ?", new String[]{
                    step.getEmail(), String.valueOf(step.getDate())});
        }
    }

    /**
     * 根据user的id删除user表里的数据
     *
     * @param step
     */
    public void deleteStep(Step step) {
        if (null != step) {
            SQLiteDatabase db = getWritableDatabase();

            db.delete(TABLE_STEP, "email = ? and date = ?", new String[]{
                    step.getEmail(), String.valueOf(step.getDate())});
            db.close();
        }
    }

    /**
     * 查询
     * @param email
     * @param date
     * @return
     */
    public Step queryStep(String email, long date) {
        Step step = null;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_STEP, null, "email = ? and date = ?",
                new String[]{email, String.valueOf(date)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                step = new Step();
                step.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                step.setStepCount(cursor.getInt(cursor.getColumnIndex("step_count")));
                step.setSync(cursor.getInt(cursor.getColumnIndex("sync")));
                step.setEmail(email);
                step.setDate(date);
            } while (cursor.moveToNext());

        } else {
            Log.i(LOG_TAG, "step is null!");
        }
        cursor.close();
        db.close();

        return step;
    }


    public int updateSteps(String email, long date, int stepCount, int sync) {
        if (!TextUtils.isEmpty(email)) {
            getWritableDatabase().beginTransaction();
            int result = -1;
            try {
                Cursor c = getReadableDatabase().query(TABLE_STEP, new String[]{"step_count"}, "email = ? and date = ?", new String[]{email, String.valueOf(date)}, null, null, null);
                int count = c.getCount();

                if (count == 0) {
                    ContentValues values = new ContentValues();
                    values.put("step_count", stepCount);
                    values.put("date", date);
                    values.put("email", email);
                    values.put("sync", sync);
                    long rowId = getWritableDatabase().insert(TABLE_STEP, null, values);
                    Log.d(LOG_TAG,"rowId = " + rowId);
                    if(rowId > 0){
                        result = stepCount;
                    }
                    Log.d(LOG_TAG,"result = " + result);
                } else if (count > 0) {
                    int oldStepCount = 0;
                    if (c.moveToFirst()) {
                        do {
                            oldStepCount = c.getInt(c.getColumnIndex("step_count"));
                        } while (c.moveToNext());
                    }

                    ContentValues values = new ContentValues();
                    values.put("step_count", stepCount + oldStepCount);
                    values.put("sync", sync);

                    int affectedRows = getWritableDatabase().update(TABLE_STEP, values, "email = ? and date = ?", new String[]{email, String.valueOf(date)});
                    Log.d(LOG_TAG,"affectedRows = " + affectedRows);
                    if(affectedRows > 0){
                        result = stepCount + oldStepCount;
                    } else {
                        result = oldStepCount;//貌似不会被执行
                    }
                    Log.d(LOG_TAG,"result = " + result);
                }
                c.close();
                getWritableDatabase().setTransactionSuccessful();
            } finally {
                getWritableDatabase().endTransaction();
            }
            return result;
        }
        return 0;
    }


    /**
     * 增加pref表里的数据
     *
     * @param pref
     */
    public void savePref(Pref pref) {
        if (null != pref) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("email", pref.getEmail());
            values.put("stride", pref.getStride());
            values.put("goal", pref.getGoal());
            values.put("sensitivity", pref.getSensitivity());
            values.put("sync", pref.getSync());
            db.insert(TABLE_SHARED_PREFERENCE, null, values);
        }
    }


    /**
     * 升级pref表里的数据
     *
     * @param pref
     */
    public void updatePref(Pref pref) {
        if (null != pref) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("stride", pref.getStride());
            values.put("goal", pref.getGoal());
            values.put("sensitivity", pref.getSensitivity());
            values.put("sync", pref.getSync());
            db.update(TABLE_SHARED_PREFERENCE, values, "email = ?", new String[]{pref.getEmail()});
        }
    }

    /**
     * 根据email删除pref表里的数据
     *
     * @param email
     */
    public void deletePref(String email) {
        if (!TextUtils.isEmpty(email)) {
            SQLiteDatabase db = getWritableDatabase();

            db.delete(TABLE_SHARED_PREFERENCE, "email = ?", new String[]{email});
            db.close();
        }
    }

    /**
     * 查询
     * @param email
     * @return
     */
    public Pref queryPref(String email) {
        Log.i(LOG_TAG, "...in queryPref, email = " + email);
        Pref pref = null;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_SHARED_PREFERENCE, null, "email = ?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                pref = new Pref();
                pref.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                pref.setGoal(cursor.getInt(cursor.getColumnIndex("goal")));
                pref.setStride(cursor.getInt(cursor.getColumnIndex("stride")));
                pref.setSensitivity(cursor.getFloat(cursor.getColumnIndex("sensitivity")));
                pref.setSync(cursor.getInt(cursor.getColumnIndex("sync")));
                pref.setEmail(email);
            } while (cursor.moveToNext());

        } else {
            Log.i(LOG_TAG, "pref is null!");
        }
        cursor.close();
        db.close();

        return pref;
    }


    public boolean updatePrefByGoal(int goal, String email) {
        getWritableDatabase().beginTransaction();
        long result = -1;
        try {
            Cursor c = getReadableDatabase().query(TABLE_SHARED_PREFERENCE, new String[]{"goal"}, "email = ?", new String[]{email}, null, null, null);
            int count = c.getCount();

            if (count == 0) {
                ContentValues values = new ContentValues();
                values.put("goal", goal);
                values.put("email", email);
                values.put("sync", 1);
                result =  getWritableDatabase().insert(TABLE_SHARED_PREFERENCE, null, values);
                Log.d("tag"," insert result = " + result);
            } else if(count > 0){

                ContentValues values = new ContentValues();
                values.put("goal", goal);
                values.put("sync", 1);
                result = getWritableDatabase().update(TABLE_SHARED_PREFERENCE, values, "email = ?", new String[]{email});
                Log.d("tag"," update result = " + result);
            }
            c.close();
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
        return result > -1;
    }

    public boolean updatePreference(Pref pref) {
        if (null != pref) {
            String email = pref.getEmail();
            if (!TextUtils.isEmpty(email)) {
                getWritableDatabase().beginTransaction();
                long result = -1;
                try {
                    Cursor c = getReadableDatabase().query(TABLE_SHARED_PREFERENCE, new String[]{"goal"}, "email = ?", new String[]{email}, null, null, null);
                    int count = c.getCount();

                    if (count == 0) {
                        ContentValues values = new ContentValues();
                        int goal = pref.getGoal();
                        if (goal > 0) {
                            values.put("goal", goal);
                        }
                        int stride = pref.getStride();
                        if (stride > 0) {
                            values.put("stride", stride);
                        }
                        float sensitivity = pref.getSensitivity();
                        if (sensitivity > 0.0f) {
                            values.put("sensitivity", sensitivity);
                        }

                        values.put("email", pref.getEmail());
                        values.put("sync", pref.getSync());
                        result = getWritableDatabase().insert(TABLE_SHARED_PREFERENCE, null, values);
                        Log.d(LOG_TAG, " insert result = " + result);
                    } else if (count > 0) {

                        ContentValues values = new ContentValues();
                        int goal = pref.getGoal();
                        if (goal > 0) {
                            values.put("goal", goal);
                        }
                        int stride = pref.getStride();
                        if (stride > 0) {
                            values.put("stride", stride);
                        }
                        float sensitivity = pref.getSensitivity();
                        if (sensitivity > 0.0f) {
                            values.put("sensitivity", sensitivity);
                        }

                        values.put("email", pref.getEmail());
                        values.put("sync", pref.getSync());
                        result = getWritableDatabase().update(TABLE_SHARED_PREFERENCE, values, "email = ?", new String[]{email});
                        Log.d(LOG_TAG, " update result = " + result);
                    }
                    c.close();
                    getWritableDatabase().setTransactionSuccessful();
                } finally {
                    getWritableDatabase().endTransaction();
                }
                return result > -1;
            }
        }
        return false;
    }

    public boolean setPrefFlag(String email,int flag) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("sync", flag);
        return db.update(TABLE_SHARED_PREFERENCE, values, "email = ?", new String[]{email}) > 0;
    }
}
