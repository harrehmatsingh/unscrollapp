package com.example.unscrollapplication.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unscrollapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UsernameSetupActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnSave;
    private TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_setup);

        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSaveUsername);
        tvHint = findViewById(R.id.tvUsernameHint);

        btnSave.setOnClickListener(v -> saveUsername());
    }

    private void saveUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username) || username.length() < 3) {
            tvHint.setText("Username must be at least 3 characters.");
            return;
        }

        String lower = username.toLowerCase();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("usernameLower", lower)
                .get()
                .addOnSuccessListener(this::onUsernameQuery)
                .addOnFailureListener(e -> tvHint.setText("Could not check username."));
    }

    private void onUsernameQuery(QuerySnapshot snapshot) {
        if (snapshot != null && !snapshot.isEmpty()) {
            tvHint.setText("That username is taken.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String username = etUsername.getText().toString().trim();
        String lower = username.toLowerCase();

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("usernameLower", lower);
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("uid", user.getUid());
        data.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Username saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> tvHint.setText("Could not save username."));
    }
}
