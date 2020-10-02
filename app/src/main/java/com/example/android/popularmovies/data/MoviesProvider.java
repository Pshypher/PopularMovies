package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.android.popularmovies.data.MoviesContract.MoviesEntry;

public class MoviesProvider extends ContentProvider {

    private MoviesDbHelper moviesDbHelper;

    private static final int MOVIES = 100;
    private static final int MOVIES_ITEM = 101;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MoviesContract.AUTHORITY,
                MoviesContract.PATH_FAVOURITES_MOVIES, MOVIES);
        matcher.addURI(MoviesContract.AUTHORITY,
                MoviesContract.PATH_FAVOURITES_MOVIES + "/#", MOVIES_ITEM);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        moviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = moviesDbHelper.getReadableDatabase();

        Cursor retCursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                retCursor = db.query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIES_ITEM:
                String id = uri.getLastPathSegment();
                selection = MoviesEntry.COLUMN_MOVIE_ID;
                selectionArgs = new String[] {id};
                retCursor = db.query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        String type;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                type = "vnd.android.cursor.dir/vnd." + MoviesContract.AUTHORITY
                        + "/" + MoviesEntry.TABLE_NAME;
                break;
            case MOVIES_ITEM:
                type = "vnd.android.cursor.item/vnd." + MoviesContract.AUTHORITY
                        + "/" + MoviesEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return type;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final SQLiteDatabase db = moviesDbHelper.getWritableDatabase();

        Uri retUri;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                long id = db.insert(
                        MoviesEntry.TABLE_NAME,
                        null,
                        values);
                if (id > 0) {
                    retUri = ContentUris.withAppendedId(uri, id);
                    getContext().getContentResolver().notifyChange(uri, null);
                } else
                    throw new SQLException("Failed to insert user favourite movie");

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = moviesDbHelper.getWritableDatabase();

        int rowsDeleted = 0;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES_ITEM:
                selection = MoviesEntry.COLUMN_MOVIE_ID + "=?";
                String id = uri.getLastPathSegment();
                selectionArgs = new String[] {id};
                rowsDeleted = db.delete(
                        MoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = moviesDbHelper.getWritableDatabase();

        int rowsAffected = 0;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES_ITEM:
                selection = MoviesEntry.COLUMN_MOVIE_ID + "=?";
                String id = uri.getLastPathSegment();
                selectionArgs = new String[] {id};
                rowsAffected = db.update(
                        MoviesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }
}
