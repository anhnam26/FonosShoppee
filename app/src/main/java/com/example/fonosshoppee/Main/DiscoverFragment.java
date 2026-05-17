package com.example.fonosshoppee.Main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        RecyclerView rvGrid = view.findViewById(R.id.rvDiscoverGrid);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        List<DiscoverItem> list = new ArrayList<>();

        list.add(new DiscoverItem("PodCourse - Cập Nhật Mới\nHội viên giờ đây có thể xem miễn phí hơn 30 PodCourse trên Fonos!", true, 160, "#2B2D42"));

        list.add(new DiscoverItem("Sách Nói", false, 100, "#8D5524"));
        list.add(new DiscoverItem("Sách Tiếng Anh", false, 100, "#4527A0"));
        list.add(new DiscoverItem("Ebook", false, 100, "#2E7D32"));
        list.add(new DiscoverItem("Tóm Tắt Sách", false, 100, "#D84315"));
        list.add(new DiscoverItem("Thiếu Nhi", false, 100, "#C62828"));
        list.add(new DiscoverItem("Thiền", false, 100, "#00838F"));
        list.add(new DiscoverItem("Truyện Ngủ & Nhạc", false, 100, "#283593"));
        list.add(new DiscoverItem("Podcast", false, 100, "#4CAF50"));

        list.add(new DiscoverItem("Gợi Ý Nhanh", true, 40, "#00000000")); // Tiêu đề (Màu trong suốt)
        list.add(new DiscoverItem("Miễn Phí Cho Hội Viên", true, 120, "#37474F"));
        list.add(new DiscoverItem("Bảng Xếp Hạng", false, 100, "#263238"));
        list.add(new DiscoverItem("Top Reviewer", false, 100, "#BCAAA4"));
        list.add(new DiscoverItem("Sách Miễn Phí", false, 100, "#558B2F"));
        list.add(new DiscoverItem("Mới Ra Mắt", false, 100, "#1A237E"));
        list.add(new DiscoverItem("Rủ Bạn Nhập Hội\nTặng Quà Cả Đôi", true, 100, "#424242"));

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return list.get(position).isFullWidth ? 2 : 1;
            }
        });

        DiscoverAdapter adapter = new DiscoverAdapter(list);
        rvGrid.setLayoutManager(gridLayoutManager);
        rvGrid.setAdapter(adapter);

        return view;
    }

    //CLASS MODEL
    public static class DiscoverItem {
        String title;
        boolean isFullWidth;
        int heightDp;
        String colorCode;

        public DiscoverItem(String title, boolean isFullWidth, int heightDp, String colorCode) {
            this.title = title;
            this.isFullWidth = isFullWidth;
            this.heightDp = heightDp;
            this.colorCode = colorCode;
        }
    }

    //CLASS ADAPTER
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
            holder.tvTitle.setText(item.title);

            ViewGroup.LayoutParams layoutParams = holder.cardContainer.getLayoutParams();

            float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;

            if (item.title.equals("Gợi Ý Nhanh")) {

                holder.cardBackground.setBackgroundColor(Color.TRANSPARENT);
                holder.tvTitle.setTextColor(Color.parseColor("#1B263B"));
                holder.tvTitle.setTextSize(20);

                holder.cardBackground.setPadding(
                        (int)(4 * scale), (int)(24 * scale), (int)(16 * scale), (int)(8 * scale)
                );

                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            } else {

                try {
                    holder.cardBackground.setBackgroundColor(Color.parseColor(item.colorCode));
                } catch (Exception e) {
                    holder.cardBackground.setBackgroundColor(Color.parseColor("#4A4A4A"));
                }

                holder.tvTitle.setTextColor(Color.WHITE);
                holder.tvTitle.setTextSize(16);

                int p16 = (int)(16 * scale);
                holder.cardBackground.setPadding(p16, p16, p16, p16);

                layoutParams.height = (int) (item.heightDp * scale + 0.5f);
            }

            holder.cardContainer.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            CardView cardContainer;
            View cardBackground;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvCardTitle);
                cardContainer = itemView.findViewById(R.id.cardContainer);
                cardBackground = itemView.findViewById(R.id.cardBackground);
            }
        }
    }
}