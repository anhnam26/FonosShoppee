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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.model.PodCourseItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PodCourseFragment extends Fragment {

    private FirebaseFirestore db;
    private List<PodCourseItem> podList;
    private RecyclerView rvMain;
    private MainCategoryAdapter adapter;
    private List<PodCategory> categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcourse, container, false);

        db = FirebaseFirestore.getInstance();
        podList = new ArrayList<>();
        categories = new ArrayList<>();

        rvMain = view.findViewById(R.id.rvMainPodCourse);
        rvMain.setLayoutManager(new LinearLayoutManager(getContext())); // Cuộn dọc

        adapter = new MainCategoryAdapter(categories);
        rvMain.setAdapter(adapter);

        loadPodCoursesFromFirebase();

        return view;
    }

    private void loadPodCoursesFromFirebase() {
        db.collection("podcourses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    podList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PodCourseItem item = document.toObject(PodCourseItem.class);
                        podList.add(item);
                    }

                    // Sau khi có dữ liệu thật, tạo các danh mục và nhét chung list này vào để hiển thị
                    categories.clear();
                    categories.add(new PodCategory("PodCourse miễn phí", "Dành Cho Hội Viên", podList));
                    categories.add(new PodCategory("", "PodCourse Mới Ra Mắt", podList));
                    categories.add(new PodCategory("", "Top Thịnh Hành Hôm Nay", podList));
                    categories.add(new PodCategory("Từ kinh nghiệm được đúc kết", "Kinh Doanh & Khởi Nghiệp \uD83D\uDE80", podList));
                    categories.add(new PodCategory("Chia sẻ từ chuyên gia", "Phát Triển Cá Nhân \uD83C\uDFAF", podList));
                    categories.add(new PodCategory("Từ kinh nghiệm được đúc kết", "Sức Khỏe \uD83C\uDF31", podList));

                    adapter.notifyDataSetChanged();
                    Log.d("Firebase", "Tải PodCourse thành công! Số lượng: " + podList.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải PodCourse: " + e.getMessage()));
    }

    // ================= CLASS MODEL CHO DANH MỤC =================
    public static class PodCategory {
        String subTitle, mainTitle;
        List<PodCourseItem> pods; // Đổi từ String sang PodCourseItem
        public PodCategory(String subTitle, String mainTitle, List<PodCourseItem> pods) {
            this.subTitle = subTitle;
            this.mainTitle = mainTitle;
            this.pods = pods;
        }
    }

    // ================= ADAPTER CUỘN DỌC (DANH MỤC LỚN) =================
    public static class MainCategoryAdapter extends RecyclerView.Adapter<MainCategoryAdapter.CatViewHolder> {
        List<PodCategory> list;
        public MainCategoryAdapter(List<PodCategory> list) { this.list = list; }

        @NonNull @Override
        public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_podcourse, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
            PodCategory cat = list.get(position);
            holder.tvMain.setText(cat.mainTitle);

            if (cat.subTitle == null || cat.subTitle.isEmpty()) {
                holder.tvSub.setVisibility(View.GONE);
            } else {
                holder.tvSub.setVisibility(View.VISIBLE);
                holder.tvSub.setText(cat.subTitle);
            }

            // Gắn Adapter cuộn ngang cho từng danh mục
            holder.rv.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.rv.setAdapter(new PodAdapter(cat.pods));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class CatViewHolder extends RecyclerView.ViewHolder {
            TextView tvSub, tvMain;
            RecyclerView rv;
            public CatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSub = itemView.findViewById(R.id.tvSubTitle);
                tvMain = itemView.findViewById(R.id.tvMainTitle);
                rv = itemView.findViewById(R.id.rvPodList);
            }
        }
    }

    // ================= ADAPTER CUỘN NGANG (THẺ PODCOURSE) =================
    public static class PodAdapter extends RecyclerView.Adapter<PodAdapter.PodViewHolder> {
        List<PodCourseItem> pods;
        public PodAdapter(List<PodCourseItem> pods) { this.pods = pods; }

        @NonNull @Override
        public PodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_podcourse, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PodViewHolder holder, int position) {
            PodCourseItem item = pods.get(position);

            holder.tvTitle.setText(item.getTitle());
            if (holder.tvAuthor != null) holder.tvAuthor.setText(item.getAuthor());

            // Load ảnh bằng Glide
            if (holder.ivCover != null && item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.getCoverUrl())
                        .into(holder.ivCover);
            } else if (holder.ivCover != null) {
                holder.ivCover.setBackgroundColor(Color.parseColor("#E0E0E0"));
            }
        }

        @Override
        public int getItemCount() { return pods.size(); }

        static class PodViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor;
            ImageView ivCover; // Thêm ImageView

            public PodViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvPodTitle);
                tvAuthor = itemView.findViewById(R.id.tvPodAuthor);
                // CHÚ Ý: Đảm bảo file item_podcourse.xml của bạn có thẻ ImageView mang ID này nhé!
                ivCover = itemView.findViewById(R.id.ivPodCover);
            }
        }
    }
}