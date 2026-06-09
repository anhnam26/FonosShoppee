package com.example.fonosshoppee;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.adapter.BookAdapter;
import com.example.fonosshoppee.model.BookItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContentListActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private ImageView btnBack, ivHeaderBackground;
    private TextView tvPageTitle, tvPageSubtitle;

    private RecyclerView recyclerViewFeatured, recyclerViewAll;
    private BookAdapter adapterFeatured, adapterAll;
    private List<BookItem> listFeatured, listAll;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        // Ánh xạ View
        mainLayout = findViewById(R.id.mainLayout);
        btnBack = findViewById(R.id.btnBack);
        ivHeaderBackground = findViewById(R.id.ivHeaderBackground);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvPageSubtitle = findViewById(R.id.tvPageSubtitle);
        recyclerViewFeatured = findViewById(R.id.recyclerViewFeatured);
        recyclerViewAll = findViewById(R.id.recyclerViewAll);

        btnBack.setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();

        // Thiết lập RecyclerView
        listFeatured = new ArrayList<>();
        listAll = new ArrayList<>();

        adapterFeatured = new BookAdapter(listFeatured);
        adapterAll = new BookAdapter(listAll);

        // --- BẬT CHẾ ĐỘ NỀN TỐI (CHỮ TRẮNG) ---
        adapterFeatured.setDarkMode(true);
        adapterAll.setDarkMode(true);
        // --------------------------------------

        // Mục Nổi Bật: Vuốt ngang (Horizontal)
        recyclerViewFeatured.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerViewFeatured.setAdapter(adapterFeatured);

        // Mục Tất cả: Dạng Lưới (Grid 2 cột)
        recyclerViewAll.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewAll.setAdapter(adapterAll);

        // LẤY DỮ LIỆU ĐỘNG TỪ INTENT
        String contentType = getIntent().getStringExtra("CONTENT_TYPE");
        String collectionName = "books"; // Mặc định
        String headerImageUrl = ""; // Biến chứa link ảnh nền mờ phía trên cùng

        // TÙY CHỈNH GIAO DIỆN VÀ MÀU SẮC DỰA TRÊN THỂ LOẠI
        if (contentType != null) {
            switch (contentType) {
                case "SLEEP_STORY":
                    mainLayout.setBackgroundColor(Color.parseColor("#0F172A"));
                    tvPageTitle.setText("Truyện ngủ");
                    tvPageSubtitle.setText("Chuyện kể nhẹ nhàng cho giấc ngủ sâu");
                    collectionName = "sleep_stories";
                    headerImageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800";
                    break;
                case "RELAX_MUSIC":
                    mainLayout.setBackgroundColor(Color.parseColor("#4A2B4D"));
                    tvPageTitle.setText("Nhạc chủ đề");
                    tvPageSubtitle.setText("Thăng hoa và hiệu quả hơn với bản nhạc nền");
                    collectionName = "relax_music";
                    headerImageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=800";
                    break;
                case "PODCAST":
                    mainLayout.setBackgroundColor(Color.parseColor("#1C1C1E"));
                    tvPageTitle.setText("Podcast");
                    tvPageSubtitle.setText("Lắng nghe những cuộc trò chuyện thú vị");
                    recyclerViewAll.setLayoutManager(new GridLayoutManager(this, 1));
                    collectionName = "podcasts";
                    headerImageUrl = "https://images.unsplash.com/photo-1593697821252-0c9137d9fc45?w=800";
                    break;
                case "MEDITATION":
                    mainLayout.setBackgroundColor(Color.parseColor("#243B35"));
                    tvPageTitle.setText("Thiền");
                    tvPageSubtitle.setText("Tìm lại sự tĩnh lặng từ bên trong");
                    collectionName = "meditations";
                    headerImageUrl = "https://images.unsplash.com/photo-1508672019048-805c876b67e2?w=800";
                    break;
                case "ENGLISH_BOOKS":
                    mainLayout.setBackgroundColor(Color.parseColor("#1B3B5A"));
                    tvPageTitle.setText("Sách Tiếng Anh");
                    tvPageSubtitle.setText("Nâng cao kỹ năng với nguyên bản tiếng Anh");
                    collectionName = "english_books";
                    headerImageUrl = "https://images.unsplash.com/photo-1546422904-90eab23c3d7e?w=800";
                    break;
                case "FREE_BOOKS":
                    mainLayout.setBackgroundColor(Color.parseColor("#A84224"));
                    tvPageTitle.setText("Sách Miễn Phí");
                    tvPageSubtitle.setText("Nghe trọn vẹn không cần đăng ký");
                    collectionName = "free_books";
                    headerImageUrl = "https://images.unsplash.com/photo-1604866830893-c13cafa515d5?w=800";
                    break;
                case "KIDS":
                    mainLayout.setBackgroundColor(Color.parseColor("#3C6496"));
                    tvPageTitle.setText("Sách Thiếu Nhi");
                    tvPageSubtitle.setText("Nuôi dưỡng tâm hồn bé yêu");
                    collectionName = "kids_books";
                    headerImageUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800";
                    break;
                case "NEW_RELEASES":
                    mainLayout.setBackgroundColor(Color.parseColor("#2C3E50"));
                    tvPageTitle.setText("Mới Ra Mắt");
                    tvPageSubtitle.setText("Những tựa sách vừa cập bến Fonos Shoppee");
                    collectionName = "ALL_COLLECTIONS";
                    headerImageUrl = "https://images.unsplash.com/photo-1456953180671-730de08edaa7?w=800";
                    break;
            }
        }

        // Tải ảnh nền mờ phía trên (Header) bằng Glide
        if (!headerImageUrl.isEmpty()) {
            Glide.with(this).load(headerImageUrl).into(ivHeaderBackground);
        }

        loadDataFromFirebase(collectionName);
    }

    private void loadDataFromFirebase(String collectionName) {
        if ("ALL_COLLECTIONS".equals(collectionName)) {
            // Lệnh gom tất cả sách cho mục Mới ra mắt
            String[] allCollections = {"books", "audiobooks", "sleep_stories", "relax_music", "podcasts", "meditations", "english_books", "free_books", "kids_books"};
            final int[] completed = {0};
            List<BookItem> tempAllList = new ArrayList<>();

            for (String colName : allCollections) {
                db.collection(colName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookItem item = document.toObject(BookItem.class);
                        boolean isDup = false;
                        for (BookItem b : tempAllList) {
                            if (b.getTitle() != null && b.getTitle().equalsIgnoreCase(item.getTitle())) { isDup = true; break; }
                        }
                        if (!isDup && item.getTitle() != null) tempAllList.add(item);
                    }
                    completed[0]++;
                    if (completed[0] == allCollections.length) {
                        Collections.shuffle(tempAllList); // Đảo ngẫu nhiên để giống sách mới

                        listFeatured.clear();
                        listAll.clear();
                        for(int i = 0; i < tempAllList.size(); i++) {
                            if(i < 3) listFeatured.add(tempAllList.get(i)); // 3 cuốn nổi bật ngang
                            else listAll.add(tempAllList.get(i));           // Còn lại dọc
                        }
                        adapterFeatured.notifyDataSetChanged();
                        adapterAll.notifyDataSetChanged();
                    }
                });
            }
        } else {
            // ĐÂY LÀ ĐOẠN CODE DÀNH CHO CÁC MỤC LẺ
            db.collection(collectionName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<BookItem> tempList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    BookItem item = document.toObject(BookItem.class);
                    if (item.getTitle() != null && !item.getTitle().isEmpty()) {
                        tempList.add(item);
                    }
                }

                // Xáo trộn sách để mục "Gợi ý dành cho bạn" luôn mới mẻ mỗi lần vào
                Collections.shuffle(tempList);

                listFeatured.clear();
                listAll.clear();
                for (int i = 0; i < tempList.size(); i++) {
                    if (i < 3) listFeatured.add(tempList.get(i)); // 3 cuốn đầu vào mục Vuốt ngang
                    else listAll.add(tempList.get(i)); // Còn lại vào mục Lưới dọc
                }

                adapterFeatured.notifyDataSetChanged();
                adapterAll.notifyDataSetChanged();

            }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
        }
    }
}