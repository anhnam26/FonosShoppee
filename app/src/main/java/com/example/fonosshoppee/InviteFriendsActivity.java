package com.example.fonosshoppee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InviteFriendsActivity extends AppCompatActivity {

    private RecyclerView rvInviteBooks;
    private Button btnShare;
    private InviteAdapter adapter;
    private List<BookItem> bookList;
    private FirebaseFirestore db;

    // Lưu các tựa sách người dùng đã chọn
    private Set<String> selectedBooks = new HashSet<>();

    private final String[] allCollections = {"books", "audiobooks", "sleep_stories", "relax_music", "podcasts", "meditations", "english_books", "free_books", "kids_books"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvInviteBooks = findViewById(R.id.rvInviteBooks);
        btnShare = findViewById(R.id.btnShare);
        db = FirebaseFirestore.getInstance();

        bookList = new ArrayList<>();
        adapter = new InviteAdapter(bookList);
        rvInviteBooks.setLayoutManager(new LinearLayoutManager(this));
        rvInviteBooks.setAdapter(adapter);

        loadAllBooks();

        // XỬ LÝ CHIA SẺ
        btnShare.setOnClickListener(v -> {
            if (selectedBooks.isEmpty()) return;

            StringBuilder shareText = new StringBuilder("Ê, vào Fonos Shoppee nghe sách với tôi đi! Ứng dụng đỉnh lắm. \nTôi đang đề cử bạn nghe các cuốn này:\n\n");
            int i = 1;
            for (String title : selectedBooks) {
                shareText.append(i).append(". ").append(title).append("\n");
                i++;
            }
            shareText.append("\n👉 Tải app và nghe ngay nhé!");

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Gửi lời mời qua:"));
        });
    }

    private void loadAllBooks() {
        final int[] completed = {0};
        for (String colName : allCollections) {
            db.collection(colName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    BookItem item = document.toObject(BookItem.class);
                    // Lọc trùng lặp
                    boolean isDup = false;
                    for (BookItem b : bookList) {
                        if (b.getTitle() != null && b.getTitle().equalsIgnoreCase(item.getTitle())) { isDup = true; break; }
                    }
                    if (!isDup && item.getTitle() != null) bookList.add(item);
                }
                completed[0]++;
                if (completed[0] == allCollections.length) {
                    Collections.shuffle(bookList);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    // ADAPTER
    class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> {
        private List<BookItem> items;
        public InviteAdapter(List<BookItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "Fonos Shoppee");

            if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.getCoverUrl()).into(holder.ivCover);
            }

            // Gỡ bỏ listener cũ trước khi set trạng thái để tránh lỗi cuộn màn hình
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(selectedBooks.contains(item.getTitle()));

            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedBooks.add(item.getTitle());
                else selectedBooks.remove(item.getTitle());

                // Cập nhật trạng thái nút Share
                if (selectedBooks.isEmpty()) {
                    btnShare.setEnabled(false);
                    btnShare.setText("Chọn ít nhất 1 cuốn sách để chia sẻ");
                } else {
                    btnShare.setEnabled(true);
                    btnShare.setText("Chia sẻ " + selectedBooks.size() + " cuốn sách");
                }
            });

            // Bấm vào cả hàng cũng tính là check
            holder.itemView.setOnClickListener(v -> holder.cbSelect.setChecked(!holder.cbSelect.isChecked()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
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
}