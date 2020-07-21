package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.popularmovies.data.UserReview;

import java.util.List;

public class UserReviewsAdapter extends RecyclerView.Adapter<UserReviewsAdapter.UserReviewHolder> {

    private List<UserReview> mReviews;

    public UserReviewsAdapter(List<UserReview> reviews) {
        mReviews = reviews;
    }

    @NonNull
    @Override
    public UserReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.user_review_item, parent, false);
        UserReviewHolder holder = new UserReviewHolder(itemView);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewHolder holder, int position) {
        UserReview review = mReviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.size();
    }

    public void addAll(List<UserReview> reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    class UserReviewHolder extends RecyclerView.ViewHolder {

        private TextView authorTextView;
        private TextView contentTextView;

        public UserReviewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = (TextView) itemView.findViewById(R.id.author);
            contentTextView = (TextView) itemView.findViewById(R.id.content);
        }

        public void bind(UserReview review) {
            authorTextView.setText(review.getAuthor());
            contentTextView.setText(review.getContent());
        }
    }
}
