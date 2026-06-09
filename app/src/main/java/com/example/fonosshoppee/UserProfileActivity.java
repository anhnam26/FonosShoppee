package com.example.fonosshoppee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView btnProfileBack;
    private CircleImageView ivProfileAvatar;
    private EditText etProfileName, etProfileEmail, etProfilePassword;
    private Button btnSaveProfile, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Uri imageUri; // Lưu đường dẫn ảnh được chọn từ máy
    private ProgressDialog progressDialog;

    // Bộ lắng nghe kết quả khi người dùng chọn ảnh từ Gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivProfileAvatar.setImageURI(imageUri); // Hiển thị ảnh tạm lên màn hình
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ánh xạ View
        btnProfileBack = findViewById(R.id.btnProfileBack);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePassword = findViewById(R.id.etProfilePassword);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật...");

        btnProfileBack.setOnClickListener(v -> finish());

        // HIỂN THỊ DỮ LIỆU CŨ CỦA NGƯỜI DÙNG
        if (currentUser != null) {
            etProfileEmail.setText(currentUser.getEmail());
            if (currentUser.getDisplayName() != null) {
                etProfileName.setText(currentUser.getDisplayName());
            }
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).into(ivProfileAvatar);
            }
        }

        // SỰ KIỆN CLICK VÀO ẢNH ĐỂ ĐỔI AVATAR
        ivProfileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // SỰ KIỆN LƯU THAY ĐỔI
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        // SỰ KIỆN ĐĂNG XUẤT
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa lịch sử trang để ko bấm back lại được
            startActivity(intent);
            finish();
        });
    }

    private void saveUserProfile() {
        if (currentUser == null) return;
        progressDialog.show();

        String newName = etProfileName.getText().toString().trim();
        String newEmail = etProfileEmail.getText().toString().trim();
        String newPassword = etProfilePassword.getText().toString().trim();

        // CHỈ CẬP NHẬT TÊN (Bỏ qua phần upload ảnh lên Storage để tránh lỗi bắt nạp thẻ)
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật Email
                if (!newEmail.isEmpty() && !newEmail.equals(currentUser.getEmail())) {
                    currentUser.updateEmail(newEmail);
                }
                // Cập nhật Password (nếu có nhập)
                if (!newPassword.isEmpty()) {
                    currentUser.updatePassword(newPassword);
                }

                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Đã cập nhật hồ sơ!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại trang trước
            } else {
                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAuthProfile(String name, Uri photoUri, String email, String password) {
        // Cập nhật Tên và Avatar
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật Email
                if (!email.isEmpty() && !email.equals(currentUser.getEmail())) {
                    currentUser.updateEmail(email);
                }
                // Cập nhật Password (nếu có nhập)
                if (!password.isEmpty()) {
                    currentUser.updatePassword(password);
                }

                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Đã cập nhật hồ sơ!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại trang trước
            } else {
                progressDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}