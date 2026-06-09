package com.example.fonosshoppee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.model.BookItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Spinner spinnerCategory;
    private RecyclerView rvLeaderboard;

    private LeaderboardAdapter adapter;
    private List<BookItem> bookList;
    private FirebaseFirestore db;

    private final String[] categoryNames = {
            "Tất cả danh mục",
            "Audiobooks",
            "Truyện Ngủ",
            "Nhạc Thư Giãn",
            "Podcast",
            "Thiền",
            "Sách Tiếng Anh",
            "Sách Miễn Phí",
            "Thiếu Nhi"
    };

    private final String[] collectionNames = {
            "books",
            "audiobooks",
            "sleep_stories",
            "relax_music",
            "podcasts",
            "meditations",
            "english_books",
            "free_books",
            "kids_books"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        btnBack = findViewById(R.id.btnBack);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        // Cài đặt RecyclerView
        bookList = new ArrayList<>();
        adapter = new LeaderboardAdapter(bookList);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        // Cài đặt Spinner (Menu chọn danh mục)
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(view != null && view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.WHITE);
                }

                // NÂNG CẤP XỬ LÝ: Nếu chọn "Tất cả danh mục" (Vị trí số 0)
                if (position == 0) {
                    loadAllCategoriesData();
                } else {
                    // Nếu chọn một danh mục cụ thể lẻ
                    loadRankingData(collectionNames[position]);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // HÀM GỘP TẤT CẢ SÁCH TỪ TẤT CẢ CÁC BẢNG TRÊN FIREBASE
    private void loadAllCategoriesData() {
        bookList.clear();
        adapter.notifyDataSetChanged();

        final int[] completedQueries = {0};
        final int totalQueries = collectionNames.length;

        for (String collectionName : collectionNames) {
            db.collection(collectionName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    BookItem item = document.toObject(BookItem.class);

                    // Kiểm tra thuật toán chống trùng lặp: Nếu sách đã được add từ bảng khác rồi thì không add lại nữa
                    boolean isDuplicate = false;
                    for (BookItem existing : bookList) {
                        if (existing.getTitle() != null && existing.getTitle().equalsIgnoreCase(item.getTitle())) {
                            isDuplicate = true;
                            break;
                        }
                    }

                    if (!isDuplicate && item.getTitle() != null && !item.getTitle().isEmpty()) {
                        bookList.add(item);
                    }
                }

                completedQueries[0]++;
                // Khi toàn bộ các bộ sưu tập dữ liệu bất đồng bộ đã gom về máy xong
                if (completedQueries[0] == totalQueries) {
                    Collections.shuffle(bookList); // Xáo trộn ngẫu nhiên toàn bộ tổng kho sách
                    adapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                completedQueries[0]++;
                if (completedQueries[0] == totalQueries) {
                    Collections.shuffle(bookList);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    // HÀM TẢI MỘT DANH MỤC RIÊNG LẺ
    private void loadRankingData(String collectionName) {
        db.collection(collectionName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            bookList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                BookItem item = document.toObject(BookItem.class);
                if (item.getTitle() != null && !item.getTitle().isEmpty()) {
                    bookList.add(item);
                }
            }

            Collections.shuffle(bookList); // Xáo trộn ngẫu nhiên danh mục lẻ
            adapter.notifyDataSetChanged();

            if (bookList.isEmpty()) {
                Toast.makeText(this, "Danh mục này chưa có dữ liệu sách!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }

    // ================= ADAPTER NỘI BỘ =================
    class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<BookItem> items;

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

            // Đổi màu sắc nổi bật cho Top 3
            if (position == 0) holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Vàng Top 1
            else if (position == 1) holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Bạc Top 2
            else if (position == 2) holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Đồng Top 3
            else holder.tvRank.setTextColor(Color.parseColor("#6D7885")); // Xám thường

            holder.tvTitle.setText(item.getTitle());
            holder.tvAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "Fonos Shoppee");

            if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.getCoverUrl()).into(holder.ivCover);
            } else {
                holder.ivCover.setImageResource(android.R.color.darker_gray);
            }

            // Click vào bất kỳ dòng nào đều bay về trang Detail để nghe và điều khiển phát nhạc ngầm
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(LeaderboardActivity.this, BookDetailActivity.class);
                intent.putExtra("BOOK_TITLE", item.getTitle());
                intent.putExtra("BOOK_AUTHOR", item.getAuthor());
                intent.putExtra("BOOK_COVER", item.getCoverUrl());
                intent.putExtra("AUDIO_URL", item.getAudioUrl());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
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
}