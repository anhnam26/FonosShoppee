package com.example.fonosshoppee.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosshoppee.BookDetailActivity;
import com.example.fonosshoppee.R;
import com.example.fonosshoppee.data.UserDataStore;
import com.example.fonosshoppee.model.LibBookItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private static final String TAB_RECENT = "Gần đây";
    private static final String TAB_DOWNLOADED = "Đã tải";

    private RecyclerView rvBooks;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle, tvEmptyDesc, btnEmptyAction, tvEmptyTooltip;
    private LibBookAdapter bookAdapter;
    private final List<LibBookItem> bookList = new ArrayList<>();
    private boolean showingDownloaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvBooks = view.findViewById(R.id.rvLibraryBooks);
        layoutEmpty = view.findViewById(R.id.layoutEmptyState);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc = view.findViewById(R.id.tvEmptyDesc);
        btnEmptyAction = view.findViewById(R.id.btnEmptyAction);
        tvEmptyTooltip = view.findViewById(R.id.tvEmptyTooltip);

        setupMenu(view);
        setupBookList();
        updateContentArea(TAB_RECENT);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
    }

    private void setupMenu(View view) {
        RecyclerView rvMenu = view.findViewById(R.id.rvLibraryMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<MenuItem> menuList = new ArrayList<>();
        menuList.add(new MenuItem("◷", TAB_RECENT));
        menuList.add(new MenuItem("↓", TAB_DOWNLOADED));

        MenuAdapter menuAdapter = new MenuAdapter(menuList, (position, menuName) -> updateContentArea(menuName));
        rvMenu.setAdapter(menuAdapter);
    }

    private void setupBookList() {
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        bookAdapter = new LibBookAdapter(bookList, new LibBookAdapter.OnLibraryBookListener() {
            @Override
            public void onBookClick(LibBookItem item) {
                openBook(item);
            }

            @Override
            public boolean onBookLongClick(View anchor, LibBookItem item, int position) {
                if (!showingDownloaded) return false;
                showDownloadedOptions(anchor, item);
                return true;
            }
        });
        rvBooks.setAdapter(bookAdapter);
    }

    private void updateContentArea(String menuName) {
        showingDownloaded = TAB_DOWNLOADED.equals(menuName);
        bookAdapter.setDownloadedMode(showingDownloaded);
        btnEmptyAction.setVisibility(View.GONE);
        tvEmptyTooltip.setVisibility(View.GONE);
        loadBooks();
    }

    private void loadBooks() {
        if (getContext() == null || bookAdapter == null) return;

        bookList.clear();
        if (showingDownloaded) {
            bookList.addAll(UserDataStore.getDownloadedBooks(requireContext()));
            showEmptyIfNeeded(
                    "Bạn chưa tải nội dung nào",
                    "Nội dung đã tải sẽ hiển thị ở đây để bạn nghe offline mọi lúc, mọi nơi."
            );
        } else {
            bookList.addAll(UserDataStore.getRecentBooks(requireContext()));
            showEmptyIfNeeded(
                    "Bạn chưa nghe nội dung nào",
                    "Các sách bạn đã nghe sẽ xuất hiện ở đây cùng vị trí nghe gần nhất."
            );
        }
        bookAdapter.notifyDataSetChanged();
    }

    private void showEmptyIfNeeded(String title, String desc) {
        boolean empty = bookList.isEmpty();
        rvBooks.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        tvEmptyTitle.setText(title);
        tvEmptyDesc.setText(desc);
    }

    private void openBook(LibBookItem item) {
        String playbackUrl = showingDownloaded && item.getLocalAudioPath() != null && !item.getLocalAudioPath().isEmpty()
                ? item.getLocalAudioPath()
                : item.getAudioUrl();

        if (playbackUrl == null || playbackUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Sách này chưa có audio", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), BookDetailActivity.class);
        intent.putExtra("BOOK_TITLE", item.getTitle());
        intent.putExtra("BOOK_AUTHOR", item.getAuthor());
        intent.putExtra("BOOK_COVER", item.getCoverUrl());
        intent.putExtra("AUDIO_URL", playbackUrl);
        intent.putExtra("START_POSITION", item.getPositionMs());
        intent.putExtra("AUTO_PLAY", true);
        startActivity(intent);
    }

    private void showDownloadedOptions(View anchor, LibBookItem item) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add("Xóa khỏi máy");
        popupMenu.getMenu().add("Chia sẻ");
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            String action = menuItem.getTitle().toString();
            if (action.startsWith("Xóa")) {
                UserDataStore.removeDownloadedBook(requireContext(), item, true);
                Toast.makeText(requireContext(), "Đã xóa sách khỏi máy", Toast.LENGTH_SHORT).show();
                loadBooks();
            } else {
                shareDownloadedBook(item);
            }
            return true;
        });
        popupMenu.show();
    }

    private void shareDownloadedBook(LibBookItem item) {
        String localPath = item.getLocalAudioPath();
        if (localPath == null || localPath.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy file đã tải", Toast.LENGTH_SHORT).show();
            return;
        }

        File audioFile = new File(localPath);
        if (!audioFile.exists()) {
            Toast.makeText(requireContext(), "File đã tải không còn trên máy", Toast.LENGTH_SHORT).show();
            loadBooks();
            return;
        }

        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                audioFile
        );
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/mpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Chia sẻ sách nói: " + item.getTitle());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ sách qua:"));
    }

    public interface OnMenuClickListener {
        void onMenuClick(int position, String menuName);
    }

    public static class MenuItem {
        String emoji, name;

        public MenuItem(String emoji, String name) {
            this.emoji = emoji;
            this.name = name;
        }
    }

    public static class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        private final List<MenuItem> list;
        private final OnMenuClickListener listener;
        private int selectedPosition = 0;

        public MenuAdapter(List<MenuItem> list, OnMenuClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            MenuItem item = list.get(position);
            holder.tvIcon.setText(item.emoji);
            holder.tvName.setText(item.name);

            if (position == selectedPosition) {
                holder.circleBg.setBackgroundResource(R.drawable.bg_circle_progress);
            } else {
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(Color.parseColor("#4A5568"));
                holder.circleBg.setBackground(shape);
            }

            holder.itemView.setOnClickListener(v -> {
                int previousPos = selectedPosition;
                selectedPosition = holder.getBindingAdapterPosition();
                if (selectedPosition == RecyclerView.NO_POSITION) return;
                notifyItemChanged(previousPos);
                notifyItemChanged(selectedPosition);
                listener.onMenuClick(selectedPosition, item.name);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class MenuViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvName;
            View circleBg;

            public MenuViewHolder(@NonNull View v) {
                super(v);
                tvIcon = v.findViewById(R.id.tvMenuIcon);
                tvName = v.findViewById(R.id.tvMenuName);
                circleBg = v.findViewById(R.id.circleBg);
            }
        }
    }

    public static class LibBookAdapter extends RecyclerView.Adapter<LibBookAdapter.BookViewHolder> {
        private final List<LibBookItem> list;
        private final OnLibraryBookListener listener;
        private boolean downloadedMode = false;

        public LibBookAdapter(List<LibBookItem> list, OnLibraryBookListener listener) {
            this.list = list;
            this.listener = listener;
        }

        public void setDownloadedMode(boolean downloadedMode) {
            this.downloadedMode = downloadedMode;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_book, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            LibBookItem item = list.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvAuthor.setText(item.getAuthor());

            if (item.getProgressText() != null && !item.getProgressText().isEmpty()) {
                holder.tvProgress.setVisibility(View.VISIBLE);
                holder.tvProgress.setText(item.getProgressText());
            } else {
                holder.tvProgress.setVisibility(View.GONE);
            }

            if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(item.getCoverUrl()).into(holder.ivCover);
            } else {
                try {
                    holder.ivCover.setBackgroundColor(Color.parseColor(item.getColorHex()));
                } catch (Exception e) {
                    holder.ivCover.setBackgroundColor(Color.GRAY);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBookClick(item);
            });

            holder.itemView.setOnLongClickListener(v -> {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (!downloadedMode || listener == null || adapterPosition == RecyclerView.NO_POSITION) return false;
                return listener.onBookLongClick(v, item, adapterPosition);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public interface OnLibraryBookListener {
            void onBookClick(LibBookItem item);
            boolean onBookLongClick(View anchor, LibBookItem item, int position);
        }

        static class BookViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor, tvProgress;
            ImageView ivCover;

            public BookViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvBookTitle);
                tvAuthor = v.findViewById(R.id.tvBookAuthor);
                tvProgress = v.findViewById(R.id.tvBookProgress);
                ivCover = v.findViewById(R.id.ivBookCover);
            }
        }
    }
}
