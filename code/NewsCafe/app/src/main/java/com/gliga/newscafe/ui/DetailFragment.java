package com.gliga.newscafe.ui;

/**
 * Created by gliga on 3/8/2015.
 * Main fragment for the DetailActivity
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.tech.NfcBarcode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gliga.newscafe.R;

public class DetailFragment extends Fragment {

    public static final String SITE_URL = "siteUrl";
    private WebView detailWebView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        String urlToBeLoaded;
        if (arguments != null) {
            urlToBeLoaded = arguments.getString(DetailFragment.SITE_URL);
        }else{
            urlToBeLoaded = getActivity().getIntent().getStringExtra(SITE_URL);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        detailWebView = (WebView) rootView.findViewById(R.id.detail_web_view);


        if (savedInstanceState != null) {
            Log.d(ArticleActivity.LOG_TAG,"[Detail Fragment] Saved is not null. Resoring state...");
            detailWebView.restoreState(savedInstanceState);
        } else {

            Log.d(ArticleActivity.LOG_TAG,"Saved is null. Opening web page....");
            detailWebView.getSettings().setJavaScriptEnabled(true);


            detailWebView.setWebViewClient(new WebViewClient());


            detailWebView.loadUrl(urlToBeLoaded);

        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        detailWebView.saveState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);


        shareActionProvider.setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String url;

        if (getArguments() != null) {
            url = getArguments().getString(DetailFragment.SITE_URL);
        }else{
            url = getActivity().getIntent().getStringExtra(SITE_URL);
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome article " + url);
        return shareIntent;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String siteUrl);
    }

}