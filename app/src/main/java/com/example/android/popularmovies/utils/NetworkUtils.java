package com.example.android.popularmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.data.UserReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    public static final String RESULTS = "results";
    public static final String SITE = "site";
    public static final String ID = "id";

    public static String buildUrl(String baseUrl, String[] paths, String encodedPath, Map<String, String> queryMap) {

        Uri baseUri = Uri.parse(baseUrl);
        Uri.Builder builder = baseUri.buildUpon();

        if (paths != null) {
            for (String path : paths) {
                builder.appendPath(path);
            }
        }

        if (!TextUtils.isEmpty(encodedPath)) builder.appendEncodedPath(encodedPath);

        if (queryMap != null) {
            Set<String> keys = queryMap.keySet();
            for (String key: keys) {
                builder.appendQueryParameter(key, (String) queryMap.get(key));
            }
        }

        Uri uri = builder.build();
        return uri.toString();
    }

    public static String makeHttpRequest(URL url) throws IOException {

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                result = readStream(inputStream);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem with making HTTP request.");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    private static String readStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        Scanner scanner = new Scanner(inputStream);
        String response = null;
        if (scanner.hasNext()) {
            scanner.useDelimiter("\\A");
            response = scanner.next();
        }

        return response;
    }

    public static Movie[] extractMovies(String response) {

        final String POSTER_PATH = "poster_path";
        final String TITLE = "title";
        final String VOTE_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";
        final String OVERVIEW = "overview";

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        Movie[] movies = null;
        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray(RESULTS);

            movies = new Movie[results.length()];
            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                int id = movie.getInt(ID);
                String posterPath = movie.getString(POSTER_PATH);
                String title = movie.getString(TITLE);
                double ratings = movie.getDouble(VOTE_AVERAGE);
                String releaseDate = movie.getString(RELEASE_DATE);
                String overview = movie.getString(OVERVIEW);

                movies[i] = new Movie(id, posterPath, title, ratings, releaseDate, overview);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
        }

        return movies;
    }

    public static List<MovieTrailer> extractTrailers(String response) {

        final String VALUE_SITE = "youtube";
        final String KEY = "key";
        final String NAME = "name";

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        List<MovieTrailer> trailers = new ArrayList<MovieTrailer>();

        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray(RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject trailer = results.getJSONObject(i);
                String site = trailer.getString(SITE);

                if (site.equalsIgnoreCase(VALUE_SITE)) {
                    String key = trailer.getString(KEY);
                    String name = trailer.getString(NAME);
                    trailers.add(new MovieTrailer(key, name));
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
        }

        return trailers;
    }

    public static List<UserReview> extractReviews(String response) {

        final String AUTHOR = "author";
        final String CONTENT = "content";

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        List<UserReview> reviews = null;
        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray(RESULTS);
            reviews = new ArrayList<UserReview>();
            for (int i = 0; i < results.length(); i++) {
                JSONObject res = results.getJSONObject(i);
                String author = res.getString(AUTHOR);
                String content = res.getString(CONTENT);
                reviews.add(new UserReview(author, content));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
        }

        return reviews;
    }

    public static Integer fetchRuntime(String response) {

        final String RUNTIME = "runtime";

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        try {
            JSONObject movie = new JSONObject(response);
            Integer runtime = movie.getInt(RUNTIME);
            return runtime;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
            return null;
        }
    }

    public static String getApiKey(Context context) {
        return context.getResources().getString(R.string.api_key);
    }
}
