package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iymn.Models.FeedbackListModel
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.squareup.picasso.Picasso

class FeedbacksListAdapter(private var mList: List<FeedbackListModel>) : RecyclerView.Adapter<FeedbacksListAdapter.ViewHolder>() {

    // Click listener interface
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(documentId: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.name
        holder.date.text = ItemsViewModel.date
        holder.item1.text = ItemsViewModel.rating
        holder.item2.text = ItemsViewModel.textFeedback

        Glide.with(holder.itemView.context)
            .load(ItemsViewModel.image)
            .placeholder(R.drawable.ic_folder) // Placeholder image while loading
            .error(R.drawable.ic_insert_img) // Image to show if loading fails
            .into(holder.imageView)

        // Set onClickListener to show details on item click
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(ItemsViewModel.name)
        }
    }


    override fun getItemCount(): Int {
        return mList.size
    }
    fun updateData(newList: List<FeedbackListModel>) {
        mList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val item1: TextView = itemView.findViewById(R.id.tvItem1)
        val item2: TextView = itemView.findViewById(R.id.tvItem2)
    }
}