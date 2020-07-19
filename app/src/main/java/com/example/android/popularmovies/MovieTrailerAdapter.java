package com.example.android.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.popularmovies.data.MovieTrailer;

import java.util.List;

public class MovieTrailerAdapter extends RecyclerView.Adapter<MovieTrailerAdapter.VideoInfoHolder> {

    private List<MovieTrailer> mTrailers;
    private ListItemClickListener mOnListItemClickListener;

    public interface ListItemClickListener {
        void onListItemClick(String key);
    }

    public MovieTrailerAdapter(List<MovieTrailer> trailers, ListItemClickListener listener) {
        mTrailers = trailers;
        mOnListItemClickListener = listener;
    }

    @NonNull
    @Override
    public VideoInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.movie_trailer_item, parent, false);
        VideoInfoHolder holder = new VideoInfoHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoInfoHolder holder, int position) {
        MovieTrailer trailer = mTrailers.get(position);
        holder.bind(trailer);
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }


    class VideoInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;

        public VideoInfoHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.trailer_title);
            itemView.setOnClickListener(this);
        }

        public void bind(MovieTrailer video) {
            title.setText(video.getName());
        }

        @Override
        public void onClick(View v) {
            MovieTrailer trailer = mTrailers.get(getAdapterPosition());
            mOnListItemClickListener.onListItemClick(trailer.getKey());
        }
    }
}
