package com.example.android.popularmovies.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.popularmovies.data.Movie;
import static com.example.android.popularmovies.data.MoviesContract.MoviesEntry;

public class DatabaseUtils {

    public static final String[] sProjection = new String[] {
            MoviesEntry.COLUMN_MOVIE_ID,
            MoviesEntry.COLUMN_TITLE,
            MoviesEntry.COLUMN_POSTER_PATH,
            MoviesEntry.COLUMN_SYNOPSIS,
            MoviesEntry.COLUMN_USER_RATING,
            MoviesEntry.COLUMN_RELEASE_DATE
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_POSTER_PATH = 2;
    public static final int INDEX_SYNOPSIS = 3;
    public static final int INDEX_USER_RATING = 4;
    public static final int INDEX_RELEASE_DATE = 5;


    public static Cursor query(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MoviesEntry.CONTENT_URI,
                sProjection,
                null,
                null,
                null);
        return cursor;
    }

    public static Movie[] extractMovies(Cursor cursor) {
        Movie[] movies = new Movie[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < movies.length; i++) {
            int id = cursor.getInt(INDEX_MOVIE_ID);
            String title = cursor.getString(INDEX_TITLE);
            String posterPath = cursor.getString(INDEX_POSTER_PATH);
            String synopsis = cursor.getString(INDEX_SYNOPSIS);
            double rating = cursor.getDouble(INDEX_USER_RATING);
            String date = cursor.getString(INDEX_RELEASE_DATE);
            movies[i] = new Movie(id, posterPath, title, rating, date, synopsis);
            movies[i].setMarkedAsFavourite(1);
            cursor.moveToNext();
        }

        return movies;
    }

    public static void insert(Context context, Movie movie) {
        ContentValues values = new ContentValues();
        values.put(MoviesEntry.COLUMN_MOVIE_ID, movie.getId());
        values.put(MoviesEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MoviesEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MoviesEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        values.put(MoviesEntry.COLUMN_USER_RATING, movie.getRating());
        values.put(MoviesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());

        context.getContentResolver().insert(MoviesEntry.CONTENT_URI, values);
    }

    public static void delete(Context context, Movie movie) {
        int id = movie.getId();
        Uri uri = ContentUris.withAppendedId(MoviesEntry.CONTENT_URI, id);
        context.getContentResolver().delete(uri, null, null);
    }
}
