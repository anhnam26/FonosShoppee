package com.example.fonosshoppee.Main;

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

public class PodCourseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_podcourse, container, false);

        RecyclerView rvMain = view.findViewById(R.id.rvMainPodCourse);
        rvMain.setLayoutManager(new LinearLayoutManager(getContext())); // Cuộn dọc

        List<String> podList1 = new ArrayList<>();
        for (int i=1; i<=5; i++) podList1.add("Khóa học " + i);

        List<PodCategory> categories = new ArrayList<>();
        categories.add(new PodCategory("PodCourse miễn phí", "Dành Cho Hội Viên", podList1));
        categories.add(new PodCategory("", "PodCourse Mới Ra Mắt", podList1));
        categories.add(new PodCategory("", "Top Thịnh Hành Hôm Nay", podList1));
        categories.add(new PodCategory("Từ kinh nghiệm được đúc kết", "Kinh Doanh & Khởi Nghiệp \uD83D\uDE80", podList1));
        categories.add(new PodCategory("Chia sẻ từ chuyên gia", "Phát Triển Cá Nhân \uD83C\uDFAF", podList1));
        categories.add(new PodCategory("Từ kinh nghiệm được đúc kết", "Sức Khỏe \uD83C\uDF31", podList1));

        //Set Adapter
        MainCategoryAdapter adapter = new MainCategoryAdapter(categories);
        rvMain.setAdapter(adapter);

        return view;
    }


    public static class PodCategory {
        String subTitle, mainTitle;
        List<String> pods;
        public PodCategory(String subTitle, String mainTitle, List<String> pods) {
            this.subTitle = subTitle;
            this.mainTitle = mainTitle;
            this.pods = pods;
        }
    }

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

            if (cat.subTitle.isEmpty()) {
                holder.tvSub.setVisibility(View.GONE);
            } else {
                holder.tvSub.setVisibility(View.VISIBLE);
                holder.tvSub.setText(cat.subTitle);
            }

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

    public static class PodAdapter extends RecyclerView.Adapter<PodAdapter.PodViewHolder> {
        List<String> pods;
        public PodAdapter(List<String> pods) { this.pods = pods; }

        @NonNull @Override
        public PodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PodViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_podcourse, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PodViewHolder holder, int position) {
            holder.tvTitle.setText(pods.get(position));
            holder.tvAuthor.setText("Tác giả " + (position + 1));
        }

        @Override
        public int getItemCount() { return pods.size(); }

        static class PodViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor;
            public PodViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvPodTitle);
                tvAuthor = itemView.findViewById(R.id.tvPodAuthor);
            }
        }
    }
}