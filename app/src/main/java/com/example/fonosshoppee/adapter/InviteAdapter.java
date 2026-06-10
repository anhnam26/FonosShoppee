package com.example.fonosshoppee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.model.BookItem;

import java.util.List;
import java.util.Set;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> {
    private final List<BookItem> items;
    private final Set<String> selectedBooks;
    private final OnSelectionChangedListener listener;

    public InviteAdapter(List<BookItem> items, Set<String> selectedBooks, OnSelectionChangedListener listener) {
        this.items = items;
        this.selectedBooks = selectedBooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookItem item = items.get(position);
        String title = item.getTitle();

        holder.tvTitle.setText(title);
        holder.tvAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "Fonos Shoppee");

        if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(item.getCoverUrl()).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(android.R.color.darker_gray);
        }

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(title != null && selectedBooks.contains(title));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (title == null || title.isEmpty()) return;

            if (isChecked) {
                selectedBooks.add(title);
            } else {
                selectedBooks.remove(title);
            }

            if (listener != null) {
                listener.onSelectionChanged(selectedBooks.size());
            }
        });

        holder.itemView.setOnClickListener(v -> holder.cbSelect.setChecked(!holder.cbSelect.isChecked()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor;
        ImageView ivCover;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvInviteTitle);
            tvAuthor = itemView.findViewById(R.id.tvInviteAuthor);
            ivCover = itemView.findViewById(R.id.ivInviteCover);
            cbSelect = itemView.findViewById(R.id.cbInviteSelect);
        }
    }
}
