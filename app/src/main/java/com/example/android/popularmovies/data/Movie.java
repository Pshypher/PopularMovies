package com.example.android.popularmovies.data;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class Movie implements Parcelable {

    private int id;
    private String mPosterPath;
    private String mMovieTitle;
    private double mMovieRating;
    private String mReleaseDate;
    private String mMoviePlotSynopsis;

    private int markedAsFavourite;

    public Movie(int id, String posterPath, String name, double ratings, String releaseDate,
                 String overview) {
        this.id = id;
        mPosterPath = posterPath;
        mMovieTitle = name;
        mMovieRating = ratings;
        mReleaseDate = releaseDate;
        mMoviePlotSynopsis = overview;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        mPosterPath = in.readString();
        mMovieTitle = in.readString();
        mMovieRating = in.readDouble();
        mReleaseDate = in.readString();
        mMoviePlotSynopsis = in.readString();
        markedAsFavourite = in.readInt();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getTitle() {
        return mMovieTitle;
    }

    public void setMovieTitle(String name) {
        mMovieTitle = name;
    }

    public double getRating() {
        return mMovieRating;
    }

    public void setMovieRating(double rating) {
        mMovieRating = rating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getSynopsis() {
        return mMoviePlotSynopsis;
    }

    public void setMoviePlotSynopsis(String overview) {
        mMoviePlotSynopsis = overview;
    }

    public boolean isMarkedAsFavourite() {
        return markedAsFavourite > 0;
    }

    public void setMarkedAsFavourite(int value) {
        markedAsFavourite = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(mPosterPath);
        dest.writeString(mMovieTitle);
        dest.writeDouble(mMovieRating);
        dest.writeString(mReleaseDate);
        dest.writeString(mMoviePlotSynopsis);
        dest.writeInt(markedAsFavourite);
    }
}
