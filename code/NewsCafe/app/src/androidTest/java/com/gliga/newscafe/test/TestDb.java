package com.gliga.newscafe.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.gliga.newscafe.data.NewsContract;
import com.gliga.newscafe.data.NewsDbHelper;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 4/16/2015.
 */
public class TestDb extends AndroidTestCase {



    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(NewsDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new NewsDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }



    public void testInsertReadCategoryTable(){

        NewsDbHelper dbHelper = new NewsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        assertEquals(true, db.isOpen());

        ContentValues categoryValues = new ContentValues();

        categoryValues.put(ArticleEntry.COLUMN_PUBLISH_DATE, "12-20-2004");
        categoryValues.put(ArticleEntry.COLUMN_SOURCE, "test-source");
        categoryValues.put(ArticleEntry.COLUMN_SOURCE_URL, "test-source-url");
        categoryValues.put(ArticleEntry.COLUMN_SUMMARY, "test-summary");
        categoryValues.put(ArticleEntry.COLUMN_TITLE, "test-title");
        categoryValues.put(ArticleEntry.COLUMN_URL, "test-url");
        categoryValues.put(ArticleEntry.COLUMN_CATEGORY_ID, 12);


        long locationRowId = db.insert(ArticleEntry.TABLE_NAME, null, categoryValues);

        assertTrue("Location entry error",locationRowId != -1);

        Cursor articleCursor = db.query(
                ArticleEntry.TABLE_NAME,  // Table to Query
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue("Error: No records returned from article query ", articleCursor.moveToFirst());

        assertFalse("Error: More than one record returned from the article query ", articleCursor.moveToNext());

        articleCursor.close();
        dbHelper.close();
    }


}
