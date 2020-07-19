package com.example.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVOURITES_MOVIES = "favourites";


    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MoviesContract() {}

    /* Inner class that defines the table contents */
    public static class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITES_MOVIES).build();

        public static final String TABLE_NAME = "favourites";
        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "path";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_USER_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";

    }
}
