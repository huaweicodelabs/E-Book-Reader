package com.huawei.elibri.kotlin.adapter


/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.service.model.InterestModel


/**
 * Adapter for interests list
 *
 * @author: nWX914751
 * @since: 10-11-2020
 */
class InterestAdapter(
    private val mModelList: List<InterestModel>?,
    private val isProfile: Boolean
) :
    RecyclerView.Adapter<InterestAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View
        if (isProfile) {
            view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_interest, parent, false)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.interest.text = mModelList!![position].userInterests.interest
        holder.interest.isChecked = mModelList[position].isSelected
        holder.interest.setOnClickListener { view: View? ->
            mModelList[position].isSelected = !mModelList[position].isSelected
        }
    }

    /**
     * Get the total number of interest item
     *
     * @return number of interest
     */
    override fun getItemCount(): Int {
        return mModelList?.size ?: 0
    }

    /**
     * View holder to inflate interest list
     */
    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val interest: CheckBox

        init {
            interest = itemView.findViewById(R.id.cb_interest)
        }
    }

}
