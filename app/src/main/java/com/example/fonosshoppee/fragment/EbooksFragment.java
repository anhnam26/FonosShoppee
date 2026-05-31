package com.example.fonosshoppee.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;
import com.example.fonosshoppee.adapter.BookAdapter;

// Import Model và Firebase
import com.example.fonosshoppee.model.BookItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EbooksFragment extends Fragment {

    private FirebaseFirestore db;
    private List<BookItem> bookList;
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đúng chuẩn tên file giao diện fragment_ebook.xml
        fragmentView = inflater.inflate(R.layout.fragment_ebook, container, false);

        // Khởi tạo Firebase và danh sách rỗng
        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        // Kéo dữ liệu Ebook từ Firebase
        loadEbooksFromFirebase();

        return fragmentView;
    }

    private void loadEbooksFromFirebase() {
        // Lưu ý: Tạm thời tôi đang lấy chung collection "audiobooks" để bạn thấy dữ liệu ngay.
        // Sau này bạn có thể lên Firebase tạo collection "ebooks" rồi sửa chữ "audiobooks" ở dưới thành "ebooks" nhé.
        db.collection("audiobooks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookItem item = document.toObject(BookItem.class);
                        bookList.add(item);
                    }

                    // Sau khi tải xong dữ liệu, lắp vào 2 danh sách ngang của bạn
                    setupRecyclerView(fragmentView, R.id.rvBestEbooks, bookList);
                    setupRecyclerView(fragmentView, R.id.rvFreeEbooks, bookList);

                    Log.d("Firebase", "Tải Ebook thành công! Số lượng: " + bookList.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải Ebook: " + e.getMessage()));
    }

    // Hàm dùng chung để thiết lập RecyclerView cho gọn code
    private void setupRecyclerView(View view, int recyclerViewId, List<BookItem> booksData) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            // Truyền dữ liệu BookItem thật vào BookAdapter
            recyclerView.setAdapter(new BookAdapter(booksData));
        }
    }
}