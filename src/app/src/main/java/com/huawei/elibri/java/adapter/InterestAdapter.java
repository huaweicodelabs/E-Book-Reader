/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huawei.elibri.R;
import com.huawei.elibri.java.service.model.InterestModel;
import java.util.List;

/**
 * Adapter for interests list
 *
 * @author: nWX914751
 * @since: 10-11-2020
 */
public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.MyViewHolder> {
    private List<InterestModel> mModelList;
    private boolean isProfile;

    public InterestAdapter(List<InterestModel> interest, boolean isProfile) {
        mModelList = interest;
        this.isProfile = isProfile;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (isProfile) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_interest, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.interest.setText(mModelList.get(position).getUserInterests().getInterest());
        holder.interest.setChecked(mModelList.get(position).isSelected());
        holder.interest.setOnClickListener(
                view -> mModelList.get(position).setSelected(!mModelList.get(position).isSelected()));
    }

    /**
     * Get the total number of interest item
     *
     * @return number of interest
     */
    @Override
    public int getItemCount() {
        return mModelList == null ? 0 : mModelList.size();
    }

    /**
     * View holder to inflate interest list
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox interest;

        private MyViewHolder(View itemView) {
            super(itemView);
            interest = itemView.findViewById(R.id.cb_interest);
        }
    }
}
