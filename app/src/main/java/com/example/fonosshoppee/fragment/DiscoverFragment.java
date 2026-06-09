package com.example.fonosshoppee.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.example.fonosshoppee.R;
import com.example.fonosshoppee.MainActivity;
import com.example.fonosshoppee.model.DiscoverItem;
import com.example.fonosshoppee.ContentListActivity;
import com.example.fonosshoppee.LeaderboardActivity;
import com.example.fonosshoppee.InviteFriendsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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

        // ĐÃ XÓA LOGIC CŨ Ở ĐÂY VÌ ĐÃ CÓ AVATAR GLOBAL Ở MAINACTIVITY

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

    // ================= CLASS ADAPTER =================
    public static class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.ViewHolder> {
        List<DiscoverItem> items;
        public DiscoverAdapter(List<DiscoverItem> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DiscoverItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());

            ViewGroup.LayoutParams layoutParams = holder.cardContainer.getLayoutParams();
            float scale = holder.itemView.getContext().getResources().getDisplayMetrics().density;

            if (item.getTitle().equals("Gợi Ý Nhanh")) {
                holder.cardBackground.setBackgroundColor(Color.TRANSPARENT);
                holder.cardBackground.setImageDrawable(null);
                holder.tvTitle.setTextColor(Color.parseColor("#1B263B"));
                holder.tvTitle.setTextSize(20);
                holder.tvTitle.setPadding((int)(4 * scale), (int)(24 * scale), (int)(16 * scale), (int)(8 * scale));
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                holder.tvTitle.setTextColor(Color.WHITE);
                holder.tvTitle.setTextSize(16);
                int p16 = (int)(16 * scale);
                holder.tvTitle.setPadding(p16, p16, p16, p16);
                layoutParams.height = (int) (item.getHeightDp() * scale + 0.5f);

                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    Glide.with(holder.itemView.getContext()).load(item.getImageUrl()).into(holder.cardBackground);
                } else {
                    holder.cardBackground.setImageDrawable(null);
                    try {
                        holder.cardBackground.setBackgroundColor(Color.parseColor(item.getColorCode()));
                    } catch (Exception e) {
                        holder.cardBackground.setBackgroundColor(Color.parseColor("#4A4A4A"));
                    }
                }
            }
            holder.cardContainer.setLayoutParams(layoutParams);

            holder.cardContainer.setOnClickListener(v -> {
                String title = item.getTitle() != null ? item.getTitle() : "";

                if (title.contains("Truyện Ngủ") || title.contains("Truyện ngủ & Nhạc")) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
                    bottomSheetDialog.setContentView(R.layout.dialog_choose_content);

                    View btnChooseSleepStory = bottomSheetDialog.findViewById(R.id.btnChooseSleepStory);
                    View btnChooseRelaxMusic = bottomSheetDialog.findViewById(R.id.btnChooseRelaxMusic);

                    if (btnChooseSleepStory != null) {
                        btnChooseSleepStory.setOnClickListener(v1 -> {
                            bottomSheetDialog.dismiss();
                            Intent intent = new Intent(holder.itemView.getContext(), ContentListActivity.class);
                            intent.putExtra("CONTENT_TYPE", "SLEEP_STORY");
                            holder.itemView.getContext().startActivity(intent);
                        });
                    }
                    if (btnChooseRelaxMusic != null) {
                        btnChooseRelaxMusic.setOnClickListener(v1 -> {
                            bottomSheetDialog.dismiss();
                            Intent intent = new Intent(holder.itemView.getContext(), ContentListActivity.class);
                            intent.putExtra("CONTENT_TYPE", "RELAX_MUSIC");
                            holder.itemView.getContext().startActivity(intent);
                        });
                    }
                    bottomSheetDialog.show();
                    return;
                }

                if (title.contains("Bảng Xếp Hạng") || title.contains("Bảng xếp hạng") || title.contains("Bảng Xếp hạng")) {
                    Intent intent = new Intent(holder.itemView.getContext(), LeaderboardActivity.class);
                    holder.itemView.getContext().startActivity(intent);
                    return;
                }

                if (title.contains("Ebook") || title.contains("Sách Nói") || title.contains("Tóm Tắt Sách") || title.contains("Tóm tắt sách") || title.contains("Sách nói")) {
                    if (holder.itemView.getContext() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) holder.itemView.getContext();

                        if (title.contains("Sách Nói") || title.contains("Sách nói")) {
                            mainActivity.switchToBooksSubTab(0);
                        } else if (title.contains("Ebook")) {
                            mainActivity.switchToBooksSubTab(1);
                        } else if (title.contains("Tóm Tắt Sách") || title.contains("Tóm tắt sách")) {
                            mainActivity.switchToBooksSubTab(2);
                        }
                    }
                    return;
                }

                if (title.contains("Rủ Bạn") || title.contains("Nhập Hội") || title.contains("Tặng Quà")) {
                    Intent intent = new Intent(holder.itemView.getContext(), InviteFriendsActivity.class);
                    holder.itemView.getContext().startActivity(intent);
                    return;
                }

                if (title.toLowerCase().contains("podcourse")) {
                    if (holder.itemView.getContext() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) holder.itemView.getContext();
                        BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottom_navigation);
                        if (bottomNav != null) {
                            bottomNav.setSelectedItemId(R.id.nav_podcourse);
                        }
                    }
                    return;
                }

                Intent intent = new Intent(holder.itemView.getContext(), ContentListActivity.class);

                if (title.contains("Mới Ra Mắt") || title.contains("mới ra mắt")) {
                    intent.putExtra("CONTENT_TYPE", "NEW_RELEASES");
                } else if (title.contains("Podcast")) {
                    intent.putExtra("CONTENT_TYPE", "PODCAST");
                } else if (title.contains("Thiền")) {
                    intent.putExtra("CONTENT_TYPE", "MEDITATION");
                } else if (title.contains("Tiếng Anh")) {
                    intent.putExtra("CONTENT_TYPE", "ENGLISH_BOOKS");
                } else if (title.contains("Miễn Phí")) {
                    intent.putExtra("CONTENT_TYPE", "FREE_BOOKS");
                } else if (title.contains("Thiếu Nhi")) {
                    intent.putExtra("CONTENT_TYPE", "KIDS");
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Đang cập nhật chuyên mục: " + title, Toast.LENGTH_SHORT).show();
                    return;
                }

                holder.itemView.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            CardView cardContainer;
            ImageView cardBackground;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvCardTitle);
                cardContainer = itemView.findViewById(R.id.cardContainer);
                cardBackground = itemView.findViewById(R.id.cardBackground);
            }
        }
    }
}