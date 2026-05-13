package com.example.fonosshoppee; // Đổi lại đúng package của bạn

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Hiển thị Fragment Sách làm mặc định khi vừa mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BooksFragment()).commit();
        }

        // Xử lý sự kiện click trên thanh điều hướng
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                // Dùng if-else thay vì switch-case để tránh lỗi Constant Expression trong các phiên bản Android Studio mới
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

                // Thực hiện chuyển đổi màn hình (Fragment)
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
        });
    }
}