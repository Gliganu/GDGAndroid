package com.gliga.newscafe.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gliga.newscafe.R;
import com.gliga.newscafe.data.NewsContract;
import com.gliga.newscafe.data.NewsSyncAdapter;

import java.util.HashSet;
import java.util.Set;

import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/7/2015.
 * Main fragment  of the article activity
 */

public class ArticleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String SEARCH_TEXT = "searchText";


    public static final int ARTICLE_LOADER = 1;
    public static final String SEARCH_TEXT_PREF_KEY = "searchTextPrefKey";
    public static final String CURRENT_POSITION = "currentPosition";
    public static final String INFORMATION_FETCH_TYPE = "infoFetchType";
    public static final String SEARCH_ACTIVATED = "searchActivated";
    public static final String ACTION_FINISHED_SYNC = "your.package.ACTION_FINISHED_SYNC";

    private static int currentPosition = -1;

    public static boolean newNewsBrought;

    public static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;

    private ArticleAdapter articleAdapter;
    private ListView listView;
    private String searchText;

    private static IntentFilter syncIntentFilter = new IntentFilter(ACTION_FINISHED_SYNC);

    /**
     * When the sync adapter finishes bringing data from the internet, it broadcasts an intent to let the UI know that it needs to refresh
     * in order to display the new information. This is used for cathing that event
     */
    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(ArticleActivity.LOG_TAG, "Broadcast received...");
            ArticleActivity.hideDialog();
            Log.d(ArticleActivity.LOG_TAG, "Resetting loader because Broadcast received");
            resetLoader();


        }
    };


    public ArticleFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(syncBroadcastReceiver, syncIntentFilter);

        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editor = prefs.edit();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        articleAdapter = new ArticleAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_article, container, false);

        listView = (ListView) rootView.findViewById(R.id.article_list_view);
        listView.setAdapter(articleAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                currentPosition = position;


                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {

                    ((DetailFragment.Callback) getActivity())
                            .onItemSelected(cursor.getString(NewsContract.ALL_COLLUMN_SOURCE_URL_INDEX));

                }
            }
        });


        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!checkFirstTimeRunning()) {
            Log.d(ArticleActivity.LOG_TAG, "Not the first time running");
            ArticleActivity.setInformationMode(getActivity(), true);

            TextView infoTextView = (TextView) getActivity().findViewById(R.id.info_text_view);
            infoTextView.setVisibility(View.GONE);

            ArticleActivity.setInformationMode(getActivity(), true);
            ArticleActivity.showDialog(getActivity());


            Log.d(ArticleActivity.LOG_TAG, "[Article Fragment - onActivityCreated] Sync called");
            NewsSyncAdapter.syncImmediately(getActivity());
        }


        if (savedInstanceState != null) {

            if (savedInstanceState.getString(SEARCH_TEXT) != null) {
                TextView searchView = (TextView) getView().findViewById(R.id.info_text_view);
                searchView.setText(savedInstanceState.getString(SEARCH_TEXT));
            }

        }

        boolean searchChosen = prefs.getBoolean(SEARCH_ACTIVATED, false);

        if(searchChosen){
            startSearch();
        }


    }

    /**
     * If this is the first time the application is started, then special actions need to be performed, such as bringing all the categories information
     * from the api.
     *
     * @return
     */
    private boolean checkFirstTimeRunning() {

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());
        Set<String> storedCategories = prefs.getStringSet(CategoryFragment.CURRENTLY_STORED_CATEGORIES, new HashSet<String>());

        TextView infoTextView = (TextView) getActivity().findViewById(R.id.info_text_view);
        infoTextView.setVisibility(View.GONE);

        if (!favouriteCategories.isEmpty() || !storedCategories.isEmpty()) {
            return false;
        }

        Log.d(ArticleActivity.LOG_TAG, "First time running...");
        // SharedPreferences.Editor editor = prefs.edit();

        favouriteCategories.add("science");

        editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES, favouriteCategories);
        editor.putBoolean(ArticleActivity.FIRST_TIME, true);
        editor.apply();


        ArticleActivity.showDialog(getActivity());
        ArticleActivity.setInformationMode(getActivity(), false);

        Log.d(ArticleActivity.LOG_TAG, "[Article Fragment - check first] Sync called");
        NewsSyncAdapter.syncImmediately(getActivity());

        return true;

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_article_fragment, menu);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        TextView searchView = (TextView) getView().findViewById(R.id.info_text_view);
        outState.putString(SEARCH_TEXT, (String) searchView.getText());
        outState.putInt(CURRENT_POSITION, currentPosition);

    }


    public boolean hideSearch() {
        final TextView infoTextView = (TextView) getActivity().findViewById(R.id.info_text_view);

        if (infoTextView.getVisibility() == View.VISIBLE) {

            infoTextView.setVisibility(View.GONE);
            editor.putBoolean(SEARCH_ACTIVATED, false);
            editor.apply();
            getLoaderManager().restartLoader(ARTICLE_LOADER, null, ArticleFragment.this);
            return true;
        }

        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncBroadcastReceiver, syncIntentFilter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri articleByCategoryUri = CategoryEntry.buildArticlesByCategoryUri("x");
        Uri articleByCategoryWithSearchUri = CategoryEntry.buildArticlesByCategoryWithSearchUri("x", searchText);
        Uri usedUri;


        boolean searchChosen = prefs.getBoolean(SEARCH_ACTIVATED, false);

        if (searchChosen) {
            usedUri = articleByCategoryWithSearchUri;
            editor.putBoolean(SEARCH_ACTIVATED, false);

        } else {
            usedUri = articleByCategoryUri;
        }


        return new CursorLoader(getActivity(),
                usedUri,
                NewsContract.ALL_NEWS_COLUMNS,
                null,
                null,
                null);
    }

    /**
     * When the load is completed, if no articles were brought, then the app notifies the user of that.
     * It also scrolls to the last article he read in the list, and if it's a tablet, then it keeps it pressed. This is necessary for example at landscape changes
     *
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.getCount() == 0) {
            setInfoButtonText("No articles found to match your criteria", true);
        } else {
            setInfoButtonText("Enjoy the news", false);
        }

        articleAdapter.swapCursor(cursor);


        if(newNewsBrought == true){
            currentPosition = -1;
            newNewsBrought = false;
        }

        listView.smoothScrollToPosition(currentPosition);

        if (currentPosition != -1 && ArticleActivity.mTwoPane) {
            listView.setItemChecked(currentPosition, true);
        }

        if (currentPosition == -1 && ArticleActivity.mTwoPane) {

            currentPosition = 0;

            final int WHAT = 1;
            Handler handler = new Handler() { //TODO FIX THIS
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == WHAT) {
                        listView.performItemClick(
                                listView.getAdapter().getView(currentPosition, null, null),
                                currentPosition,
                                listView.getAdapter().getItemId(currentPosition));
                    }
                }
            };

            handler.sendEmptyMessage(WHAT);



            listView.setItemChecked(currentPosition, true);
        }


    }

    public void resetLoader() {
        getLoaderManager().initLoader(ARTICLE_LOADER, null, this);
    }


    public void startSearch(){

        this.searchText =  prefs.getString(SEARCH_TEXT_PREF_KEY,"");

        editor.putBoolean(SEARCH_ACTIVATED, true);
        editor.apply();

        getLoaderManager().restartLoader(ARTICLE_LOADER, null, ArticleFragment.this);

    }

    /**
     * The info panel appears when the user performs a search. It is used to let him know about what he has searhed and if he clicks on it then the previous
     * results are being brought back
     */
    public void setInfoButtonText(String text, boolean visible) {

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // final SharedPreferences.Editor editor = prefs.edit();

        final boolean searchChosen = prefs.getBoolean(SEARCH_ACTIVATED, false);

        final TextView infoTextView = (TextView) getActivity().findViewById(R.id.info_text_view);

        if (visible) {
            infoTextView.setVisibility(View.VISIBLE);
        } else {
            infoTextView.setVisibility(View.GONE);
        }

        if (searchChosen) {
            infoTextView.setVisibility(View.VISIBLE);
            Log.d(ArticleActivity.LOG_TAG, "Search chosen => " + text);

            if (searchText != null) {
                infoTextView.setText(getActivity().getResources().getString(R.string.your_search) + " \"" + searchText +"\"");
            }

        } else {
            infoTextView.setText(text);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        articleAdapter.swapCursor(null);
    }


}
