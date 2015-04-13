package com.gliga.newscafe.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.gliga.newscafe.R;
import com.gliga.newscafe.data.NewsContract;

import static com.gliga.newscafe.data.NewsContract.ArticleEntry;
import static com.gliga.newscafe.data.NewsContract.CategoryEntry;

/**
 * Created by gliga on 3/7/2015.
 * Adapter for the articles
 */
public class ArticleAdapter extends CursorAdapter {

    public ArticleAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int  layoutId = R.layout.article_list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ArticleViewHolder viewHolder = new ArticleViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ArticleViewHolder viewHolder = (ArticleViewHolder) view.getTag();


        String articleCategoryName = cursor.getString(NewsContract.ALL_COLUMNS_CATEGORY_NAME);

        String articleTitleName = cursor.getString(ArticleEntry.COLUMN_TITLE_INDEX);

        int firstPar = articleTitleName.indexOf('(');
        int secondPar = articleTitleName.indexOf(')');
        String toBeRemoved = articleTitleName.substring(firstPar,secondPar+1);

        String normalizedTitle = articleTitleName.replace(toBeRemoved, "");

        String articleSource = cursor.getString(ArticleEntry.COLUMN_SOURCE_INDEX);

        viewHolder.categoryView.setText(articleCategoryName);
        viewHolder.titleView.setText(normalizedTitle);
        viewHolder.sourceView.setText(articleSource);

    }


    /**
     * View holder for the articles
     */
    public static class ArticleViewHolder {
        public final TextView categoryView;
        public final TextView titleView;
        public final TextView sourceView;

        public ArticleViewHolder(View view) {
            categoryView = (TextView) view.findViewById(R.id.article_category_text_view);
            titleView = (TextView) view.findViewById(R.id.article_title_text_view);
            sourceView = (TextView) view.findViewById(R.id.article_source_text_view);
        }
    }
}
