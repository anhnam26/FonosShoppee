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

public class EbooksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ebook, container, false);

        // Tạo dummy data (Sách 1 đến Sách 8)
        List<String> dummyBooks = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            dummyBooks.add("Sách " + i);
        }

        // Setup RecyclerView Mục 1
        RecyclerView rvForYou = view.findViewById(R.id.rvBestEbooks);
        rvForYou.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvForYou.setAdapter(new BookAdapter(dummyBooks));

        // Setup RecyclerView Mục 2
        RecyclerView rvBestSellers = view.findViewById(R.id.rvFreeEbooks);
        rvBestSellers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBestSellers.setAdapter(new BookAdapter(dummyBooks));

        return view;
    }
}