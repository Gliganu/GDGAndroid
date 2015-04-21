package com.gliga.newscafe.data;

/**
 * Created by user on 2/15/2015.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.gliga.newscafe.ui.ArticleActivity;
import com.gliga.newscafe.ui.ArticleFragment;
import com.gliga.newscafe.ui.CategoryFragment;

import java.util.HashSet;
import java.util.Set;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

public class NewsProvider extends ContentProvider {

    public static final int ARTICLE = 100;

    public static final int ARTICLE_BY_CATEGORY = 200;

    public static final int CATEGORY = 300;

    public static final int ARTICLE_BY_CATEGORY_WITH_SEARCH = 400;

    private static final UriMatcher sUriMatcher = buildUriMatcher();


    private static final SQLiteQueryBuilder articleByCategoryQueryBuilder;
    private NewsDbHelper mOpenHelper;

    static {
        articleByCategoryQueryBuilder = new SQLiteQueryBuilder();
        articleByCategoryQueryBuilder.setTables(
                ArticleEntry.TABLE_NAME + " JOIN " +
                        CategoryEntry.TABLE_NAME +
                        " ON " + ArticleEntry.TABLE_NAME +
                        "." + ArticleEntry.COLUMN_CATEGORY_ID +
                        " = " + CategoryEntry.TABLE_NAME +
                        "." + CategoryEntry._ID);
    }

    private static final String sectionNameSelection =
            CategoryEntry.TABLE_NAME + "." +
                    CategoryEntry._ID + " = ? ";

    private static final String broadSearchSelection =
            ArticleEntry.COLUMN_TITLE + " LIKE '%' || ? || '%' OR " +
                    CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME + " LIKE '%' || ? || '%' ";


    /**
     * Building the uri matcher to do a mapping between the types of URIs and integers
     */
    private static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, NewsContract.PATH_ARTICLE, ARTICLE);
        matcher.addURI(authority, NewsContract.PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, NewsContract.PATH_CATEGORY + "/*/*", ARTICLE_BY_CATEGORY);
        matcher.addURI(authority, NewsContract.PATH_CATEGORY + "/*/*/*", ARTICLE_BY_CATEGORY_WITH_SEARCH);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ARTICLE_BY_CATEGORY:
                return ArticleEntry.CONTENT_TYPE;
            case ARTICLE:
                return ArticleEntry.CONTENT_TYPE;
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            case ARTICLE_BY_CATEGORY_WITH_SEARCH:
                return ArticleEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    /**
     * Get all the articles from a certain category
     */
    public Cursor getArticleByCategoryCursor(Uri uri, String[] projection, String selection, String[] selectionArgs) {

        return articleByCategoryQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

    }

    /**
     * Query the db for a subset of its entries
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {

            case ARTICLE_BY_CATEGORY: {

                Log.d(ArticleActivity.LOG_TAG,"In query, Article_By_Category: ");
                Cursor[] cursorArray = new Cursor[100];

                SharedPreferences prefs = ArticleFragment.prefs;

                Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());

                if (favouriteCategories.isEmpty()) {
                    retCursor = getArticleByCategoryCursor(uri, projection, sectionNameSelection, new String[]{""+(-1)});
                } else {
                    int i = 0;


                    for (String categoryId : favouriteCategories) {
                        cursorArray[i] = getArticleByCategoryCursor(uri, projection, sectionNameSelection, new String[]{categoryId});
                        i++;
                    }


                    retCursor = new MergeCursor(cursorArray);
                }

                break;

            }
            case ARTICLE: {
                Log.d(ArticleActivity.LOG_TAG,"In query, Article: ");
                retCursor = mOpenHelper.getReadableDatabase().query(ArticleEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case CATEGORY: {
                Log.d(ArticleActivity.LOG_TAG,"In query, Category: ");
                retCursor = mOpenHelper.getReadableDatabase().query(CategoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case ARTICLE_BY_CATEGORY_WITH_SEARCH: {


                Log.d(ArticleActivity.LOG_TAG,"In query, ARTICLE_BY_CATEGORY_WITH_SEARCH: ");
                String searchParameter = uri.getQueryParameter("q");


                retCursor = getArticleByCategoryCursor(uri, projection, broadSearchSelection, new String[]{searchParameter, searchParameter});

                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Insert an entry in a table
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        final int match = sUriMatcher.match(uri);

        Log.d(ArticleActivity.LOG_TAG, "In insert...");

        switch (match) {
            case ARTICLE: {
                long _id = db.insert(ArticleEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ArticleEntry.buildCategoryUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            case CATEGORY: {
                long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    /**
     * Delete an entry from a table
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowDeleted;
        //This makes delete all rows return the number of rows deleted
        if(selection == null) selection="1";

        switch (match) {
            case ARTICLE: {
                rowDeleted = db.delete(ArticleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case CATEGORY: {
                rowDeleted = db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        Log.d(ArticleActivity.LOG_TAG, "Numbers of deleted rows: " + rowDeleted);

        return rowDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        mOpenHelper.onUpgrade(mOpenHelper.getWritableDatabase(), 0, 0);

        return -1;
    }

    /**
     * Used to insert multiple entries in the db
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int returnCount = 0;

        switch (match) {
            case ARTICLE:
                returnCount = 0;
                try {
                    db.beginTransaction();
                    for (ContentValues value : values) {
                        long _id = db.insert(ArticleEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case CATEGORY:
                returnCount = 0;
                Log.d(ArticleActivity.LOG_TAG,"Chooses category in bulk insert....");
                try {
                    db.beginTransaction();
                    for (ContentValues value : values) {
                        long _id = db.insert(CategoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.d(ArticleActivity.LOG_TAG,"Introduced after bulk insert: "+returnCount);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }
}