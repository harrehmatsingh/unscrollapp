package com.example.unscrollapplication.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;

import java.util.ArrayList;
import java.util.List;

public class TopRanksAdapter extends RecyclerView.Adapter<TopRanksAdapter.ViewHolder> {

    static class TopRankItem {
        final String uid;
        final String username;
        final long dailyScore;

        TopRankItem(String uid, String username, long dailyScore) {
            this.uid = uid;
            this.username = username;
            this.dailyScore = dailyScore;
        }
    }

    private final List<TopRankItem> items = new ArrayList<>();
    private String selfUid;

    void setItems(List<TopRankItem> newItems, String selfUid) {
        items.clear();
        items.addAll(newItems);
        this.selfUid = selfUid;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_rank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopRankItem item = items.get(position);
        int rank = position + 1;
        holder.tvRank.setText("#" + rank);
        holder.tvUsername.setText(item.username);
        holder.tvScore.setText(String.valueOf(item.dailyScore));

        if (item.uid != null && item.uid.equals(selfUid)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_list_item_highlight);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_list_item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvUsername;
        TextView tvScore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
