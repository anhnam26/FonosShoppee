package com.example.fonosshoppee.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;

import java.util.ArrayList;
import java.util.List;

public class AudiobooksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_audiobooks, container, false);

        //dữ liệu giả
        List<String> dummyBooks = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            dummyBooks.add("Sách " + i);
        }

        setupRecyclerView(view, R.id.rvForYou, dummyBooks);
        setupRecyclerView(view, R.id.rvBestSellers, dummyBooks);
        setupRecyclerView(view, R.id.rvMembersBooks, dummyBooks);
        setupRecyclerView(view, R.id.rvBestChildBooks, dummyBooks);
        setupRecyclerView(view, R.id.rvBussinessBooks, dummyBooks);
        setupRecyclerView(view, R.id.rvAbsoluteCinemas, dummyBooks);
        setupRecyclerView(view, R.id.rvRaisingChildren, dummyBooks);
        setupRecyclerView(view, R.id.rvTenMarkBooks, dummyBooks);
        setupRecyclerView(view, R.id.rvWorldBest, dummyBooks);
        setupRecyclerView(view, R.id.rvForYourHealth, dummyBooks);
        setupRecyclerView(view, R.id.rvStoryOfFamousPp, dummyBooks);

        return view;
    }

    private void setupRecyclerView(View view, int recyclerViewId, List<String> booksData) {
        RecyclerView recyclerView = view.findViewById(recyclerViewId);
        if (recyclerView != null) {

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            recyclerView.setAdapter(new BookAdapter(booksData));
        }
    }
}