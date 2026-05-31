package com.example.fonosshoppee.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fonosshoppee.fragment.AudiobooksFragment;
import com.example.fonosshoppee.fragment.EbooksFragment;
import com.example.fonosshoppee.fragment.SummariesFragment;

public class BooksPagerAdapter extends FragmentStateAdapter {

    public BooksPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AudiobooksFragment();
            case 1:
                return new EbooksFragment();
            case 2:
                return new SummariesFragment();
            default:
                return new AudiobooksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Có 3 tab
    }
}