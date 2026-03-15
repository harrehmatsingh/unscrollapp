package com.example.unscrollapplication.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

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

public class FriendsActivity extends AppCompatActivity {

    private EditText etSearchUsername;
    private Button btnSendRequest;
    private ProgressBar pbLoading;
    private TextView tvRequestsEmpty;
    private TextView tvFriendsEmpty;
    private Button btnFindFriends;

    private RecyclerView rvRequests;
    private RecyclerView rvFriends;
    private FriendRequestAdapter requestAdapter;
    private FriendListAdapter friendsAdapter;
    private String selfUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        etSearchUsername = findViewById(R.id.etSearchUsername);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        pbLoading = findViewById(R.id.pbFriendsLoading);
        tvRequestsEmpty = findViewById(R.id.tvRequestsEmpty);
        tvFriendsEmpty = findViewById(R.id.tvFriendsEmpty);
        btnFindFriends = findViewById(R.id.btnFindFriends);

        rvRequests = findViewById(R.id.rvRequests);
        rvFriends = findViewById(R.id.rvFriends);

        requestAdapter = new FriendRequestAdapter(
                this::acceptRequest,
                this::declineRequest
        );
        friendsAdapter = new FriendListAdapter();

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);

        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(friendsAdapter);

        btnSendRequest.setOnClickListener(v -> sendRequest());
        btnFindFriends.setOnClickListener(v -> {
            Intent intent = new Intent(FriendsActivity.this, FindFriendsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSelfUsername();
        loadRequestsAndFriends();
    }

    private void sendRequest() {
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

        db.collection("users")
                .whereEqualTo("usernameLower", lower)
                .get()
                .addOnSuccessListener(snapshot -> {
                    pbLoading.setVisibility(View.GONE);
                    if (snapshot == null || snapshot.isEmpty()) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String targetUid = null;
                    String targetUsername = null;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        targetUid = doc.getId();
                        targetUsername = doc.getString("username");
                        break;
                    }

                    if (targetUid == null) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (targetUid.equals(user.getUid())) {
                        Toast.makeText(this, "That's you", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createFriendRequest(db, user, targetUid, targetUsername);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not search", Toast.LENGTH_SHORT).show();
                });
    }

    private void createFriendRequest(FirebaseFirestore db, FirebaseUser user, String targetUid, String targetUsername) {
        Map<String, Object> incoming = new HashMap<>();
        incoming.put("uid", user.getUid());
        incoming.put("username", selfUsername != null ? selfUsername
                : (user.getDisplayName() != null ? user.getDisplayName() : "User"));
        incoming.put("createdAt", System.currentTimeMillis());

        Map<String, Object> outgoing = new HashMap<>();
        outgoing.put("uid", targetUid);
        outgoing.put("username", targetUsername != null ? targetUsername : "User");
        outgoing.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(targetUid)
                .collection("requests_in")
                .document(user.getUid())
                .set(incoming);

        db.collection("users").document(user.getUid())
                .collection("requests_out")
                .document(targetUid)
                .set(outgoing)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Could not send request", Toast.LENGTH_SHORT).show());
    }

    private void loadRequestsAndFriends() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("requests_in")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<FriendRequestAdapter.RequestItem> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getString("uid");
                        String username = doc.getString("username");
                        if (uid != null) {
                            requests.add(new FriendRequestAdapter.RequestItem(uid, username));
                        }
                    }
                    requestAdapter.setItems(requests);
                    tvRequestsEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                    loadFriends(db, user.getUid());
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    tvRequestsEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void loadFriends(FirebaseFirestore db, String uid) {
        db.collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<FriendListAdapter.FriendItem> friends = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String fuid = doc.getString("uid");
                        String username = doc.getString("username");
                        if (fuid != null) {
                            friends.add(new FriendListAdapter.FriendItem(fuid, username));
                        }
                    }
                    friendsAdapter.setItems(friends);
                    tvFriendsEmpty.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
                    rvFriends.setVisibility(friends.isEmpty() ? View.GONE : View.VISIBLE);
                    btnFindFriends.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
                    pbLoading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    tvFriendsEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void acceptRequest(FriendRequestAdapter.RequestItem item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String selfUid = user.getUid();

        Map<String, Object> selfFriend = new HashMap<>();
        selfFriend.put("uid", item.uid);
        selfFriend.put("username", item.username);
        selfFriend.put("createdAt", System.currentTimeMillis());

        Map<String, Object> otherFriend = new HashMap<>();
        otherFriend.put("uid", selfUid);
        otherFriend.put("username", selfUsername != null ? selfUsername
                : (user.getDisplayName() != null ? user.getDisplayName() : "User"));
        otherFriend.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(selfUid)
                .collection("friends").document(item.uid)
                .set(selfFriend);

        db.collection("users").document(item.uid)
                .collection("friends").document(selfUid)
                .set(otherFriend);

        db.collection("users").document(selfUid)
                .collection("requests_in").document(item.uid)
                .delete();

        db.collection("users").document(item.uid)
                .collection("requests_out").document(selfUid)
                .delete();

        Toast.makeText(this, "Friend added", Toast.LENGTH_SHORT).show();
        loadRequestsAndFriends();
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

    private void declineRequest(FriendRequestAdapter.RequestItem item) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String selfUid = user.getUid();

        db.collection("users").document(selfUid)
                .collection("requests_in").document(item.uid)
                .delete();

        db.collection("users").document(item.uid)
                .collection("requests_out").document(selfUid)
                .delete();

        Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show();
        loadRequestsAndFriends();
    }
}
