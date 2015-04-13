package com.gliga.newscafe.ui;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.gliga.newscafe.R;

import java.util.HashSet;
import java.util.Set;

import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/7/2015.
 * Main fragment for the CategoryActivity
 */
public class CategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FAVOURITE_CATEGORIES = "favouriteCategories";
    public static final String CURRENTLY_STORED_CATEGORIES = "currentCategoriesInDb";

    private static final int CATEGORY_LOADER = 0;
    private CategoryAdapter categoryAdapter;
    private ListView listView;


    public CategoryFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        categoryAdapter = new CategoryAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        listView = (ListView) rootView.findViewById(R.id.category_list_view);
        listView.setAdapter(categoryAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_categories_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences prefs = ArticleFragment.prefs;
            SharedPreferences.Editor editor = prefs.edit();
        Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());
        int count = listView.getAdapter().getCount();


        if (id == R.id.select_all_item) {

            for (int i = 0; i < count; i++) {
                CheckedTextView checkedTextView = (CheckedTextView) listView.getAdapter().getView(i, null, null).findViewById(R.id.category_list_item_textview);
                checkedTextView.setChecked(true);
                Cursor cursor = (Cursor) listView.getAdapter().getItem(i);
                favouriteCategories.add(cursor.getString(CategoryEntry.COLUMN__ID_INDEX));

            }



            editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES, favouriteCategories);
            editor.apply();
            getActivity().recreate();

            return true;
        }

        if (id == R.id.deselect_all_item) {
            for (int i = 0; i < count; i++) {
                CheckedTextView checkedTextView = (CheckedTextView) listView.getAdapter().getView(i, null, null).findViewById(R.id.category_list_item_textview);
                checkedTextView.setChecked(false);
                Cursor cursor = (Cursor) listView.getAdapter().getItem(i);
                String categoryId = cursor.getString(CategoryEntry.COLUMN__ID_INDEX);

                favouriteCategories.remove(categoryId);

            }


            editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES, favouriteCategories);
            editor.apply();

            getActivity().recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                CategoryEntry.CONTENT_URI,
                CategoryEntry.CATEGORY_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        categoryAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        categoryAdapter.swapCursor(null);
    }
}