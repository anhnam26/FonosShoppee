package com.example.fonosshoppee;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.fragment.ChallengeFragment;
import com.example.fonosshoppee.fragment.DiscoverFragment;
import com.example.fonosshoppee.fragment.LibraryFragment;
import com.example.fonosshoppee.fragment.PodCourseFragment;
import com.example.fonosshoppee.fragment.BooksFragment;
import com.example.fonosshoppee.data.UserDataStore;
import com.example.fonosshoppee.service.AudioPlayerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VONG_DOI_MAIN";

    private CardView miniPlayerLayout;
    private ImageView miniPlayerCover;
    private ImageView miniPlayerBtnPlay;
    private TextView miniPlayerTitle, miniPlayerAuthor;

    // Khai báo Avatar toàn cục
    private de.hdodenhof.circleimageview.CircleImageView ivGlobalAvatar;

    private AudioPlayerService audioService;
    private boolean isBound = false;

    private Handler handler = new Handler();
    private Runnable updateMiniPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            updateMiniPlayerUI();
            handler.postDelayed(this, 1000);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioService = binder.getService();
            isBound = true;
            handler.post(updateMiniPlayerRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private final NavigationBarView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
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

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");
        UserDataStore.recordAppOpened(this);

        miniPlayerLayout = findViewById(R.id.miniPlayerLayout);
        miniPlayerCover = findViewById(R.id.miniPlayerCover);
        miniPlayerBtnPlay = findViewById(R.id.miniPlayerBtnPlay);
        miniPlayerTitle = findViewById(R.id.miniPlayerTitle);
        miniPlayerAuthor = findViewById(R.id.miniPlayerAuthor);

        ivGlobalAvatar = findViewById(R.id.ivGlobalAvatar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BooksFragment()).commit();
        }

        bottomNav.setOnItemSelectedListener(navListener);

        miniPlayerBtnPlay.setOnClickListener(v -> {
            if (isBound && audioService != null) {
                if (audioService.isPlaying()) audioService.pauseAudio();
                else audioService.resumeAudio();
                updateMiniPlayerUI();
            }
        });

        miniPlayerLayout.setOnClickListener(v -> {
            if (isBound && audioService != null) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("BOOK_TITLE", audioService.getCurrentBookTitle());
                intent.putExtra("BOOK_AUTHOR", audioService.getCurrentBookAuthor());
                intent.putExtra("BOOK_COVER", audioService.getCurrentBookCover());
                intent.putExtra("AUDIO_URL", audioService.getCurrentAudioUrl());
                startActivity(intent);
            }
        });

        // Bắt sự kiện Click vào Avatar nổi
        ivGlobalAvatar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        });
    }

    public void switchToBooksSubTab(int subTabPosition) {
        BooksFragment booksFragment = new BooksFragment();
        Bundle args = new Bundle();
        args.putInt("SUB_TAB_POSITION", subTabPosition);
        booksFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, booksFragment)
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(null);
            bottomNav.setSelectedItemId(R.id.nav_books);
            bottomNav.setOnItemSelectedListener(navListener);
        }
    }

    private void updateMiniPlayerUI() {
        if (isBound && audioService != null) {
            String currentUrl = audioService.getCurrentAudioUrl();
            if (currentUrl != null && !currentUrl.isEmpty()) {
                miniPlayerLayout.setVisibility(View.VISIBLE);
                miniPlayerTitle.setText(audioService.getCurrentBookTitle() != null ? audioService.getCurrentBookTitle() : "Đang phát");
                miniPlayerAuthor.setText(audioService.getCurrentBookAuthor() != null ? audioService.getCurrentBookAuthor() : "");
                String coverUrl = audioService.getCurrentBookCover();
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    Glide.with(this).load(coverUrl).into(miniPlayerCover);
                }
                miniPlayerBtnPlay.setImageResource(audioService.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            } else {
                miniPlayerLayout.setVisibility(View.GONE);
            }
        }
    }

    // Hàm cập nhật Avatar
    private void updateGlobalAvatar() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null && ivGlobalAvatar != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(ivGlobalAvatar);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserDataStore.recordAppOpened(this);
        updateMiniPlayerUI();
        updateGlobalAvatar(); // Gọi cập nhật Avatar mỗi khi quay lại màn hình chính
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            handler.removeCallbacks(updateMiniPlayerRunnable);
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
