package com.example.fonosshoppee.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;
import com.example.fonosshoppee.adapter.DiscoverAdapter;
import com.example.fonosshoppee.model.DiscoverItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private RecyclerView rvGrid;
    private DiscoverAdapter adapter;
    private List<DiscoverItem> list;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        db = FirebaseFirestore.getInstance();
        rvGrid = view.findViewById(R.id.rvDiscoverGrid);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        list = new ArrayList<>();
        adapter = new DiscoverAdapter(list);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return list.get(position).isFullWidth() ? 2 : 1;
            }
        });

        rvGrid.setLayoutManager(gridLayoutManager);
        rvGrid.setAdapter(adapter);

        loadDiscoverData();

        return view;
    }

    private void loadDiscoverData() {
        db.collection("discover")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DiscoverItem item = document.toObject(DiscoverItem.class);
                        list.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi tải dữ liệu Khám phá: " + e.getMessage()));
    }
}
