package com.example.unscrollapplication.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {

    public interface AddListener {
        void onAdd(SearchItem item);
    }

    static class SearchItem {
        final String uid;
        final String username;

        SearchItem(String uid, String username) {
            this.uid = uid;
            this.username = username != null ? username : "User";
        }
    }

    private final List<SearchItem> items = new ArrayList<>();
    private final AddListener listener;

    FindFriendsAdapter(AddListener listener) {
        this.listener = listener;
    }

    void setItems(List<SearchItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_find_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem item = items.get(position);
        holder.tvUsername.setText(item.username);
        holder.btnAdd.setOnClickListener(v -> listener.onAdd(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Button btnAdd;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvFindUsername);
            btnAdd = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}
