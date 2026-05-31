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

    // 1. Đổi kiểu dữ liệu từ List<String> thành List<BookItem>
    private List<BookItem> bookList;

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

        // 2. Gắn dữ liệu Tiêu đề và Tác giả
        holder.tvTitle.setText(item.getTitle());

        if (holder.tvAuthor != null && item.getAuthor() != null) {
            holder.tvAuthor.setText(item.getAuthor());
        }

        // 3. Dùng Glide để load ảnh bìa sách
        if (holder.ivCover != null && item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getCoverUrl())
                    .into(holder.ivCover);
        } else if (holder.ivCover != null) {
            // Nếu không có link ảnh, set một màu nền mặc định
            holder.ivCover.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        }
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
            // Bạn nhớ kiểm tra xem file item_book.xml của bạn đã có đủ 3 ID này chưa nhé!
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            ivCover = itemView.findViewById(R.id.ivBookCover);
        }
    }
}