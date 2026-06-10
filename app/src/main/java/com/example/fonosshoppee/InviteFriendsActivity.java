package com.example.fonosshoppee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.adapter.InviteAdapter;
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
    private final Set<String> selectedBooks = new HashSet<>();

    private final String[] allCollections = {
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
        setContentView(R.layout.activity_invite_friends);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvInviteBooks = findViewById(R.id.rvInviteBooks);
        btnShare = findViewById(R.id.btnShare);
        db = FirebaseFirestore.getInstance();

        bookList = new ArrayList<>();
        adapter = new InviteAdapter(bookList, selectedBooks, this::updateShareButton);
        rvInviteBooks.setLayoutManager(new LinearLayoutManager(this));
        rvInviteBooks.setAdapter(adapter);

        loadAllBooks();

        btnShare.setOnClickListener(v -> {
            if (selectedBooks.isEmpty()) return;

            StringBuilder shareText = new StringBuilder(
                    "Ê, vào Fonos Shoppee nghe sách với tôi đi! Ứng dụng đỉnh lắm.\n" +
                            "Tôi đang đề cử bạn nghe các cuốn này:\n\n"
            );
            int i = 1;
            for (String title : selectedBooks) {
                shareText.append(i).append(". ").append(title).append("\n");
                i++;
            }
            shareText.append("\nTải app và nghe ngay nhé!");

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Gửi lời mời qua:"));
        });
    }

    private void updateShareButton(int selectedCount) {
        if (selectedCount == 0) {
            btnShare.setEnabled(false);
            btnShare.setText("Chọn ít nhất 1 cuốn sách để chia sẻ");
        } else {
            btnShare.setEnabled(true);
            btnShare.setText("Chia sẻ " + selectedCount + " cuốn sách");
        }
    }

    private void loadAllBooks() {
        final int[] completed = {0};
        for (String colName : allCollections) {
            db.collection(colName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    BookItem item = document.toObject(BookItem.class);
                    boolean isDup = false;
                    for (BookItem b : bookList) {
                        if (b.getTitle() != null && b.getTitle().equalsIgnoreCase(item.getTitle())) {
                            isDup = true;
                            break;
                        }
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
}
