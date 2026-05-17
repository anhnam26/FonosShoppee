package com.example.fonosshoppee.Main;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosshoppee.R;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    // Khai báo các view để thay đổi giao diện
    private RecyclerView rvBooks;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle, tvEmptyDesc, btnEmptyAction, tvEmptyTooltip;

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

        // 1. Cài đặt Menu ngang
        RecyclerView rvMenu = view.findViewById(R.id.rvLibraryMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<MenuItem> menuList = new ArrayList<>();
        menuList.add(new MenuItem("🕒", "Gần đây", true));
        menuList.add(new MenuItem("🛍️", "Đã mua", false));
        menuList.add(new MenuItem("⬇️", "Đã tải", false));
        menuList.add(new MenuItem("❤️", "Yêu thích", false));
        menuList.add(new MenuItem("🔖", "Đánh dấu", false));
        menuList.add(new MenuItem("📦", "Lưu trữ", false));

        MenuAdapter menuAdapter = new MenuAdapter(menuList, new OnMenuClickListener() {
            @Override
            public void onMenuClick(int position, String menuName) {
                updateContentArea(menuName);
            }
        });
        rvMenu.setAdapter(menuAdapter);

        // 2. Cài đặt Danh sách Sách (Mặc định cho mục Gần đây)
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        List<LibBookItem> bookList = new ArrayList<>();
        bookList.add(new LibBookItem("Một Đời Quản Trị", "Phan Văn Trường", "#1976D2"));
        bookList.add(new LibBookItem("Tĩnh Lặng", "Thích Nhất Hạnh", "#F57C00"));
        rvBooks.setAdapter(new LibBookAdapter(bookList));

        return view;
    }

    // Hàm điều khiển thay đổi nội dung khi bấm vào menu
    private void updateContentArea(String menuName) {
        // Reset ẩn hết các nút và tooltip đặc biệt
        btnEmptyAction.setVisibility(View.GONE);
        tvEmptyTooltip.setVisibility(View.GONE);

        if (menuName.equals("Gần đây")) {
            rvBooks.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        } else {
            rvBooks.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            switch (menuName) {
                case "Đã mua":
                    tvEmptyTitle.setText("Bạn chưa mua nội dung nào");
                    tvEmptyDesc.setText("Nội dung mua bằng thẻ Fonos sẽ hiển thị ở đây và thuộc về bạn mãi mãi.\n\nBạn nhận được thẻ Fonos khi là Hội viên.");
                    btnEmptyAction.setVisibility(View.VISIBLE); // Hiện nút màu cam
                    break;
                case "Đã tải":
                    tvEmptyTitle.setText("Bạn chưa tải nội dung nào");
                    tvEmptyDesc.setText("Nội dung đã tải sẽ hiển thị ở đây để bạn nghe offline mọi lúc, mọi nơi.");
                    break;
                case "Yêu thích":
                    tvEmptyTitle.setText("Bạn chưa yêu thích nội dung nào");
                    tvEmptyDesc.setText("Bấm biểu tượng trái tim để yêu thích một nội dung và dễ dàng xem lại ở đây.");
                    break;
                case "Đánh dấu":
                    tvEmptyTitle.setText("Bạn chưa có đánh dấu nào");
                    tvEmptyDesc.setText("Khi bắt gặp một đoạn tâm đắc, bạn có thể đánh dấu để nghe lại dễ dàng.");
                    tvEmptyTooltip.setVisibility(View.VISIBLE); // Hiện hình ảnh hướng dẫn
                    break;
                case "Lưu trữ":
                    tvEmptyTitle.setText("Bạn chưa lưu trữ nội dung nào");
                    tvEmptyDesc.setText("Để ẩn một nội dung khỏi mục “Đã mua”, hãy bấm biểu tượng 3 chấm -> chọn “Lưu trữ” và nội dung đó sẽ xuất hiện ở đây.");
                    break;
            }
        }
    }

    // ================= INTERFACE LẮNG NGHE SỰ KIỆN CLICK =================
    public interface OnMenuClickListener {
        void onMenuClick(int position, String menuName);
    }

    // ================= CLASS MODEL & ADAPTER CHO MENU =================
    public static class MenuItem {
        String emoji, name; boolean isSelected;
        public MenuItem(String emoji, String name, boolean isSelected) {
            this.emoji = emoji; this.name = name; this.isSelected = isSelected;
        }
    }

    public static class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        List<MenuItem> list;
        OnMenuClickListener listener;
        int selectedPosition = 0; // Biến lưu vị trí đang được chọn

        public MenuAdapter(List<MenuItem> list, OnMenuClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            MenuItem item = list.get(position);
            holder.tvIcon.setText(item.emoji);
            holder.tvName.setText(item.name);

            if(position == selectedPosition){
                holder.circleBg.setBackgroundResource(R.drawable.bg_circle_progress);
            } else {
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(Color.parseColor("#4A5568"));
                holder.circleBg.setBackground(shape);
            }

            // Bắt sự kiện Click
            holder.itemView.setOnClickListener(v -> {
                int previousPos = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                // Báo cho Adapter biết để vẽ lại màu sắc của vòng tròn
                notifyItemChanged(previousPos);
                notifyItemChanged(selectedPosition);
                // Báo cho Fragment biết để đổi nội dung
                listener.onMenuClick(selectedPosition, item.name);
            });
        }
        @Override public int getItemCount() { return list.size(); }

        static class MenuViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvName; View circleBg;
            public MenuViewHolder(@NonNull View v) {
                super(v);
                tvIcon = v.findViewById(R.id.tvMenuIcon);
                tvName = v.findViewById(R.id.tvMenuName);
                circleBg = v.findViewById(R.id.circleBg);
            }
        }
    }

    // ================= CLASS MODEL & ADAPTER CHO SÁCH =================
    // (Giữ nguyên không thay đổi logic phần này)
    public static class LibBookItem {
        String title, author, colorHex;
        public LibBookItem(String title, String author, String colorHex) {
            this.title = title; this.author = author; this.colorHex = colorHex;
        }
    }

    public static class LibBookAdapter extends RecyclerView.Adapter<LibBookAdapter.BookViewHolder> {
        List<LibBookItem> list;
        public LibBookAdapter(List<LibBookItem> list) { this.list = list; }
        @NonNull @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_book, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            LibBookItem item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvAuthor.setText(item.author);
            holder.vCover.setBackgroundColor(Color.parseColor(item.colorHex));
        }
        @Override public int getItemCount() { return list.size(); }
        static class BookViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor; View vCover;
            public BookViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvBookTitle);
                tvAuthor = v.findViewById(R.id.tvBookAuthor);
                vCover = v.findViewById(R.id.vCoverColor);
            }
        }
    }
}