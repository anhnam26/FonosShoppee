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

public class AudiobooksFragment extends Fragment {

    private FirebaseFirestore db;
    private List<BookItem> bookList;
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_audiobooks, container, false);

        // Khởi tạo Firebase và list
        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();

        // Tải dữ liệu từ mạng
        loadAudiobooksFromFirebase();

        return fragmentView;
    }

    private void loadAudiobooksFromFirebase() {
        // Lấy dữ liệu từ collection "audiobooks"
        db.collection("audiobooks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookItem item = document.toObject(BookItem.class);
                        bookList.add(item);
                    }

                    // Sau khi tải xong dữ liệu thật, gắn vào tất cả các RecyclerView
                    setupAllRecyclerViews();

                    Log.d("Firebase", "Tải Sách nói thành công! Số lượng: " + bookList.size());
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải Sách nói: " + e.getMessage()));
    }

    private void setupAllRecyclerViews() {
        // Tạm thời đổ chung 1 list dữ liệu thật cho tất cả các hàng
        setupRecyclerView(fragmentView, R.id.rvForYou, bookList);
        setupRecyclerView(fragmentView, R.id.rvBestSellers, bookList);
        setupRecyclerView(fragmentView, R.id.rvMembersBooks, bookList);
        setupRecyclerView(fragmentView, R.id.rvBestChildBooks, bookList);
        setupRecyclerView(fragmentView, R.id.rvBussinessBooks, bookList);
        setupRecyclerView(fragmentView, R.id.rvAbsoluteCinemas, bookList);
        setupRecyclerView(fragmentView, R.id.rvRaisingChildren, bookList);
        setupRecyclerView(fragmentView, R.id.rvTenMarkBooks, bookList);
        setupRecyclerView(fragmentView, R.id.rvWorldBest, bookList);
        setupRecyclerView(fragmentView, R.id.rvForYourHealth, bookList);
        setupRecyclerView(fragmentView, R.id.rvStoryOfFamousPp, bookList);
    }

    private void setupRecyclerView(View view, int recyclerViewId, List<BookItem> booksData) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            // Truyền dữ liệu thật vào BookAdapter
            recyclerView.setAdapter(new BookAdapter(booksData));
        }
    }
}