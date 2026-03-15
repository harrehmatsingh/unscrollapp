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

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    public interface ActionListener {
        void onAction(RequestItem item);
    }

    static class RequestItem {
        final String uid;
        final String username;

        RequestItem(String uid, String username) {
            this.uid = uid;
            this.username = username != null ? username : "User";
        }
    }

    private final List<RequestItem> items = new ArrayList<>();
    private final ActionListener onAccept;
    private final ActionListener onDecline;

    FriendRequestAdapter(ActionListener onAccept, ActionListener onDecline) {
        this.onAccept = onAccept;
        this.onDecline = onDecline;
    }

    void setItems(List<RequestItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestItem item = items.get(position);
        holder.tvUsername.setText(item.username);
        holder.btnAccept.setOnClickListener(v -> onAccept.onAction(item));
        holder.btnDecline.setOnClickListener(v -> onDecline.onAction(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Button btnAccept;
        Button btnDecline;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvRequestUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
