package com.gliga.newscafe.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gliga.newscafe.R;

/**
 * Activity used for the settings
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_often_update_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_keep_memory_key)));

        SharedPreferences prefs = ArticleFragment.prefs;

        String oftenUpdate = prefs.getString(getString(R.string.pref_often_update_key),
                "Default often keep memory");

        String keepMemory = prefs.getString(getResources().getString(R.string.pref_keep_memory_key),
                "Default often keep memory");

        Log.d(ArticleActivity.LOG_TAG, "Often update: " + oftenUpdate);
        Log.d(ArticleActivity.LOG_TAG, "Keep memory: " + keepMemory);


        Preference button = findPreference("button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage(
                        "Are you sure you want to reset to factory settings?")
                        .setCancelable(true)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        ArticleActivity.resetToFactorySettings(SettingsActivity.this);
                                        Intent intent = new Intent(SettingsActivity.this,ArticleActivity.class);
                                        startActivity(intent);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {

                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });


    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        Log.d(ArticleActivity.LOG_TAG,"Got in onPreferenceChange");
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            Log.d(ArticleActivity.LOG_TAG,"is instance of list preferece");

            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
                Log.d(ArticleActivity.LOG_TAG,"pref index > 0 ");



            }


        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
            Log.d(ArticleActivity.LOG_TAG,"not list preferecnce");
        }


        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}