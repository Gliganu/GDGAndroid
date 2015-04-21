package com.gliga.newscafe.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gliga.newscafe.ui.ArticleActivity;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/7/2015.
 */
public class NewsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "news.db";

    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * Create the tables in the database
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " ( " +
                CategoryEntry._ID + " TEXT PRIMARY KEY ON CONFLICT IGNORE ," +
                CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME + " TEXT NOT NULL " +
                ");";


        final String SQL_CREATE_ARTICLE_TABLE = "CREATE TABLE " + ArticleEntry.TABLE_NAME + " ( " +

                ArticleEntry._ID + " TEXT PRIMARY KEY ON CONFLICT IGNORE ," +

                // the ID of the location entry associated with this weather data
                ArticleEntry.COLUMN_PUBLISH_DATE + " TEXT NOT NULL, " +
                ArticleEntry.COLUMN_SOURCE_URL + " TEXT NOT NULL," +
                ArticleEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                ArticleEntry.COLUMN_URL + " TEXT NOT NULL," +
                ArticleEntry.COLUMN_CATEGORY_ID + " TEXT NOT NULL," +

                " UNIQUE (" + ArticleEntry.COLUMN_TITLE + ") ON CONFLICT IGNORE " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + ArticleEntry.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + "(" + CategoryEntry._ID + ")) ; ";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ARTICLE_TABLE);
        Log.d(ArticleActivity.LOG_TAG, "Creating new db...");
    }

    /**
     * Upgrade the database. It does this by deleting the previous db, and recreating it
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ArticleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
