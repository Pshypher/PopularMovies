package com.example.android.popularmovies;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.utils.DatabaseUtils;
import com.example.android.popularmovies.utils.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        MoviePosterAdapter.MovieAdapterClickListener, LoaderManager.LoaderCallbacks<Movie[]> {

    private ArrayList<Movie> mMovieList;
    private MoviePosterAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    private TextView mNetworkStatusDisplay;
    private ProgressBar mLoadingIndicator;

    private boolean wasClicked;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_DISCOVERY_PATH = "discover";
    private static final String TMDB_MOVIE_PATH = "movie";
    private static final String TMDB_API_KEY = "api_key";
    private static final String TMDB_SORT_PARAM = "sort_by";
    private static final String TMDB_SORT_BY_POPULAR_MOVIES = "popularity.desc";
    private static final String TMDB_SORT_BY_RATINGS = "vote_average.desc";
    private static final String TMDB_LANGUAGE = "language";
    private static final String TMDB_INCLUDE_ADULT = "include_adult";
    private static final String TMDB_FLAG = "false";

    private static final String MOVIE_EXTRAS = "details";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int POPULAR_LOADER_ID = 100;
    private static final int TOP_RATED_LOADER_ID = 200;
    private static final int FAVOURITE_LOADER_ID = 205;

    private static final String RESOURCE_ID = "location";
        private static final String CLICKED = "clicked?";

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        wasClicked = intent.getBooleanExtra(CLICKED, false);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        populateRecyclerView(savedInstanceState);
    }

    private void populateRecyclerView(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_EXTRAS)) {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_EXTRAS);
            mAdapter.addAll(mMovieList);
        } else {
            Bundle args = new Bundle();
            args.putString(RESOURCE_ID, TMDB_SORT_BY_POPULAR_MOVIES);
            getSupportLoaderManager().initLoader(POPULAR_LOADER_ID, args, this);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
                GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new MoviePosterAdapter(new ArrayList<Movie>());
        mAdapter.setOnClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setHasFixedSize(true);
    }

    private void initViews() {
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message);
        mNetworkStatusDisplay = (TextView) findViewById(R.id.tv_network_status);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(MOVIE_EXTRAS, mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasClicked) {
            getSupportLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, this);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Bundle args = new Bundle();

        int selectedMenuItemId = item.getItemId();

        switch (selectedMenuItemId) {
            case R.id.action_popular_movies:
                args.putString(RESOURCE_ID, TMDB_SORT_BY_POPULAR_MOVIES);
                getSupportLoaderManager().initLoader(POPULAR_LOADER_ID, args, this);
                return true;
            case R.id.action_ratings:
                args.putString(RESOURCE_ID, TMDB_SORT_BY_RATINGS);
                getSupportLoaderManager().initLoader(TOP_RATED_LOADER_ID, args, this);
                return true;
            case R.id.action_favourite_movies:
                getSupportLoaderManager().initLoader(FAVOURITE_LOADER_ID, null, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isConnected() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onListItemClick(int position) {
        Movie movie = mMovieList.get(position);
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(MOVIE_EXTRAS, movie);
        mStartForResult.launch(intent);
    }

    @NonNull
    @Override
    public Loader<Movie[]> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<Movie[]>(MainActivity.this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                mRecyclerView.setVisibility(View.INVISIBLE);
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if (!isConnected()) {
                    showNoNetworkStatus();
                    return;
                }
                forceLoad();
            }

            @Nullable
            @Override
            public Movie[] loadInBackground() {

                Movie[] movies = null;

                if (args != null) {
                    String[] paths = new String[] { TMDB_DISCOVERY_PATH, TMDB_MOVIE_PATH };
                    Map<String, String> queryMap = new HashMap<>();
                    queryMap.put(TMDB_API_KEY, NetworkUtils.getApiKey(MainActivity.this));
                    queryMap.put(TMDB_SORT_PARAM, args.getString(RESOURCE_ID));
                    queryMap.put(TMDB_LANGUAGE, "en-US");
                    queryMap.put(TMDB_INCLUDE_ADULT, TMDB_FLAG);
                    String urlString = NetworkUtils.buildUrl(TMDB_BASE_URL, paths, null, queryMap);

                    URL url = null;
                    try {
                        url = new URL(urlString);
                        String response = NetworkUtils.makeHttpRequest(url);
                        movies = NetworkUtils.extractMovies(response);
                    } catch (MalformedURLException e) {
                        Log.e(LOG_TAG, "URL format " + urlString + " was not properly specified.");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Cursor cursor = DatabaseUtils.query(MainActivity.this);
                    movies = DatabaseUtils.extractMovies(cursor);
                }

                return movies;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Movie[]> loader, Movie[] movies) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (movies != null && movies.length > 0) {
            mMovieList = new ArrayList<Movie>(Arrays.asList(movies));
            mAdapter.addAll(mMovieList);
            showMoviePosters();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Movie[]> loader) {
        mAdapter.addAll(null);
    }

    private void showErrorMessage() {
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mNetworkStatusDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showMoviePosters() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mNetworkStatusDisplay.setVisibility(View.INVISIBLE);
    }

    private void showNoNetworkStatus() {
        mNetworkStatusDisplay.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }
}
