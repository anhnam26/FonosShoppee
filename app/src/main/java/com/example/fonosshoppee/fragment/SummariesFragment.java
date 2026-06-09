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

public class SummariesFragment extends Fragment {

    private FirebaseFirestore db;
    private List<BookItem> bookList;
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng đúng layout của màn hình Tóm tắt sách
        fragmentView = inflater.inflate(R.layout.fragment_booksummary, container, false);

        // Khởi tạo Firebase và danh sách
        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        // Kéo dữ liệu từ Firebase
        loadSummariesFromFirebase();

        return fragmentView;
    }

    private void loadSummariesFromFirebase() {
        // lấy chung dữ liệu collection "audiobooks".

        db.collection("audiobooks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookItem item = document.toObject(BookItem.class);
                        bookList.add(item);
                    }

                    // Gắn dữ liệu vào 2 RecyclerView theo đúng ID của bạn
                    setupRecyclerView(fragmentView, R.id.rvRecommend, bookList);
                    setupRecyclerView(fragmentView, R.id.rvNew, bookList);

                    Log.d("Firebase", "Tải Tóm tắt sách thành công! Số lượng: " + bookList.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải Tóm tắt sách: " + e.getMessage()));
    }

    // Hàm thiết lập RecyclerView gọn gàng
    private void setupRecyclerView(View view, int recyclerViewId, List<BookItem> booksData) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            // Đã truyền đúng List<BookItem> vào BookAdapter
            recyclerView.setAdapter(new BookAdapter(booksData));
        }
    }
}