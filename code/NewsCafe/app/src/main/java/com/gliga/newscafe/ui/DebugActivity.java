package com.gliga.newscafe.ui;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gliga.newscafe.R;
import com.gliga.newscafe.data.NewsContract;
import com.gliga.newscafe.data.NewsSyncAdapter;

import java.util.HashSet;
import java.util.Set;

public class DebugActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }


    public void bringCategoriesFromApi(View view) {

        SharedPreferences prefs =ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ArticleFragment.INFORMATION_FETCH_TYPE, false);
        editor.apply();

        Log.d(ArticleActivity.LOG_TAG, "Bringing sections from api...");

        NewsSyncAdapter.syncImmediately(this);
    }

    public void bringArticlesFromApi(View view) {

        SharedPreferences prefs =ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ArticleFragment.INFORMATION_FETCH_TYPE, true);
        editor.apply();

        Log.d(ArticleActivity.LOG_TAG, "Bringing articles from api...");

        NewsSyncAdapter.syncImmediately(this);
    }

    public void clearPrefs(View view) {

        SharedPreferences prefs =ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(ArticleFragment.INFORMATION_FETCH_TYPE, false);
        editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());
        editor.putStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());

        editor.apply();

    }


    public void sendNotification(View view) {

//         NewsSyncAdapter.sendNotification(this);

    }



    public void readCategoriesFromDb(View view) {
        NewsSyncAdapter.readCategoriesFromDb(this);
    }

   public void readArticlesFromDb(View view) {
        NewsSyncAdapter.readArticlesFromDb(this);
    }

    public void deleteAllFromDb(View view) {

        getContentResolver().update(NewsContract.ArticleEntry.CONTENT_URI, null, null, null); // deleted all the articles
        getContentResolver().update(NewsContract.CategoryEntry.CONTENT_URI, null, null, null); // deleted all the categories

    }
}
