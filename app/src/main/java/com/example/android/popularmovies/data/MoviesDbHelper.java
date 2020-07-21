package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.popularmovies.data.MoviesContract.MoviesEntry;

import androidx.annotation.Nullable;

public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_FAVOURITE_MOVIES =
            "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                    MoviesEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, " +
                    MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                    MoviesEntry.COLUMN_POSTER_PATH + " TEXT, " +
                    MoviesEntry.COLUMN_SYNOPSIS + " TEXT, " +
                    MoviesEntry.COLUMN_USER_RATING + " REAL, " +
                    MoviesEntry.COLUMN_RELEASE_DATE + " INTEGER)";

    private static final String SQL_DELETE_FAVOURITE_MOVIES =
            "DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FavouriteMovies.db";

    public MoviesDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FAVOURITE_MOVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over
        db.execSQL(SQL_DELETE_FAVOURITE_MOVIES);
        onCreate(db);
    }
}
