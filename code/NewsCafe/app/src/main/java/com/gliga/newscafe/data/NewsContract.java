/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gliga.newscafe.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database.
 */
public class NewsContract {

    public static final String CONTENT_AUTHORITY = "com.gliga.newscafe";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String SEARCH_URI = "search";
    public static final String PATH_ARTICLE = "articles";
    public static final String PATH_CATEGORY = "categories";

    //All the collumns from both the tables
    public static final String[] ALL_NEWS_COLUMNS = {

            ArticleEntry.TABLE_NAME + "." + ArticleEntry._ID,
            ArticleEntry.COLUMN_PUBLISH_DATE,
            ArticleEntry.COLUMN_SOURCE,
            ArticleEntry.COLUMN_SOURCE_URL,
            ArticleEntry.COLUMN_SUMMARY,
            ArticleEntry.COLUMN_TITLE,
            ArticleEntry.COLUMN_URL,

            CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID,
            CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME,
            CategoryEntry.COLUMN_DISPLAY_CATEGORY_NAME
    };

    public static final int ALL_COLUMNS_URL_INDEX = 6;
    public static final int ALL_COLUMNS_CATEGORY_NAME = 8;


    /**
     *   The information about the "Categories" table
     */
    public static final class CategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;


        public static final String TABLE_NAME = "category";
        public static final String COLUMN_DISPLAY_CATEGORY_NAME = "display_name";
        public static final String COLUMN_URL_CATEGORY_NAME = "url_name";


        public static final int COLUMN__ID_INDEX = 0;
        public static final int COLUMN_DISPLAY_CATEGORY_NAME_INDEX = 1;


        public static final String[] CATEGORY_COLUMNS = {

                TABLE_NAME + "." + _ID,
                COLUMN_DISPLAY_CATEGORY_NAME,
                COLUMN_DISPLAY_CATEGORY_NAME

        };

        /**
         * Construct the uri with an appended id for the category table
         * @param id
         * @return
         */
        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Get the uri for retrieving categories belonging to a certain category
         * @param categoryId
         * @return
         */
        public static Uri buildArticlesByCategoryUri(int categoryId) {
            return CONTENT_URI.buildUpon().appendPath(((Integer) categoryId).toString()).appendPath(PATH_ARTICLE).build();
        }

        /**
         *  Get the uri for retrieving categories belonging to a certain category but with search activated
         * @param categoryId
         * @param searchText
         * @return
         */
        public static Uri buildArticlesByCategoryWithSearchUri(int categoryId, String searchText) {
            return CONTENT_URI.buildUpon().appendPath(((Integer) categoryId).toString()).appendPath(PATH_ARTICLE).appendPath(SEARCH_URI).appendQueryParameter("q", searchText).build();
        }


    }

    /**
     * The information about the "Articles" table
     */
    public static final class ArticleEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTICLE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTICLE;


        public static final String TABLE_NAME = "article";
        public static final String COLUMN_PUBLISH_DATE = "publish_date";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_SOURCE_URL = "source_url";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_CATEGORY_ID = "category_id";

        public static final int COLUMN_PUBLISH_DATE_INDEX = 1;
        public static final int COLUMN_SOURCE_INDEX = 2;
        public static final int COLUMN_SOURCE_URL_INDEX = 3;
        public static final int COLUMN_SUMMARY_INDEX = 4;
        public static final int COLUMN_TITLE_INDEX = 5;
        public static final int COLUMN_URL_INDEX = 6;
        public static final int COLUMN_CATEGORY_ID_INDEX = 7;

        public static final String[] ARTICLE_COLUMNS = {

                TABLE_NAME + "." + _ID,
                COLUMN_PUBLISH_DATE,
                COLUMN_SOURCE,
                COLUMN_SOURCE_URL,
                COLUMN_SUMMARY,
                COLUMN_TITLE,
                COLUMN_URL,
                COLUMN_CATEGORY_ID,

        };

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}