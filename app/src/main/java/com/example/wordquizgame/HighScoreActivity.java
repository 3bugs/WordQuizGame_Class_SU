package com.example.wordquizgame;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;

import com.example.wordquizgame.db.ScoreDb;

public class HighScoreActivity extends AppCompatActivity {

    private static final String TAG = "HighScoreActivity";

/*
    private MyHelper mHelper;
    private SQLiteDatabase mDatabase;
*/
    private ListView list;
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        list = (ListView) findViewById(R.id.highScoreListView);

/*
        mHelper = new MyHelper(this);
        mDatabase = mHelper.getWritableDatabase();
*/

        mAdapter = new SimpleCursorAdapter(
                this,
                R.layout.high_score_row,
                null,
                new String[] {ScoreDb.COL_SCORE},
                new int[] {R.id.scoreTextView}
        );

        list.setAdapter(mAdapter);

        RadioGroup difficultyRadioGroup = (RadioGroup) findViewById(R.id.difficultyRadioGroup);
        difficultyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.easyRadioButton:
                        showHighScoreByDifficulty(0);
                        break;
                    case R.id.mediumRadioButton:
                        showHighScoreByDifficulty(1);
                        break;
                    case R.id.hardRadioButton:
                        showHighScoreByDifficulty(2);
                        break;
                }
            }
        });

/*
        while (c.moveToNext()) {
            double score = c.getDouble(c.getColumnIndex(MyHelper.COL_SCORE));
            int diff = c.getInt(c.getColumnIndex(MyHelper.COL_DIFFICULTY));

            Log.i(TAG, "Score: " + score + ", Difficulty: " + diff);
        }
*/
    }

    private void showHighScoreByDifficulty(int diff) {
/*
        Cursor c = mDatabase.query(
                MyHelper.TABLE_NAME,
                null,
                MyHelper.COL_DIFFICULTY + "=" + diff,
                null,
                null,
                null,
                MyHelper.COL_SCORE + " DESC",
                null
        );
*/
        ScoreDb db = new ScoreDb(this);
        Cursor c = db.selectScoreByDifficulty(diff);
        mAdapter.changeCursor(c);
    }
}
