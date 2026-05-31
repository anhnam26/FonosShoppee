package com.example.fonosshoppee;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fonosshoppee.fragment.ChallengeFragment;
import com.example.fonosshoppee.fragment.DiscoverFragment;
import com.example.fonosshoppee.fragment.LibraryFragment;
import com.example.fonosshoppee.fragment.PodCourseFragment;
import com.example.fonosshoppee.fragment.BooksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

// Import thêm cho Firebase và Model Sách
import com.example.fonosshoppee.model.BookItem;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Fragment Sách mặc định khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BooksFragment()).commit();
        }

        // Lắng nghe sự kiện click trên Bottom Navigation
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_books) {
                    selectedFragment = new BooksFragment();
                } else if (itemId == R.id.nav_podcourse) {
                    selectedFragment = new PodCourseFragment();
                } else if (itemId == R.id.nav_discover) {
                    selectedFragment = new DiscoverFragment();
                } else if (itemId == R.id.nav_challenge) {
                    selectedFragment = new ChallengeFragment();
                } else if (itemId == R.id.nav_library) {
                    selectedFragment = new LibraryFragment();
                }

                // Chuyển đổi Fragment tương ứng
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
        });


    }



}