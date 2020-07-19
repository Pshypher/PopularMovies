package com.example.android.popularmovies.data;

public class MovieTrailer {

    private String key;
    private String name;

    public MovieTrailer(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
