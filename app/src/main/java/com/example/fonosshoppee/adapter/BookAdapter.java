package com.example.fonosshoppee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Import thư viện Glide và Model
import com.bumptech.glide.Glide;
import com.example.fonosshoppee.model.BookItem;
import com.example.fonosshoppee.R;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<BookItem> bookList;
    private boolean isDarkMode = false;

    public void setDarkMode(boolean isDark) {
        this.isDarkMode = isDark;
    }
    public BookAdapter(List<BookItem> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookItem item = bookList.get(position);

        // Gắn dữ liệu Tiêu đề và Tác giả
        holder.tvTitle.setText(item.getTitle());

        if (holder.tvAuthor != null && item.getAuthor() != null) {
            holder.tvAuthor.setText(item.getAuthor());
        }

        // Dùng Glide để load ảnh bìa sách
        if (holder.ivCover != null && item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getCoverUrl())
                    .into(holder.ivCover);
        } else if (holder.ivCover != null) {
            holder.ivCover.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        }

        // --- ĐỔI MÀU CHỮ DỰA TRÊN CÔNG TẮC DARK MODE ---
        if (isDarkMode) {
            // Nền tối -> Chữ Trắng, Tác giả Xám sáng
            holder.tvTitle.setTextColor(android.graphics.Color.WHITE);
            if (holder.tvAuthor != null) {
                holder.tvAuthor.setTextColor(android.graphics.Color.parseColor("#B0BEC5"));
            }
        } else {
            // Nền sáng -> Chữ Xanh đen, Tác giả Xám
            holder.tvTitle.setTextColor(android.graphics.Color.parseColor("#1B263B"));
            if (holder.tvAuthor != null) {
                holder.tvAuthor.setTextColor(android.graphics.Color.parseColor("#6D7885"));
            }
        }
        // -------------------------------------------------

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), com.example.fonosshoppee.BookDetailActivity.class);

            intent.putExtra("BOOK_TITLE", item.getTitle());
            intent.putExtra("BOOK_AUTHOR", item.getAuthor());
            intent.putExtra("BOOK_COVER", item.getCoverUrl());
            intent.putExtra("AUDIO_URL", item.getAudioUrl());

            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;
        ImageView ivCover;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            ivCover = itemView.findViewById(R.id.ivBookCover);
        }
    }
}