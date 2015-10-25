package com.example.wordquizgame;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.wordquizgame.db.MyHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";

    private int mNumChoices;

    private ArrayList<String> mFileNameList;
    private ArrayList<String> mQuizWordList;
    private ArrayList<String> mChoiceWordList;

    private int mScore;
    private int mTotalGuesses;
    private String mAnswerFileName;

    private Random mRandom;
    private Handler mHandler;

    private TextView mQuestionNumberTextView;
    private ImageView mQuestionImageView;
    private TableLayout mButtonTableLayout;
    private TextView mAnswerTextView;

    private MyHelper mHelper;
    private SQLiteDatabase mDatabase;
    private int mDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent i = getIntent();
        mDifficulty = i.getIntExtra(MainActivity.KEY_DIFFICULTY, 0);

        Log.i(TAG, "Difficulty: " + mDifficulty);

        switch (mDifficulty) {
            case 0:
                mNumChoices = 2;
                break;
            case 1:
                mNumChoices = 4;
                break;
            case 2:
                mNumChoices = 6;
                break;
        }

        Log.i(TAG, "Number of choices: " + mNumChoices);

        mFileNameList = new ArrayList<>();
        mQuizWordList = new ArrayList<>();
        mChoiceWordList = new ArrayList<>();

        mRandom = new Random();
        mHandler = new Handler();

        mHelper = new MyHelper(this);
        mDatabase = mHelper.getWritableDatabase();

        setupViews();
        getImageFileNames();
    }

    private void setupViews() {
        mQuestionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        mQuestionImageView = (ImageView) findViewById(R.id.questionImageView);
        mButtonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);
        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);
    }

    private void getImageFileNames() {
        String[] categories = {"animals", "body", "colors", "numbers", "objects"};

        AssetManager assets = getAssets();

        for (String category : categories) {
            try {
                String[] fileNames = assets.list(category);

                for (String f : fileNames) {
                    mFileNameList.add(f.replace(".png", ""));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error listing files in " + category);
            }
        }

        Log.i(TAG, "***** รายชื่อไฟล์ภาพทั้งหมด *****");
        for (String f : mFileNameList) {
            Log.i(TAG, f);
        }

        startQuiz();
    }

    private void startQuiz() {
        mTotalGuesses = 0;
        mScore = 0;
        mQuizWordList.clear();

        while (mQuizWordList.size() < 3) {
            int randomIndex = mRandom.nextInt(mFileNameList.size());
            String fileName = mFileNameList.get(randomIndex);

            if (mQuizWordList.contains(fileName) == false) {
                mQuizWordList.add(fileName);
            }
        }

        Log.i(TAG, "***** ชื่อไฟล์ที่สุ่มได้สำหรับตั้งโจทย์ *****");
        for (String f : mQuizWordList) {
            Log.i(TAG, f);
        }

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        mAnswerTextView.setText(null);
        mAnswerFileName = mQuizWordList.remove(0);

        String msg = String.format("คำถามข้อ %d จาก %d ข้อ", mScore + 1, 3);
        mQuestionNumberTextView.setText(msg);

        loadQuestionImage();
        prepareChoiceWords();
    }

    private void loadQuestionImage() {
        String category = mAnswerFileName.substring(0, mAnswerFileName.indexOf('-'));
        String filePath = category + "/" + mAnswerFileName + ".png";

        AssetManager assets = getAssets();
        InputStream stream;

        try {
            stream = assets.open(filePath);
            Drawable image = Drawable.createFromStream(stream, filePath);
            mQuestionImageView.setImageDrawable(image);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error opening file: " + filePath);
        }
    }

    private void prepareChoiceWords() {
        mChoiceWordList.clear();

        while (mChoiceWordList.size() < mNumChoices) {
            int randomIndex = mRandom.nextInt(mFileNameList.size());
            String randomWord = getWord(mFileNameList.get(randomIndex));
            String answerWord = getWord(mAnswerFileName);

            if (mChoiceWordList.contains(randomWord) == false &&
                    randomWord.equals(answerWord) == false) {
                mChoiceWordList.add(randomWord);
            }
        }

        int randomIndex = mRandom.nextInt(mChoiceWordList.size());
        mChoiceWordList.set(randomIndex, getWord(mAnswerFileName));

        Log.i(TAG, "***** คำศัพท์ตัวเลือกที่สุ่มได้ *****");
        for (String w : mChoiceWordList) {
            Log.i(TAG, w);
        }

        createChoiceButtons();
    }

    private void createChoiceButtons() {
        for (int row = 0; row < mButtonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);
            tr.removeAllViews();
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        for (int row = 0; row < mNumChoices / 2; row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);

            for (int column = 0; column < 2; column++) {
                Button guessButton = (Button) inflater.inflate(R.layout.guess_button, tr, false);
                guessButton.setText(mChoiceWordList.remove(0));
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitGuess((Button) v);
                    }
                });

                tr.addView(guessButton);
            }
        }
    }

    private void submitGuess(Button guessButton) {
        String guessWord = guessButton.getText().toString();
        String answerWord = getWord(mAnswerFileName);

        mTotalGuesses++;

        // ตอบถูก
        if (guessWord.equals(answerWord)) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.applause);
            mp.start();

            mScore++;

            String msg = guessWord + " ถูกต้องนะคร้าบบบ";
            mAnswerTextView.setText(msg);
            mAnswerTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            disableAllButtons();

            // ตอบถูกและจบเกม (เล่นครบ 3 ข้อแล้ว)
            if (mScore == 3) {
                saveScore();

                String msgResult = String.format(
                        "จำนวนครั้งที่ทาย: %d\nเปอร์เซ็นต์ความถูกต้อง: %.1f",
                        mTotalGuesses,
                        (100 * 3) / (double) mTotalGuesses
                );

                new AlertDialog.Builder(this)
                        .setTitle("สรุปผล")
                        .setMessage(msgResult)
                        .setCancelable(false)
                        .setPositiveButton("เริ่มเกมใหม่", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startQuiz();
                            }
                        })
                        .setNegativeButton("กลับหน้าหลัก", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
            // ตอบถูก แต่ยังไม่จบเกม (ยังไม่ครบ 3 ข้อ)
            else {
                mHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                loadNextQuestion();
                            }
                        },
                        2000
                );
            }

        }
        // ตอบผิด
        else {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.fail3);
            mp.start();

            String msg = "ผิดครับ ลองใหม่นะครับ";
            mAnswerTextView.setText(msg);
            mAnswerTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            guessButton.setEnabled(false);
        }
    }

    private void saveScore() {
        ContentValues cv = new ContentValues();
        cv.put(MyHelper.COL_SCORE, 100 * 3 / (double) mTotalGuesses);
        cv.put(MyHelper.COL_DIFFICULTY, mDifficulty);

        mDatabase.insert(MyHelper.TABLE_NAME, null, cv);
    }

    private void disableAllButtons() {
        for (int row = 0; row < mButtonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) mButtonTableLayout.getChildAt(row);

            for (int column = 0; column < tr.getChildCount(); column++) {
                tr.getChildAt(column).setEnabled(false);
            }
        }
    }

    private String getWord(String fileName) {
        String word = fileName.substring(fileName.indexOf('-') + 1);
        return word;
    }
}