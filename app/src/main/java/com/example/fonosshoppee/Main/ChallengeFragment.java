package com.example.fonosshoppee.Main; // Đổi lại đúng tên package của bạn

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;

import java.util.ArrayList;
import java.util.List;

public class ChallengeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        RecyclerView rvChallenges = view.findViewById(R.id.rvChallenges);
        rvChallenges.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Tạo Data giả dựa trên hình ảnh
        List<ChallengeItem> list = new ArrayList<>();

        // Thử thách có nút Khám phá
        list.add(new ChallengeItem("⏳", "Nghe Fonos 5 phút.", "0/5", "Khám phá Sách nói >", false));
        list.add(new ChallengeItem("🎬", "Xem 1 trailer PodCourse.", "0/1", "Khám phá PodCourse >", false));

        // Thử thách không có nút
        list.add(new ChallengeItem("🔥", "Nghe Fonos 60 phút.", "0/60", null, false));

        // Thử thách đã hoàn thành (isCompleted = true)
        list.add(new ChallengeItem("🔑", "Truy cập vào Fonos 3 ngày\nliên tiếp.", "", null, true));
        list.add(new ChallengeItem("🔑", "Đăng nhập vào Fonos.", "", null, true));

        // 2. Set Adapter
        ChallengeAdapter adapter = new ChallengeAdapter(list);
        rvChallenges.setAdapter(adapter);

        return view;
    }

    // ================= CLASS MODEL =================
    public static class ChallengeItem {
        String emoji, title, progressText, actionButtonText;
        boolean isCompleted;

        public ChallengeItem(String emoji, String title, String progressText, String actionButtonText, boolean isCompleted) {
            this.emoji = emoji;
            this.title = title;
            this.progressText = progressText;
            this.actionButtonText = actionButtonText;
            this.isCompleted = isCompleted;
        }
    }

    // ================= CLASS ADAPTER =================
    public static class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {
        List<ChallengeItem> items;
        public ChallengeAdapter(List<ChallengeItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChallengeItem item = items.get(position);

            holder.tvEmoji.setText(item.emoji);
            holder.tvTitle.setText(item.title);

            // Xử lý Vòng tròn tiến độ
            if (item.isCompleted) {
                holder.tvProgress.setText("✔"); // Thay bằng dấu tick
                holder.tvProgress.setTextColor(Color.parseColor("#F25B3E")); // Màu cam Fonos
                holder.tvProgress.setTextSize(18);
            } else {
                holder.tvProgress.setText(item.progressText);
                holder.tvProgress.setTextColor(Color.parseColor("#6B7280")); // Màu xám
                holder.tvProgress.setTextSize(14);
            }

            // Xử lý Nút Khám phá
            if (item.actionButtonText != null && !item.actionButtonText.isEmpty()) {
                holder.tvActionButton.setVisibility(View.VISIBLE);
                holder.tvActionButton.setText(item.actionButtonText);
            } else {
                holder.tvActionButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvProgress, tvActionButton;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvEmoji);
                tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
                tvProgress = itemView.findViewById(R.id.tvProgress);
                tvActionButton = itemView.findViewById(R.id.tvActionButton);
            }
        }
    }
}