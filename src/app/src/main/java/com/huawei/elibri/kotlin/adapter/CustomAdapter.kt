package com.huawei.elibri.kotlin.adapter


/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.huawei.elibri.R
import com.huawei.elibri.kotlin.service.model.Books
import com.huawei.elibri.kotlin.utility.Util.loadImage
import com.huawei.elibri.kotlin.view.fragment.MyBooksFragment


/**
 * Recycler adapter for book list
 *
 * @author lWX916345
 * @since 06-11-2020
 */
class CustomAdapter(private val context: MyBooksFragment, private val books: List<Books>) :
    RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.mybook_card_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.tvBookName.setText(books[position].bookName)
        holder.tvBookType.text = books[position].interest
        loadImage(
            context.context,
            books[position].imageUrl,
            holder.imgBookImage
        )
        holder.itemView.setOnClickListener { view: View? ->
            val bundle = Bundle()
            bundle.putInt("BOOK_ID", books[position].bookId!!)
            bundle.putString("BOOK_URL", books[position].bookUrl)
            bundle.putString("BOOK_NAME", books[position].bookName)
            fragmentTransaction(bundle)
        }
    }

    private fun fragmentTransaction(bundle: Bundle) {
        Navigation.findNavController(context.view!!)
            .navigate(R.id.action_nav_my_book_to_display_book, bundle)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    /**
     * ViewHolder class for book list
     */
    class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // init the item view's
        val tvBookName: TextView
        val tvBookType: TextView
        val imgBookImage: ImageView

        init {
            tvBookName = itemView.findViewById(R.id.tvCardBookName)
            tvBookType = itemView.findViewById(R.id.tvCardBookType)
            imgBookImage = itemView.findViewById(R.id.imgCardBookImage)
        }
    }

}
