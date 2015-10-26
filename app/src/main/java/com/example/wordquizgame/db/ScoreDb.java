package com.example.wordquizgame.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Promlert on 10/25/2015.
 */
public class ScoreDb {
    private static final String TAG = "ScoreDb";

    private static final String DATABASE_NAME = "wordquizgame.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "scores";
    public static final String COL_ID = "_id";
    public static final String COL_SCORE = "score";
    public static final String COL_DIFFICULTY = "difficulty";

    private final Context mContext;

    // ประกาศ mDbHelper เป็น static เพื่อให้เป็นตัวแปรของคลาส (class variable) แทนที่จะเป็นตัวแปรของออบเจ็ค
    // ที่สร้างมาจากคลาสนี้ (instance variable) ดังนั้นไม่ว่าจะสร้างออบเจ็คขึ้นมาจากคลาส ScoreDb กี่ออบเจ็คก็ตาม
    // ตัวแปร mDbHelper ก็จะมีแค่ตัวเดียว และเป็น member ของคลาส ไม่ใช่ member ของออบเจ็คต่างๆเหล่านั้น
    private static DatabaseHelper mDbHelper;
    // ตัวแปรสำหรับอ้างอิงไปยังฐานข้อมูล (เป็นตัวแทนของฐานข้อมูล)
    private SQLiteDatabase mDatabase;

    public ScoreDb(Context context) {
        this.mContext = context;

        // สร้าง helper object ครั้งเดียวเท่านั้น ไม่ว่าจะนำคลาส ScoreDb ไป new เป็นออบเจ็คกี่ครั้งก็ตาม
        // (เราทำแบบนี้ได้เพราะประกาศ mDbHelper เป็น static)
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(context);
        }
        mDatabase = mDbHelper.getWritableDatabase();
    }

    // ใช้เพิ่มแถวข้อมูลใหม่ในเทเบิล scores
    public void insertScore(double score, int difficulty) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SCORE, score);
        cv.put(COL_DIFFICULTY, difficulty);
        mDatabase.insert(TABLE_NAME, null, cv);
    }

    // คัดเลือกแถวข้อมูลที่มีค่าในคอลัมน์ difficulty ตามที่ระบุ
    public Cursor selectScoreByDifficulty(int difficulty) {
        Cursor c = mDatabase.query(
                TABLE_NAME,
                null,
                COL_DIFFICULTY + "=?",
                new String[]{String.valueOf(difficulty)},
                null,
                null,
                COL_SCORE + " DESC",
                null
        );
        return c;
    }

    // Helper class
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sqlCreateTable = "CREATE TABLE " + TABLE_NAME + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_SCORE + " REAL,"
                    + COL_DIFFICULTY + " INTEGER)";

            db.execSQL(sqlCreateTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            return;
        }
    }
}
