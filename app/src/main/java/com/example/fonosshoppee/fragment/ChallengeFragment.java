package com.example.fonosshoppee.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.MainActivity;
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.data.UserDataStore;
import com.example.fonosshoppee.model.ChallengeItem;

import java.util.ArrayList;
import java.util.List;

public class ChallengeFragment extends Fragment {

    private static final int DAILY_LISTEN_TARGET_MINUTES = 15;
    private static final int MONTHLY_LISTEN_TARGET_MINUTES = 300;
    private static final int MONTHLY_LOGIN_TARGET_DAYS = 15;
    private static final int STREAK_TARGET_DAYS = 3;

    private RecyclerView rvDailyChallenges;
    private RecyclerView rvMonthlyChallenges;
    private ChallengeAdapter dailyAdapter;
    private ChallengeAdapter monthlyAdapter;
    private final List<ChallengeItem> dailyList = new ArrayList<>();
    private final List<ChallengeItem> monthlyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        rvDailyChallenges = view.findViewById(R.id.rvDailyChallenges);
        rvMonthlyChallenges = view.findViewById(R.id.rvMonthlyChallenges);

        dailyAdapter = new ChallengeAdapter(dailyList, this::openAudiobooks);
        monthlyAdapter = new ChallengeAdapter(monthlyList, this::openAudiobooks);

        setupRecyclerView(rvDailyChallenges, dailyAdapter);
        setupRecyclerView(rvMonthlyChallenges, monthlyAdapter);
        loadChallengeData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChallengeData();
    }

    private void setupRecyclerView(RecyclerView recyclerView, ChallengeAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
    }

    private void loadChallengeData() {
        UserDataStore.ChallengeStats stats = UserDataStore.getChallengeStats(requireContext());

        dailyList.clear();
        dailyList.add(new ChallengeItem(
                "✓",
                "Đăng nhập vào Fonos hôm nay",
                stats.loggedInToday ? "1/1" : "0/1",
                "",
                stats.loggedInToday
        ));
        dailyList.add(new ChallengeItem(
                "▶",
                "Nghe Fonos trong 15 phút",
                minuteProgress(stats.dailyListenMs, DAILY_LISTEN_TARGET_MINUTES),
                "Khám phá Sách nói >",
                stats.dailyListenMs >= minutesToMs(DAILY_LISTEN_TARGET_MINUTES)
        ));

        monthlyList.clear();
        monthlyList.add(new ChallengeItem(
                "⏱",
                "Nghe Fonos trong 300 phút",
                minuteProgress(stats.monthlyListenMs, MONTHLY_LISTEN_TARGET_MINUTES),
                "Khám phá Sách nói >",
                stats.monthlyListenMs >= minutesToMs(MONTHLY_LISTEN_TARGET_MINUTES)
        ));
        monthlyList.add(new ChallengeItem(
                "📅",
                "Đăng nhập Fonos 15 ngày trong tháng",
                Math.min(stats.monthlyLoginDays, MONTHLY_LOGIN_TARGET_DAYS) + "/" + MONTHLY_LOGIN_TARGET_DAYS,
                "",
                stats.monthlyLoginDays >= MONTHLY_LOGIN_TARGET_DAYS
        ));
        monthlyList.add(new ChallengeItem(
                "🔥",
                "Đăng nhập Fonos 3 ngày liên tiếp",
                Math.min(stats.loginStreakDays, STREAK_TARGET_DAYS) + "/" + STREAK_TARGET_DAYS,
                "",
                stats.loginStreakDays >= STREAK_TARGET_DAYS
        ));

        dailyAdapter.notifyDataSetChanged();
        monthlyAdapter.notifyDataSetChanged();
    }

    private String minuteProgress(long listenedMs, int targetMinutes) {
        long listenedMinutes = Math.min(targetMinutes, listenedMs / 60000);
        return listenedMinutes + "/" + targetMinutes;
    }

    private long minutesToMs(int minutes) {
        return minutes * 60L * 1000L;
    }

    private void openAudiobooks(ChallengeItem ignored) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchToBooksSubTab(0);
        }
    }

    public static class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {
        private final List<ChallengeItem> items;
        private final OnChallengeActionListener listener;

        public ChallengeAdapter(List<ChallengeItem> items, OnChallengeActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChallengeItem item = items.get(position);
            holder.tvEmoji.setText(item.getEmoji());
            holder.tvTitle.setText(item.getTitle());

            if (item.isCompleted()) {
                holder.tvProgress.setText("✓");
                holder.tvProgress.setTextColor(Color.parseColor("#F25B3E"));
                holder.tvProgress.setTextSize(18);
            } else {
                holder.tvProgress.setText(item.getProgressText());
                holder.tvProgress.setTextColor(Color.parseColor("#6B7280"));
                holder.tvProgress.setTextSize(14);
            }

            if (item.getActionButtonText() != null && !item.getActionButtonText().isEmpty()) {
                holder.tvActionButton.setVisibility(View.VISIBLE);
                holder.tvActionButton.setText(item.getActionButtonText());
                holder.tvActionButton.setOnClickListener(v -> {
                    if (listener != null) listener.onActionClick(item);
                });
            } else {
                holder.tvActionButton.setVisibility(View.GONE);
                holder.tvActionButton.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        interface OnChallengeActionListener {
            void onActionClick(ChallengeItem item);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvProgress, tvActionButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvEmoji);
                tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
                tvProgress = itemView.findViewById(R.id.tvProgress);
                tvActionButton = itemView.findViewById(R.id.tvActionButton);
            }
        }
    }
}
