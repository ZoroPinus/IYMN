package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R

class VegItemAdapter(private var mList: List<VegItemViewModel>) : RecyclerView.Adapter<VegItemAdapter.ViewHolder>() {
    // Click listener interface
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(documentId: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.veg_item_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.title
        holder.item1.text = ItemsViewModel.item1
        holder.item2.text = ItemsViewModel.item2
        holder.item3.text = ItemsViewModel.item3
        holder.item4.text = ItemsViewModel.item4

        Glide.with(holder.itemView.context)
            .load(ItemsViewModel.image)
            .placeholder(R.drawable.ic_folder) // Placeholder image while loading
            .error(R.drawable.ic_insert_img) // Image to show if loading fails
            .into(holder.imageView)
        // Set onClickListener to show details on item click
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(ItemsViewModel.documentId)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val item1: TextView = itemView.findViewById(R.id.tvItem1)
        val item2: TextView = itemView.findViewById(R.id.tvItem2)
        val item3: TextView = itemView.findViewById(R.id.tvItem3)
        val item4: TextView = itemView.findViewById(R.id.tvItem4)
    }

    fun updateData(newList: List<VegItemViewModel>) {
        mList = newList
        notifyDataSetChanged()
    }

}