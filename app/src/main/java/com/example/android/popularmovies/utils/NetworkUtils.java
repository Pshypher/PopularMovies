package com.example.android.popularmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.data.MovieTrailer;

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

    public static List<MovieTrailer> extractTrailers(String response) {
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        List<MovieTrailer> trailers = new ArrayList<MovieTrailer>();

        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject trailer = results.getJSONObject(i);
                String site = trailer.getString("site");
                if (site.equalsIgnoreCase("youtube")) {
                    String key = trailer.getString("key");
                    String name = trailer.getString("name");
                    trailers.add(new MovieTrailer(key, name));
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing response to JSON object.");
        }

        return trailers;
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

    public static String getApiKey(Context context) {
        return context.getResources().getString(R.string.api_key);
    }
}
