package com.example.android.ud801_popularmovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.ud801_popularmovies.utils.ApiKeyUtil;
import com.example.android.ud801_popularmovies.data.Movie;
import com.example.android.ud801_popularmovies.utils.NetworkUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        MoviePosterAdapter.MovieAdapterClickListener {

    private ArrayList<Movie> mMovieList;
    private MoviePosterAdapter mAdapter;
    private RecyclerView recyclerView;
    private TextView errorMessageDisplay;
    private TextView networkStatusDisplay;
    private ProgressBar loadingIndicator;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        errorMessageDisplay = (TextView) findViewById(R.id.tv_error_message);
        networkStatusDisplay = (TextView) findViewById(R.id.tv_network_status);
        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
                GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MoviePosterAdapter(new ArrayList<Movie>());
        mAdapter.setOnClickListener(this);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setHasFixedSize(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_EXTRAS)) {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_EXTRAS);
            mAdapter.addAll(mMovieList);
        } else {
            getMoviePosters(TMDB_SORT_BY_POPULAR_MOVIES);
        }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedMenuItemId = item.getItemId();
        switch (selectedMenuItemId) {
            case R.id.action_popular_movies:
                getMoviePosters(TMDB_SORT_BY_POPULAR_MOVIES);
                return true;
            case R.id.action_ratings:
                getMoviePosters(TMDB_SORT_BY_RATINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getMoviePosters(String orderBy) {
        URL url = buildUrl(orderBy, "en-US");
        new FetchMovieAsyncTask().execute(url);
    }

    private boolean isConnected() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private URL buildUrl(String orderBy, String lang) {
        Uri baseUri = Uri.parse(TMDB_BASE_URL);
        String apiKey = ApiKeyUtil.getApiKey(this);
        Uri.Builder builder = baseUri.buildUpon()
                .appendPath(TMDB_DISCOVERY_PATH)
                .appendPath(TMDB_MOVIE_PATH)
                .appendQueryParameter(TMDB_API_KEY, apiKey)
                .appendQueryParameter(TMDB_SORT_PARAM, orderBy)
                .appendQueryParameter(TMDB_LANGUAGE, lang)
                .appendQueryParameter(TMDB_INCLUDE_ADULT, TMDB_FLAG);

        URL queryUrl = null;
        String urlString = builder.toString();
        try {
            queryUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "URL format " + urlString + " was not properly specified.");
        }

        return queryUrl;
    }

    @Override
    public void onClick(int position) {
        Movie movie = mMovieList.get(position);

        Context context = MainActivity.this;

        Class detailsActivity = DetailActivity.class;

        Intent intent = new Intent(context, detailsActivity);
        intent.putExtra(MOVIE_EXTRAS, movie);
        startActivity(intent);
    }

    class FetchMovieAsyncTask extends AsyncTask<URL, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recyclerView.setVisibility(View.INVISIBLE);
            loadingIndicator.setVisibility(View.VISIBLE);

            if (!isConnected()) {
//                loadingIndicator.setVisibility(View.INVISIBLE);
                showNoNetworkStatus();
                cancel(true);
            }
        }

        @Override
        protected Movie[] doInBackground(URL... params) {
            URL url = params[0];

            try {
                String response = NetworkUtils.makeHttpRequest(url);
                Movie[] movies = NetworkUtils.extractMovies(response);
                return movies;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            super.onPostExecute(movies);

            loadingIndicator.setVisibility(View.INVISIBLE);
            if (movies != null && movies.length > 0) {
                mMovieList = new ArrayList<Movie>(Arrays.asList(movies));
                mAdapter.addAll(mMovieList);
                showMoviePosters();
            } else {
                showErrorMessage();
            }
        }
    }

    private void showErrorMessage() {
        errorMessageDisplay.setVisibility(View.VISIBLE);
        networkStatusDisplay.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void showMoviePosters() {
        recyclerView.setVisibility(View.VISIBLE);
        errorMessageDisplay.setVisibility(View.INVISIBLE);
        networkStatusDisplay.setVisibility(View.INVISIBLE);
    }

    private void showNoNetworkStatus() {
        networkStatusDisplay.setVisibility(View.VISIBLE);
        errorMessageDisplay.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }
}
