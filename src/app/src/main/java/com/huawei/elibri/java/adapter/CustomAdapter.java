/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.huawei.elibri.java.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.huawei.elibri.R;
import com.huawei.elibri.java.service.model.Books;
import com.huawei.elibri.java.utility.Util;
import com.huawei.elibri.java.view.fragment.MyBooksFragment;

import java.util.List;

/**
 * Recycler adapter for book list
 *
 * @author lWX916345
 * @since 06-11-2020
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private final List<Books> books;
    private final MyBooksFragment context;

    public CustomAdapter(MyBooksFragment context, List<Books> books) {
        this.context = context;
        this.books = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mybook_card_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.tvBookName.setText(books.get(position).getBookName());
        holder.tvBookType.setText(books.get(position).getInterest());
        Util.loadImage(context.getContext(), books.get(position).getImageUrl(), holder.imgBookImage);
        holder.itemView.setOnClickListener(
                view -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("BOOK_ID", books.get(position).getBookId());
                    bundle.putString("BOOK_URL", books.get(position).getBookUrl());
                    bundle.putString("BOOK_NAME", books.get(position).getBookName());
                    fragmentTransaction(bundle);
                });
    }

    private void fragmentTransaction(Bundle bundle) {
        Navigation.findNavController(context.getView()).navigate(R.id.action_nav_my_book_to_display_book, bundle);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    /**
     * ViewHolder class for book list
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        private final TextView tvBookName;
        private final TextView tvBookType;
        private final ImageView imgBookImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvBookName = itemView.findViewById(R.id.tvCardBookName);
            tvBookType = itemView.findViewById(R.id.tvCardBookType);
            imgBookImage = itemView.findViewById(R.id.imgCardBookImage);
        }
    }
}
