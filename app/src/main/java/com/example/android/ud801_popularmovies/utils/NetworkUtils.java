package com.example.android.ud801_popularmovies.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.ud801_popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {



    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    public static String buildPosterPathUrl(String baseUrl, String imageSize,
                                            String moviePosterPath) {
        Uri baseUri = Uri.parse(baseUrl);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendPath(imageSize);
        builder.appendEncodedPath(moviePosterPath);

        Uri uri = builder.build();
        return uri.toString();
    }

    public static String buildMovieDetailUrl(String baseUrl, String path, int id,
                                             String paramKey, String paramValue) {
        Uri baseUri = Uri.parse(baseUrl);
        Uri.Builder builder = baseUri.buildUpon()
                .appendPath(path)
                .appendPath(Integer.toString(id))
                .appendQueryParameter(paramKey, paramValue);
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
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        Movie[] movies = null;
        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray("results");

            movies = new Movie[results.length()];
            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                int id = movie.getInt("id");
                String posterPath = movie.getString("poster_path");
                String title = movie.getString("title");
                double ratings = movie.getDouble("vote_average");
                String releaseDate = movie.getString("release_date");
                String overview = movie.getString("overview");

                movies[i] = new Movie(id, posterPath, title, ratings, releaseDate, overview);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
        }

        return movies;
    }

    public static Integer fetchRuntime(String response) {
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        try {
            JSONObject movie = new JSONObject(response);
            Integer runtime = movie.getInt("runtime");
            return runtime;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
            return null;
        }
    }
}
