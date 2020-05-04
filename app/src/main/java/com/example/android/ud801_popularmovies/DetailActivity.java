package com.example.android.ud801_popularmovies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.ud801_popularmovies.utils.ApiKeyUtil;
import com.example.android.ud801_popularmovies.data.Movie;
import com.example.android.ud801_popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextView movieRuntimeTextView;

    private static final String MOVIE_EXTRAS = "details";

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_IMAGE_SIZE = "w185";
    private static final String TMDB_MOVIE_PATH = "movie";
    private static final String TMDB_API_KEY = "api_key";

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(MOVIE_EXTRAS)) {
                Movie movie = intent.getParcelableExtra(MOVIE_EXTRAS);

                TextView movieTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
                ImageView moviePosterImageView = (ImageView) findViewById(R.id.iv_thumbnail);
                TextView releaseYearTextView = (TextView) findViewById(R.id.tv_year_of_release);
                movieRuntimeTextView = (TextView) findViewById(R.id.tv_running_time);
                TextView movieRatingTextView = (TextView) findViewById(R.id.tv_movie_rating);
                TextView plotSynopsisTextView = (TextView) findViewById(R.id.tv_plot_synopsis);

                if (movie.getPosterPath() != null) {
                    String urlString = NetworkUtils.buildPosterPathUrl(TMDB_IMAGE_BASE_URL,
                            TMDB_IMAGE_SIZE, movie.getPosterPath());
                    Picasso.get().load(urlString).into(moviePosterImageView);
                }

                movieTitleTextView.setText(movie.getMovieTitle());
                releaseYearTextView.setText(year(movie.getReleaseDate()));
                String rating = String.format(Locale.US, "%.1f/10", movie.getMovieRating());
                movieRatingTextView.setText(rating);
                plotSynopsisTextView.setText(movie.getMoviePlotSynopsis());

                postMovieRuntime(movie.getId());
            }
        }
    }

    private void postMovieRuntime(int movieId) {
        String urlString = NetworkUtils.buildMovieDetailUrl(TMDB_BASE_URL, TMDB_MOVIE_PATH,
                movieId, TMDB_API_KEY, ApiKeyUtil.getApiKey(this));
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
                movieRuntimeTextView.setText(out);
            }
        }
    }
}
