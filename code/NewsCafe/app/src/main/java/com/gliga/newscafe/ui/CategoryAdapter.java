package com.gliga.newscafe.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;

import com.gliga.newscafe.R;

import java.util.HashSet;
import java.util.Set;

import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/7/2015.
 */
public class CategoryAdapter extends CursorAdapter {

    public CategoryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.category_list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        final String currentCategoryId = cursor.getString(CategoryEntry.COLUMN__ID_INDEX);

        CategoryViewHolder viewHolder = new CategoryViewHolder(view);

        CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.category_list_item_textview);

        SharedPreferences prefs = ArticleFragment.prefs;
        Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());

        checkedTextView.setChecked(false);
        for(String categoryId: favouriteCategories){

            if(currentCategoryId.equals(categoryId)){
                checkedTextView.setChecked(true);
            }
        }

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        CategoryViewHolder viewHolder = (CategoryViewHolder) view.getTag();

        final String categoryName = cursor.getString(CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME_INDEX);
        final String currentCategoryId = cursor.getString(CategoryEntry.COLUMN__ID_INDEX);

        viewHolder.nameView.setText(categoryName);

        final CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.category_list_item_textview);

        final SharedPreferences prefs = ArticleFragment.prefs;
        final Set<String> favouriteCategories = prefs.getStringSet(CategoryFragment.FAVOURITE_CATEGORIES, new HashSet<String>());

        final SharedPreferences.Editor editor = prefs.edit();



        checkedTextView.setChecked(false);
        for(String categoryId: favouriteCategories){

            if(currentCategoryId.equals(categoryId)){
                checkedTextView.setChecked(true);

            }
        }

        checkedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (checkedTextView.isChecked()) {
                    checkedTextView.setChecked(false);

                    favouriteCategories.remove(currentCategoryId);


                    Log.d(ArticleActivity.LOG_TAG, "Removed: " + categoryName +"--"+currentCategoryId);
                } else {
                    checkedTextView.setChecked(true);

                    favouriteCategories.add(currentCategoryId);


                    Log.d(ArticleActivity.LOG_TAG, "Added: " + categoryName + "--" + currentCategoryId);
                }


                editor.putStringSet(CategoryFragment.FAVOURITE_CATEGORIES,favouriteCategories);
                editor.apply();

            }
        });


    }




    /**
     * Cache of the children views for a forecast list item.
     */
    public static class CategoryViewHolder {
        public final CheckedTextView nameView;

        public CategoryViewHolder(View view) {
            nameView = (CheckedTextView) view.findViewById(R.id.category_list_item_textview);



        }
    }
}
