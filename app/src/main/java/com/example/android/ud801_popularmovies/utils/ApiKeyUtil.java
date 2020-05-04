package com.example.android.ud801_popularmovies.utils;

import android.content.Context;

import com.example.android.ud801_popularmovies.R;

public class ApiKeyUtil {

    public static String getApiKey(Context context) {
        return context.getResources().getString(R.string.api_key);
    }
}
