package com.example.android.ud801_popularmovies;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.ud801_popularmovies.data.Movie;
import com.example.android.ud801_popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviePosterAdapter extends
        RecyclerView.Adapter<MoviePosterAdapter.MoviePosterViewHolder> {

    private List<Movie> mMovies;
    private MovieAdapterClickListener mOnClickListener;

    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String TMDB_IMAGE_SIZE = "w185";

    public interface MovieAdapterClickListener {

        void onClick(int position);

    }

    public MoviePosterAdapter(List<Movie> movies) {
        mMovies = movies;
    }

    public void addAll(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void setOnClickListener(MovieAdapterClickListener listener) {
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public MoviePosterAdapter.MoviePosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        boolean shouldAttachImmediatelyToParent = false;
        View itemView = inflater.inflate(R.layout.movie_poster_item, parent,
                shouldAttachImmediatelyToParent);
        MoviePosterViewHolder viewHolder = new MoviePosterViewHolder(itemView, mOnClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MoviePosterAdapter.MoviePosterViewHolder
                                             holder, int position) {
        Movie movie = mMovies.get(position);
        if (movie.getPosterPath() != null) {
            holder.bind(movie);
        }
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    static class MoviePosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView moviePosterDisplay;
        private MovieAdapterClickListener listener;

        public MoviePosterViewHolder(@NonNull View itemView, MovieAdapterClickListener listener) {
            super(itemView);
            moviePosterDisplay = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                listener.onClick(position);
            }
        }

        public void bind(Movie movie) {
            if (movie.getPosterPath() != null) {
                String urlString = NetworkUtils.buildPosterPathUrl(TMDB_IMAGE_BASE_URL, TMDB_IMAGE_SIZE,
                        movie.getPosterPath());
                Picasso.get().load(urlString).into(moviePosterDisplay);
            }
        }



    }
}
