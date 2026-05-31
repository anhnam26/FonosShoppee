package com.example.fonosshoppee.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fonosshoppee.R;
import com.example.fonosshoppee.adapter.BooksPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BooksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        //adapter ViewPager2
        BooksPagerAdapter pagerAdapter = new BooksPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        //TabLayout link ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Sách nói");
                        break;
                    case 1:
                        tab.setText("Ebook");
                        break;
                    case 2:
                        tab.setText("Tóm tắt sách");
                        break;
                }
            }
        }).attach();

        return view;
    }
}