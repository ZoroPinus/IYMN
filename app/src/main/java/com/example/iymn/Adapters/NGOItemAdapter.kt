package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iymn.Models.NGOItemViewModel
import com.example.iymn.R

class NGOItemAdapter : ListAdapter<NGOItemViewModel, NGOItemAdapter.ViewHolder>(NGOItemDiffCallback) {
    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageview)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val item1: TextView = itemView.findViewById(R.id.tvItem1)
        private val item2: TextView = itemView.findViewById(R.id.tvItem2)
        private var currentItem: NGOItemViewModel? = null

        init {
            itemView.setOnClickListener {
                currentItem?.let {
                    onItemClickListener?.invoke(it.documentId)
                }
            }
        }

        /* Bind VegItemViewModel data to views. */
        fun bind(item: NGOItemViewModel) {
            currentItem = item
            title.text = item.ngoName
            item1.text = item.contact
            item2.text = item.address

            Glide.with(itemView.context)
                .load(item.image)
                .placeholder(R.drawable.ic_folder) // Placeholder image while loading
                .error(R.drawable.ic_insert_img) // Image to show if loading fails
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ngo_item_layout, parent, false)

        val isDarkTheme =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        if (isDarkTheme) {
            view.findViewById<CardView>(R.id.cardViewRoot).setBackgroundColor(
                view.context.getColor(R.color.white)
            )
        } else {
            view.findViewById<CardView>(R.id.cardViewRoot).setBackgroundColor(
                view.context.getColor(R.color.white)
            )
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

object NGOItemDiffCallback : DiffUtil.ItemCallback<NGOItemViewModel>() {
    override fun areItemsTheSame(oldItem: NGOItemViewModel, newItem: NGOItemViewModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NGOItemViewModel, newItem: NGOItemViewModel): Boolean {
        return oldItem.documentId == newItem.documentId
    }
}