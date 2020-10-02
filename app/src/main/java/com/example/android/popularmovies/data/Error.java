package com.example.android.popularmovies.data;

public class Error {

    private String mMessage;

    public Error(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
