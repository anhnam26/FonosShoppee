package com.example.fonosshoppee.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import thư viện Glide và Firebase
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Import Model
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.model.DiscoverItem;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private RecyclerView rvGrid;
    private DiscoverAdapter adapter;
    private List<DiscoverItem> list;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        rvGrid = view.findViewById(R.id.rvDiscoverGrid);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        // Khởi tạo danh sách rỗng
        list = new ArrayList<>();
        adapter = new DiscoverAdapter(list);

        // Thuật toán chia cột (Lấy biến fullWidth từ class Model)
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return list.get(position).isFullWidth() ? 2 : 1;
            }
        });

        rvGrid.setLayoutManager(gridLayoutManager);
        rvGrid.setAdapter(adapter);

        // Kéo dữ liệu từ Firebase
        loadDiscoverData();

        return view;
    }

    private void loadDiscoverData() {
        // Lấy dữ liệu từ collection "discover"
        db.collection("discover")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DiscoverItem item = document.toObject(DiscoverItem.class);
                        list.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("Firebase", "Tải dữ liệu khám phá thành công! Số thẻ: " + list.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải dữ liệu Khám phá: " + e.getMessage()));
    }

    // ================= CLASS ADAPTER =================
    public static class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.ViewHolder> {
        List<DiscoverItem> items;
        public DiscoverAdapter(List<DiscoverItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DiscoverItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());

            ViewGroup.LayoutParams layoutParams = holder.cardContainer.getLayoutParams();
            float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;

            if (item.getTitle().equals("Gợi Ý Nhanh")) {
                // Tiêu đề Gợi Ý Nhanh
                holder.cardBackground.setBackgroundColor(Color.TRANSPARENT);
                holder.cardBackground.setImageDrawable(null); // Xóa ảnh nếu có
                holder.tvTitle.setTextColor(Color.parseColor("#1B263B"));
                holder.tvTitle.setTextSize(20);

                // ÁP DỤNG PADDING LÊN TEXTVIEW (Chữ), KHÔNG PHẢI IMAGEVIEW
                holder.tvTitle.setPadding(
                        (int)(4 * scale), (int)(24 * scale), (int)(16 * scale), (int)(8 * scale)
                );

                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            } else {
                holder.tvTitle.setTextColor(Color.WHITE);
                holder.tvTitle.setTextSize(16);

                // ÁP DỤNG PADDING LÊN TEXTVIEW
                int p16 = (int)(16 * scale);
                holder.tvTitle.setPadding(p16, p16, p16, p16);

                layoutParams.height = (int) (item.getHeightDp() * scale + 0.5f);

                // Load ảnh bằng Glide nếu có link ảnh
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(item.getImageUrl())
                            .into(holder.cardBackground);
                } else {
                    // Nếu chưa có ảnh, đổ màu nền Hex
                    holder.cardBackground.setImageDrawable(null);
                    try {
                        holder.cardBackground.setBackgroundColor(Color.parseColor(item.getColorCode()));
                    } catch (Exception e) {
                        holder.cardBackground.setBackgroundColor(Color.parseColor("#4A4A4A"));
                    }
                }
            }

            holder.cardContainer.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            CardView cardContainer;
            // Đã đổi từ View thành ImageView
            ImageView cardBackground;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvCardTitle);
                cardContainer = itemView.findViewById(R.id.cardContainer);
                // ID này phải khớp với <ImageView> trong item_discover_card.xml
                cardBackground = itemView.findViewById(R.id.cardBackground);
            }
        }
    }
}