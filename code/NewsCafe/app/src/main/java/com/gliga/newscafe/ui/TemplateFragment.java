package com.gliga.newscafe.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gliga.newscafe.R;

/**
 * This is a fragment used as an initial placeholder for the detail web view for the tablet-like deviced. Initially, when the users starts the application,
 * no articles is selected and this fragment appears in the right sidfe of the screen. After the user selects an article, this fragment will be
 * replaced by a DetailFragment
 */
public class TemplateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_template, container, false);

    }
}
