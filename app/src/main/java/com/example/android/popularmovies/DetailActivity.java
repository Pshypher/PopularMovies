package com.example.android.popularmovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.Error;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.data.UserReview;
import com.example.android.popularmovies.utils.DatabaseUtils;
import com.example.android.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends AppCompatActivity
        implements ExtrasAdapter.ListItemClickListener {

    private TextView mMovieTitleTextView;
    private TextView mMovieRuntimeTextView;
    private ImageView mMoviePosterImageView;
    private TextView mReleaseYearTextView;
    private TextView mMovieRatingTextView;
    private TextView mPlotSynopsisTextView;

    private ExtrasAdapter mExtrasAdapter;
    private TextView mErrorTextView;
    private ProgressBar mLoadingIndicator;
    private RecyclerView mRecyclerView;

    private ArrayList<Object> extraDetails;

    private static final String MOVIE_EXTRAS = "details";

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_IMAGE_SIZE = "w185";
    private static final String TMDB_API_KEY = "api_key";
    private static final String TMDB_PATH_MOVIE = "movie";
    private static final String TMDB_PATH_VIDEOS = "videos";
    private static final String TMDB_PATH_REVIEWS = "reviews";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/";
    private static final String YOUTUBE_VIEW_PATH = "watch";
    private static final String YOUTUBE_VIEW_KEY = "v";

    private static final String CLICKED = "clicked?";

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(MOVIE_EXTRAS)) {
                Movie movie = intent.getParcelableExtra(MOVIE_EXTRAS);
                initViews(movie);
                loadImage(movie);
                bind(movie);
                int id = movie.getId();
                fetchMovieRuntime(id);
                fetchMovieTrailers(id);
                fetchUserReviews(id);
            }
        }

        extraDetails = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void bind(Movie movie) {
        mMovieTitleTextView.setText(movie.getTitle());
        mReleaseYearTextView.setText(year(movie.getReleaseDate()));
        String rating = String.format(Locale.US, "%.1f/10", movie.getRating());
        mMovieRatingTextView.setText(rating);
        mPlotSynopsisTextView.setText(movie.getSynopsis());
    }

    private void loadImage(Movie movie) {
        if (movie.getPosterPath() != null) {
            String[] paths = new String[] { TMDB_IMAGE_SIZE };
            String urlString = NetworkUtils.buildUrl(TMDB_IMAGE_BASE_URL, paths,
                    movie.getPosterPath(), null);
            if (movie.isMarkedAsFavourite()) {
                Picasso.get().load(urlString)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mMoviePosterImageView);
                return;
            }

            Picasso.get().load(urlString)
                    .into(mMoviePosterImageView);
        }
    }

    private void initViews(Movie movie) {
        setupDetailViews();
        initExtrasViews();

        Button btn = (Button) findViewById(R.id.btn_favourite);
        if (movie.isMarkedAsFavourite()) {
            btn.setText(getString(R.string.btn_remove));
        }
        btn.setOnClickListener(new View.OnClickListener() {

            boolean clicked = true;

            @Override
            public void onClick(View v) {
                if (movie.isMarkedAsFavourite()) {
                    (new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            DatabaseUtils.delete(DetailActivity.this, movie);
                            return null;
                        }
                    }).execute();
                } else {
                    (new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            DatabaseUtils.insert(DetailActivity.this, movie);
                            return null;
                        }
                    }).execute();
                }

                Intent intent = new Intent();
                intent.putExtra(CLICKED, clicked);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initExtrasViews() {
        mErrorTextView = (TextView) findViewById(R.id.error_message_text);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.result_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mExtrasAdapter = new ExtrasAdapter(null, this);
        mRecyclerView.setAdapter(mExtrasAdapter);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setClipToPadding(false);
    }

    private void setupDetailViews() {
        mMovieTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
        mMoviePosterImageView = (ImageView) findViewById(R.id.iv_thumbnail);
        mReleaseYearTextView = (TextView) findViewById(R.id.tv_year_of_release);
        mMovieRuntimeTextView = (TextView) findViewById(R.id.tv_running_time);
        mMovieRatingTextView = (TextView) findViewById(R.id.tv_movie_rating);
        mPlotSynopsisTextView = (TextView) findViewById(R.id.tv_plot_synopsis);
    }

    private void fetchMovieRuntime(int id) {

        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put(TMDB_API_KEY, NetworkUtils.getApiKey(this));
        String[] paths = new String[] { TMDB_PATH_MOVIE, Integer.toString(id) };
        String urlString = NetworkUtils.buildUrl(TMDB_BASE_URL, paths, null, queryMap);

        try {
            URL url = new URL(urlString);
            new FetchMovieRuntimeTask().execute(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Illegal URL string format " + urlString + " specified.");
        }
    }

    private String year(String releaseDate) {
        final String SEPARATOR = "-";
        String[] dateFields = releaseDate.split(SEPARATOR);
        return dateFields[0];
    }

    @Override
    public void onListItemClick(String value) {
        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put(YOUTUBE_VIEW_KEY, value);
        String urlString = NetworkUtils.buildUrl(YOUTUBE_BASE_URL, new String[]{YOUTUBE_VIEW_PATH},
                null, queryMap);
        Uri file = Uri.parse(urlString);
        playMedia(file);
    }

    private void playMedia(Uri file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(file);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    class FetchMovieRuntimeTask extends AsyncTask<URL, Void, Integer> {

        @Override
        protected Integer doInBackground(URL... urls) {
            URL url = urls[0];
            try {
                String response = NetworkUtils.makeHttpRequest(url);
                Integer runtime = NetworkUtils.fetchRuntime(response);
                return runtime;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer runtime) {
            if (runtime != null) {
                String out = String.format(Locale.US, "%dmin", runtime);
                mMovieRuntimeTextView.setText(out);
            }
        }
    }

    private void fetchMovieTrailers(int id) {
        String[] paths = new String[] { TMDB_PATH_MOVIE, Integer.toString(id), TMDB_PATH_VIDEOS };
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(TMDB_API_KEY, NetworkUtils.getApiKey(DetailActivity.this));
        String urlString = NetworkUtils.buildUrl(TMDB_BASE_URL, paths, null, queryMap);

        try {
            URL url = new URL(urlString);
            new FetchTrailersAsyncTack().execute(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem parsing url string " + urlString);
        }
    }

    class FetchTrailersAsyncTack extends AsyncTask<URL, Void, List<MovieTrailer>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isConnected()) {
                Context context = DetailActivity.this;
                mErrorTextView.setText(context.getString(R.string.error_network));
                publishError();
            }
        }

        @Override
        protected List<MovieTrailer> doInBackground(URL... params) {

            URL url = params[0];

            List<MovieTrailer> trailers = null;
            try {
                String response = NetworkUtils.makeHttpRequest(url);
                trailers = NetworkUtils.extractTrailers(response);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with http request.");
                e.printStackTrace();
            }
            return trailers;
        }

        @Override
        protected void onPostExecute(List<MovieTrailer> trailers) {
            super.onPostExecute(trailers);

            extraDetails.add(getString(R.string.label_trailers));

            if (trailers != null && trailers.size() > 0)
                extraDetails.addAll(trailers);
            else
                extraDetails.add(new Error(getString(R.string.error_msg_trailer)));

            mExtrasAdapter.addAll(extraDetails);
            publishResult();
        }
    }

    private void fetchUserReviews(int id) {
        String[] paths = new String[] { TMDB_PATH_MOVIE, Integer.toString(id), TMDB_PATH_REVIEWS };
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(TMDB_API_KEY, NetworkUtils.getApiKey(this));
        String urlString = NetworkUtils.buildUrl(TMDB_BASE_URL, paths, null, queryMap);

        try {
            URL url = new URL(urlString);
            new FetchUserReviewsAsyncTask().execute(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem parsing url string " + urlString);
        }
    }

    class FetchUserReviewsAsyncTask extends AsyncTask<URL, Void, List<UserReview>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isConnected()) {
                Context context = DetailActivity.this;
                mErrorTextView.setText(context.getString(R.string.error_network));
                publishError();
            }
        }

        @Override
        protected List<UserReview> doInBackground(URL... params) {
            URL url = params[0];

            List<UserReview> reviews = null;
            try {
                String response = NetworkUtils.makeHttpRequest(url);
                reviews = NetworkUtils.extractReviews(response);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with http request.");
                e.printStackTrace();
            }
            return reviews;
        }

        @Override
        protected void onPostExecute(List<UserReview> reviews) {
            super.onPostExecute(reviews);

            extraDetails.add(getString(R.string.label_reviews));

            if (reviews != null && reviews.size() > 0) extraDetails.addAll(reviews);
            else extraDetails.add(new Error(getString(R.string.error_msg_reviews)));

            mExtrasAdapter.addAll(extraDetails);
            publishResult();
        }
    }

    private boolean isConnected() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void publishResult() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void publishError() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }
}
