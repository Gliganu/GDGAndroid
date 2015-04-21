package com.gliga.newscafe.data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gliga.newscafe.R;
import com.gliga.newscafe.ui.ArticleActivity;
import com.gliga.newscafe.ui.ArticleFragment;
import com.gliga.newscafe.ui.CategoryFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/26/2015.
 */
public class NewsSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOG_TAG = "Gliga Debug";
    public static final String CATEGORIES_TYPE_JSON = "categories";
    public static final String ARTICLES_TYPE_JSON = "articles";
    private static final int NEWS_NOTIFICATION_ID = 3004;

    private static int notificatioNumber = 0;

    public static final String BASE_URL = "http://content.guardianapis.com";

    public static final int SYNC_INTERVAL = 60 * 180;

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private static ProgressDialog progressDialog;

    private Fragment fragment;

    private Context context;

    public NewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        SharedPreferences prefs =ArticleFragment.prefs;
        Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());
        Set<String> categoriesStoredInDatabase = prefs.getStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());

        SharedPreferences.Editor editor = prefs.edit();

        boolean articleFetch = prefs.getBoolean(ArticleFragment.INFORMATION_FETCH_TYPE, true);
        boolean firstTime = prefs.getBoolean(ArticleActivity.FIRST_TIME,false);


        //sendNotification();

        Log.d(ArticleActivity.LOG_TAG, "Article Fetch:" + articleFetch);

        String newsUrl = "";
        String uri;

        if (!articleFetch) { //we are bringing categories not articles
            uri = "/sections?api-key=sd6tb3e6rp7qa4e3f93ybgjq";
            newsUrl = BASE_URL + uri;

            getDataFromApi(newsUrl, uri, articleFetch);

        } else {//we are bringing articles not categories

            Log.d(ArticleActivity.LOG_TAG, "NSA: Favourite" + favouriteCategories.toString());
            Log.d(ArticleActivity.LOG_TAG, "NSA: Stored" + categoriesStoredInDatabase.toString());


            for (String neededCategory : favouriteCategories) {
                if (!categoriesStoredInDatabase.contains(neededCategory)) {

                    ArticleFragment.newNewsBrought = true;

                    uri = "/search?api-key=sd6tb3e6rp7qa4e3f93ybgjq&section="+neededCategory;
                    newsUrl = BASE_URL + uri;
                    getDataFromApi(newsUrl, uri, articleFetch);
                }

            }

        }


       if(firstTime){
           Log.d(ArticleActivity.LOG_TAG, "NSA: First time: Loading articles as well");

            articleFetch = true;

           for (String neededCategory : favouriteCategories) {

               if (!categoriesStoredInDatabase.contains(neededCategory)) {

                   uri = "/search?api-key=sd6tb3e6rp7qa4e3f93ybgjq&section="+neededCategory;
                   newsUrl = BASE_URL + uri;
                   getDataFromApi(newsUrl, uri, articleFetch);
               }

           }

           editor.putBoolean(ArticleActivity.FIRST_TIME,false);
           editor.apply();
       }

        Log.d(ArticleActivity.LOG_TAG, "Finished bringing data. Sending broadcast");
        getContext().sendBroadcast(new Intent(ArticleFragment.ACTION_FINISHED_SYNC));

    }

    public void getDataFromApi(String newsUrl, String uri, boolean articleFetch) {


        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;


        Log.d(LOG_TAG, "Started to bring news.... ");
        try {
            URL url = new URL(Uri.parse(newsUrl).toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.d(LOG_TAG, "Connected to url ");

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + " \n ");
            }

            if (buffer.length() == 0) {
                return;
            }


            String newsJsonString = buffer.toString();

            if (!articleFetch) {
                getCategoriesDataFromJson(newsJsonString);
            } else {
                getArticlesDataFromJson(newsJsonString);
            }

            deleteOldNews();



        } catch (IOException e) {
            Log.e(LOG_TAG, "Error from IO Exception " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

    }


    public void deleteOldNews() {

        Cursor cursor =  getContext().getContentResolver().query(ArticleEntry.CONTENT_URI, null, null, null, null);

        cursor.moveToFirst();

        Pattern p = Pattern.compile("(\\d{1,2}\\s\\w{3}\\s\\d{4})");
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        SharedPreferences prefs = ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> storedCategories = prefs.getStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());

        String keepMemory = prefs.getString( getContext().getResources().getString(R.string.pref_keep_memory_key),
                "5");

        int weeksToKeepInMemory = Integer.parseInt(keepMemory.substring(0, 1));
        int daysToKeepInMemory = weeksToKeepInMemory * 7;


        Date currentDate = new Date(System.currentTimeMillis());

        while (cursor.moveToNext()) {
            int indexPublishDate = cursor.getColumnIndex(ArticleEntry.COLUMN_PUBLISH_DATE);
            int indexTitle = cursor.getColumnIndex(ArticleEntry.COLUMN_TITLE);
            int indexCategoryNumber = cursor.getColumnIndex(ArticleEntry.COLUMN_CATEGORY_ID);

            String publishDate = cursor.getString(indexPublishDate);
            String title = cursor.getString(indexTitle);
            int categoryId = cursor.getInt(indexCategoryNumber);


            Matcher m = p.matcher(publishDate);

            while (m.find()) {

                Date articlePublishDate = new Date(m.group());
               // Log.d(ArticleActivity.LOG_TAG,"Article Publish date: " + publishDate);

                Calendar c = Calendar.getInstance();
                c.setTime(articlePublishDate);
                c.add(Calendar.DATE, daysToKeepInMemory);  // number of days to add
                Date deleteDate = c.getTime();


                //Log.d(ArticleActivity.LOG_TAG, title.substring(0,5) +" -- " +"Current: "+ currentDate+" || Delete: " + deleteDate +" || Article: " + articlePublishDate);

                if (deleteDate.before(currentDate)) {
                    Log.d(ArticleActivity.LOG_TAG, "Deleting:" + title);
                    Log.d(ArticleActivity.LOG_TAG, "Publish Date:" + formatter.format(articlePublishDate));
                    Log.d(ArticleActivity.LOG_TAG, "Delete Date:" + formatter.format(deleteDate));

                    getContext().getContentResolver().delete(ArticleEntry.CONTENT_URI, ArticleEntry.COLUMN_CATEGORY_ID + " = ? ",
                            new String[]{"" + categoryId});


                    Log.d(ArticleActivity.LOG_TAG, "Removoved from stored categories:" + formatter.format(deleteDate));
                    storedCategories.remove("" + categoryId);

                }

            }


        }

        Log.d(ArticleActivity.LOG_TAG, "AFTER DELETION CURRENTLY STORED: " + storedCategories);
        editor.putStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, storedCategories);
        editor.apply();
        cursor.close();

    }


    private void getArticlesDataFromJson(String newsJsonString) {

        final String ID = "id";
        final String SECTION_ID = "sectionId";
        final String WEB_TITLE = "webTitle";
        final String SOURCE_URL = "webUrl";
        final String URL = "apiUrl";

        SharedPreferences prefs = ArticleFragment.prefs;
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> categoriesStoredInDatabase = prefs.getStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        try {

            JSONObject articleObject = new JSONObject(newsJsonString);

            JSONObject response = articleObject.getJSONObject("response");

            JSONArray articleArray = response.getJSONArray("results");

            Vector<ContentValues> cVVector = new Vector<ContentValues>(articleArray.length());

            String categoryId = "";

            for (int i = 0; i < 5; i++) {

                JSONObject article = articleArray.getJSONObject(i);

                String publishDate = formatter.format(new Date(System.currentTimeMillis()));

                String articleId = article.getString(ID);
                categoryId = article.getString(SECTION_ID);
                String title = article.getString(WEB_TITLE);
                String sourceUrl = article.getString(SOURCE_URL);
                String url = article.getString(URL);

                ContentValues categoryValues = new ContentValues();

                categoryValues.put(ArticleEntry._ID, articleId);
                categoryValues.put(ArticleEntry.COLUMN_PUBLISH_DATE, publishDate);
                categoryValues.put(ArticleEntry.COLUMN_SOURCE_URL, sourceUrl);
                categoryValues.put(ArticleEntry.COLUMN_TITLE, title);
                categoryValues.put(ArticleEntry.COLUMN_URL, url);
                categoryValues.put(ArticleEntry.COLUMN_CATEGORY_ID, categoryId);

                Log.d(ArticleActivity.LOG_TAG, "From JSON: ( Article )  " + title);

                cVVector.add(categoryValues);

            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(ArticleEntry.CONTENT_URI, cvArray);


            }


            Log.d(LOG_TAG, "Added to stored categories from sync adapter: " + categoryId);
            categoriesStoredInDatabase.add(categoryId);

            Log.d(ArticleActivity.LOG_TAG, "Putting into stored: NSA " + categoriesStoredInDatabase);

            editor.putStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, categoriesStoredInDatabase);

            editor.apply();

            Log.d(LOG_TAG, "Sync Complete for articles. " + cVVector.size() + " Inserted");


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCategoriesDataFromJson(String newsJsonString) {

        Log.d(ArticleActivity.LOG_TAG,"Parsing sections...");

        try {

            JSONObject genericObject = new JSONObject(newsJsonString);

            JSONObject response = genericObject.getJSONObject("response");

            JSONArray results = response.getJSONArray("results");

            Vector<ContentValues> cVVector = new Vector<ContentValues>(results.length());

            for (int i = 0; i < results.length(); i++) {

                JSONObject category = results.getJSONObject(i);
                String categoryId = category.getString("id");
                String categoryWebTitle = category.getString("webTitle");


//      These are the values that will be collected.

                ContentValues categoryValues = new ContentValues();

                categoryValues.put(CategoryEntry._ID, categoryId);
                categoryValues.put(CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME, categoryWebTitle);

                Log.d(ArticleActivity.LOG_TAG, "From JSON: (Category)  " + categoryWebTitle);

                cVVector.add(categoryValues);

            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(CategoryEntry.CONTENT_URI, cvArray);

            }

            Log.d(ArticleActivity.LOG_TAG, "All good so far");


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static void readCategoriesFromDb(Context context) {


        Cursor cursor = context.getContentResolver().query(CategoryEntry.CONTENT_URI, null, null, null, null);

        cursor.moveToFirst();

        while (cursor.moveToNext()) {

            String sectionId = cursor.getString(CategoryEntry.COLUMN__ID_INDEX);
            String sectionName = cursor.getString(CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME_INDEX);

            Log.d(ArticleActivity.LOG_TAG, "From DB:(Category) --- " + sectionName);
        }

        cursor.close();
    }

    public static void readArticlesFromDb(Context context) {
        Cursor cursor = context.getContentResolver().query(ArticleEntry.CONTENT_URI, null, null, null, null);

        cursor.moveToFirst();

        while (cursor.moveToNext()) {

            String title = cursor.getString(ArticleEntry.COLUMN_TITLE_INDEX);


            Log.d(ArticleActivity.LOG_TAG, "From DB ( Article ) " + title);
        }

        cursor.close();
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        NewsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public  void sendNotification() {

        Context context = getContext();

        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs =ArticleFragment.prefs;

        String displayNotificationKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if (!displayNotifications) {
            return;
        }

        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);


        if (System.currentTimeMillis() - lastSync >= 1000 * 60 * 60 * 24) {

                // this will be routed to onNewIntent(), SendToGCMActivity is this class
                Intent onNotificationDiscarded = new Intent(context,ArticleActivity.class);
                PendingIntent notificationIntent = PendingIntent.getActivity(context, 0, onNotificationDiscarded, 0);
                Notification notification = null;

                // notificationCounter is a private static AtomicInteger
                int notificationNumber = notificatioNumber;
                notificatioNumber++;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("News Cafe")
                        .setContentText("Find out what happened today in the world!")
                        .setContentIntent(notificationIntent)
                        .setDeleteIntent(PendingIntent.getActivity(context, 0, onNotificationDiscarded, 0))
                                // update the counter
                        .setNumber(notificationNumber);

                if (Build.VERSION.SDK_INT > 15) {
                    // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
                    builder.setPriority(Notification.PRIORITY_HIGH);
                    notification = builder.build();
                } else {
                    notification = builder.getNotification();
                }

                // notifications disappear after the user taps on them
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // cancel previous notification to clean up garbage in the status bar
                nm.cancel(notificationNumber - 1);
                // add new notification
                nm.notify(notificationNumber, notification);

            //refreshing last sync
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(lastNotificationKey, System.currentTimeMillis());
            editor.apply();

        }
    }



}


