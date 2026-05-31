package com.example.fonosshoppee.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
// Import Model mà bạn đã tạo ở bước trước
import com.example.fonosshoppee.model.ChallengeItem;

// Import Firebase
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChallengeFragment extends Fragment {

    private RecyclerView rvChallenges;
    private ChallengeAdapter adapter;
    private List<ChallengeItem> list;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        // 1. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // 2. Thiết lập RecyclerView
        rvChallenges = view.findViewById(R.id.rvChallenges);
        rvChallenges.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Tạo danh sách rỗng và cài đặt Adapter
        list = new ArrayList<>();
        adapter = new ChallengeAdapter(list);
        rvChallenges.setAdapter(adapter);

        // 4. Kéo dữ liệu từ Firebase
        loadChallengeData();

        return view;
    }

    private void loadChallengeData() {
        // Lấy dữ liệu từ collection "challenges"
        db.collection("challenges")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear(); // Xóa dữ liệu cũ
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ChallengeItem item = document.toObject(ChallengeItem.class);
                        list.add(item);
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật giao diện
                    Log.d("Firebase", "Tải dữ liệu thử thách thành công! Số lượng: " + list.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải thử thách: " + e.getMessage()));
    }

    // ================= CLASS ADAPTER =================
    public static class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {
        List<ChallengeItem> items;

        public ChallengeAdapter(List<ChallengeItem> items) {
            this.items = items;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChallengeItem item = items.get(position);

            // Dùng các hàm Getter từ class Model
            holder.tvEmoji.setText(item.getEmoji());
            holder.tvTitle.setText(item.getTitle());

            // Xử lý hiển thị tiến độ
            if (item.isCompleted()) {
                holder.tvProgress.setText("✔"); // Thay bằng dấu tick
                holder.tvProgress.setTextColor(Color.parseColor("#F25B3E")); // Màu cam Fonos
                holder.tvProgress.setTextSize(18);
            } else {
                holder.tvProgress.setText(item.getProgressText());
                holder.tvProgress.setTextColor(Color.parseColor("#6B7280")); // Màu xám
                holder.tvProgress.setTextSize(14);
            }

            // Xử lý nút Khám phá
            if (item.getActionButtonText() != null && !item.getActionButtonText().isEmpty()) {
                holder.tvActionButton.setVisibility(View.VISIBLE);
                holder.tvActionButton.setText(item.getActionButtonText());
            } else {
                holder.tvActionButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

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