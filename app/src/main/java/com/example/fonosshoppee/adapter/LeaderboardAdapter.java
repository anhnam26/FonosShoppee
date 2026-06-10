package com.example.fonosshoppee.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.BookDetailActivity;
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.model.BookItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final List<BookItem> items;

    public LeaderboardAdapter(List<BookItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookItem item = items.get(position);

        holder.tvRank.setText(String.valueOf(position + 1));
        if (position == 0) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700"));
        } else if (position == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0"));
        } else if (position == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32"));
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#6D7885"));
        }

        holder.tvTitle.setText(item.getTitle());
        holder.tvAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "Fonos Shoppee");

        if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(item.getCoverUrl()).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(android.R.color.darker_gray);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), BookDetailActivity.class);
            intent.putExtra("BOOK_TITLE", item.getTitle());
            intent.putExtra("BOOK_AUTHOR", item.getAuthor());
            intent.putExtra("BOOK_COVER", item.getCoverUrl());
            intent.putExtra("AUDIO_URL", item.getAudioUrl());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTitle, tvAuthor;
        ImageView ivCover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ivCover = itemView.findViewById(R.id.ivCover);
        }
    }
}
