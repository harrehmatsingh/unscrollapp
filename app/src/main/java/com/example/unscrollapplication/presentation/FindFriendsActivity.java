package com.example.unscrollapplication.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindFriendsActivity extends AppCompatActivity {

    private EditText etSearchUsername;
    private Button btnSearch;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private RecyclerView rvResults;
    private FindFriendsAdapter adapter;
    private String selfUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        etSearchUsername = findViewById(R.id.etSearchUsername);
        btnSearch = findViewById(R.id.btnSearch);
        pbLoading = findViewById(R.id.pbFindLoading);
        tvEmpty = findViewById(R.id.tvFindEmpty);
        rvResults = findViewById(R.id.rvFindResults);

        adapter = new FindFriendsAdapter(this::sendRequest);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> search());
        loadSelfUsername();
    }

    private void search() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        String input = etSearchUsername.getText() == null ? "" : etSearchUsername.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        String lower = input.toLowerCase();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        pbLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("usernameLower", lower)
                .get()
                .addOnSuccessListener(snapshot -> {
                    pbLoading.setVisibility(View.GONE);
                    List<FindFriendsAdapter.SearchItem> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getId();
                        String username = doc.getString("username");
                        if (uid != null) {
                            results.add(new FindFriendsAdapter.SearchItem(uid, username));
                        }
                    }
                    adapter.setItems(results);
                    tvEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void sendRequest(FindFriendsAdapter.SearchItem item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        if (item.uid.equals(user.getUid())) {
            Toast.makeText(this, "That's you", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> incoming = new HashMap<>();
        incoming.put("uid", user.getUid());
        incoming.put("username", selfUsername != null ? selfUsername
                : (user.getDisplayName() != null ? user.getDisplayName() : "User"));
        incoming.put("createdAt", System.currentTimeMillis());

        Map<String, Object> outgoing = new HashMap<>();
        outgoing.put("uid", item.uid);
        outgoing.put("username", item.username != null ? item.username : "User");
        outgoing.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(item.uid)
                .collection("requests_in")
                .document(user.getUid())
                .set(incoming);

        db.collection("users").document(user.getUid())
                .collection("requests_out")
                .document(item.uid)
                .set(outgoing)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Could not send request", Toast.LENGTH_SHORT).show());
    }

    private void loadSelfUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> selfUsername = doc.getString("username"));
    }
}
