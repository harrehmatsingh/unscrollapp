// AppAdapter.java
package com.example.unscrollapplication.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unscrollapplication.R;
import com.example.unscrollapplication.domain.AppInfo;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<AppInfo> appList;

    public AppAdapter(List<AppInfo> appList) {
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppViewHolder holder, int position) {
        final AppInfo appInfo = appList.get(position);
        holder.tvAppName.setText(appInfo.appName);
        holder.cbSelected.setOnCheckedChangeListener(null);
        holder.cbSelected.setChecked(appInfo.selected);

        holder.cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appInfo.selected = isChecked;
        });

        holder.itemView.setOnClickListener(v -> {
            boolean newChecked = !holder.cbSelected.isChecked();
            holder.cbSelected.setChecked(newChecked);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        CheckBox cbSelected;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            cbSelected = itemView.findViewById(R.id.cbSelected);
        }
    }

    public List<AppInfo> getAppList() {
        return appList;
    }
}
