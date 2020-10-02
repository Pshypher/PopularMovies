package com.example.android.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.data.UserReview;
import com.example.android.popularmovies.data.Error;

import java.util.List;
import java.util.Locale;

public class ExtrasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> mExtras;
    private ListItemClickListener mOnListItemClickListener;

    private final int TRAILERS = 0;
    private final int REVIEWS = 1;
    private final int TITLE = 2;
    private final int ERROR = 3;

    public void addAll(List<Object> extras) {
        mExtras = extras;
        notifyDataSetChanged();
    }

    public interface ListItemClickListener {
        void onListItemClick(String key);
    }

    public ExtrasAdapter(List<Object> extras, ListItemClickListener listener) {
        mExtras = extras;
        mOnListItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder;

        switch (viewType) {
            case TRAILERS:
                View itemView = inflater.inflate(R.layout.movie_trailer_item, parent, false);
                holder = new VideoInfoHolder(itemView);
                break;
            case REVIEWS:
                itemView = inflater.inflate(R.layout.user_review_item, parent, false);
                holder = new UserReviewHolder(itemView);
                break;
            case TITLE:
                itemView = inflater.inflate(R.layout.title_layout, parent, false);
                holder = new TextHolder(itemView);
                break;
            case ERROR:
                itemView = inflater.inflate(R.layout.error_layout, parent, false);
                holder = new ErrorMessageHolder(itemView);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format(Locale.getDefault(),
                                "No ViewHolder of the corresponding %d found", viewType));
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TRAILERS:
                ((VideoInfoHolder) holder).bind(mExtras.get(position));
                break;
            case REVIEWS:
                ((UserReviewHolder) holder).bind(mExtras.get(position));
                break;
            case TITLE:
                ((TextHolder) holder).bind(mExtras.get(position));
                break;
            case ERROR:
                ((ErrorMessageHolder) holder).bind(mExtras.get(position));
                break;
            default:
                throw new IllegalArgumentException("No such view type " + holder.getItemViewType());
        }
    }

    @Override
    public int getItemCount() {
        if (mExtras == null) return 0;
        return mExtras.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (mExtras.get(position) instanceof MovieTrailer) return TRAILERS;
        else if (mExtras.get(position) instanceof UserReview) return REVIEWS;
        else if (mExtras.get(position) instanceof String) return TITLE;
        else if (mExtras.get(position) instanceof Error) return ERROR;

        return super.getItemViewType(position);
    }

    class VideoInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;

        public VideoInfoHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.trailer_title);
            itemView.setOnClickListener(this);
        }

        public void bind(Object obj) {
            title.setText(((MovieTrailer) obj).getName());
        }

        @Override
        public void onClick(View v) {
            Object obj = mExtras.get(getAdapterPosition());
            if (obj instanceof MovieTrailer) {
                mOnListItemClickListener.onListItemClick(((MovieTrailer) obj).getKey());
            }
        }
    }

    class UserReviewHolder extends RecyclerView.ViewHolder {

        private TextView authorTextView;
        private TextView contentTextView;

        public UserReviewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = (TextView) itemView.findViewById(R.id.author);
            contentTextView = (TextView) itemView.findViewById(R.id.content);
        }

        public void bind(Object obj) {
            authorTextView.setText(((UserReview) obj).getAuthor());
            contentTextView.setText(((UserReview) obj).getContent());
        }
    }

    class TextHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;

        public TextHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.text);
        }

        public void bind(Object obj) {
            titleTextView.setText((String) obj);
        }
    }

    class ErrorMessageHolder extends RecyclerView.ViewHolder {

        private TextView errorTextView;

        public ErrorMessageHolder(@NonNull View itemView) {
            super(itemView);
            errorTextView = (TextView) itemView.findViewById(R.id.error_message_text);
        }

        public void bind(Object obj) {
            errorTextView.setText(((Error) obj).getMessage());
        }
    }
}
