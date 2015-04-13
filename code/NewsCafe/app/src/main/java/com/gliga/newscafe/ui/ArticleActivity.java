package com.gliga.newscafe.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gliga.newscafe.R;
import com.gliga.newscafe.data.NewsContract;
import com.gliga.newscafe.data.NewsProvider;
import com.gliga.newscafe.data.NewsSyncAdapter;

import org.apache.http.entity.ContentProducer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.ArticleEntry.*;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;


/**
 * Activity used to display the main lists of articles available to read
 */
public class ArticleActivity extends ActionBarActivity implements DetailFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String DETAIL_FRAGMENT_PRESENT = "detailFragmentPresent";
    public static final String FIRST_TIME = "firstTime";

    public static boolean mTwoPane;


    public static final String LOG_TAG = "Gliga Debug";

    private static ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        if (findViewById(R.id.article_detail_container_wide) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.article_detail_container_wide, new TemplateFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }

            else {
               savedInstanceState.getBoolean(DETAIL_FRAGMENT_PRESENT);
           }

        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }


        NewsSyncAdapter.initializeSyncAdapter(this);

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;

        Bundle bundle = new Bundle();
        bundle.putBoolean(DETAIL_FRAGMENT_PRESENT,true);
        onSaveInstanceState(bundle);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

            Log.d(ArticleActivity.LOG_TAG, "SUCCESS");
            Log.d(ArticleActivity.LOG_TAG, "Value is: " + savedInstanceState.getBoolean(DETAIL_FRAGMENT_PRESENT));

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(DETAIL_FRAGMENT_PRESENT,true);
        super.onSaveInstanceState(outState, outPersistentState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.see_categories_item) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(String siteUrl) {
        if (mTwoPane) {
            Log.d(ArticleActivity.LOG_TAG, "Its the two pane way");

            Bundle args = new Bundle();
            args.putString(DetailFragment.SITE_URL, siteUrl);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            int commit = getSupportFragmentManager().beginTransaction()
                    .replace(R.id.article_detail_container_wide, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();


            Log.d(ArticleActivity.LOG_TAG, "Commit id is:" + commit);

        } else {


            Log.d(ArticleActivity.LOG_TAG, "Its the one pane way");
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.SITE_URL, siteUrl);
            startActivity(intent);
        }
    }

    /**
     * Show the loading dialog when data is being brought from the net
     * @param context
     */
    public static void showDialog(final Context context) {

        progressDialog = new ProgressDialog(context);


        progressDialog.setMessage("Bringing news...");
        progressDialog.setTitle("Loading...");

        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.d(ArticleActivity.LOG_TAG, "Clicked on Cancel button");
                ArticleFragment articleFragment = (ArticleFragment) ((ArticleActivity) context).getSupportFragmentManager().findFragmentById(R.id.article_fragment);
                Log.d(ArticleActivity.LOG_TAG, "Resetting loader because Cancel button pressed");


                ContentResolver.cancelSync(NewsSyncAdapter.getSyncAccount(context), context.getString(R.string.content_authority));
                articleFragment.resetLoader();

                dialog.dismiss();
            }
        });

        progressDialog.show();

    }

    /**
     * Hide the dialog
     */
    public static void hideDialog() {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;

    }

    /**
     * Choose what type of information to get form the API. Category information or Article information.
     * This is needed because they come under different forms of JSON and we need different types of parsers for each one of them
     */
    public static void setInformationMode(Context context, boolean articleTypeFetch) {

        SharedPreferences prefs = ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();
        Log.d(ArticleActivity.LOG_TAG, "Changing information mode to:" + articleTypeFetch);
        editor.putBoolean(ArticleFragment.INFORMATION_FETCH_TYPE, articleTypeFetch);
        editor.apply();
    }


    /**
     * Reset all to factory settings. This means deleting the preferences and all cached articles from the db
     * @param context
     */
    public static void resetToFactorySettings(Context context) {

        Log.d(ArticleActivity.LOG_TAG, "Reseting to factory settings.....");

        SharedPreferences prefs = ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();


        editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());
        editor.putStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());
        editor.putBoolean(ArticleFragment.SEARCH_ACTIVATED, false);
        editor.putString(context.getResources().getString(R.string.pref_keep_memory_key), "1");
        editor.putString(context.getResources().getString(R.string.pref_often_update_key), "1");
        editor.putBoolean(context.getResources().getString(R.string.pref_enable_notifications_key), true);
        editor.apply();

        context.getContentResolver().update(ArticleEntry.CONTENT_URI, null, null, null); // deleted all the articles
        context.getContentResolver().update(CategoryEntry.CONTENT_URI, null, null, null); // deleted all the categories

    }
}