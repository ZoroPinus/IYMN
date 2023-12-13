package com.example.iymn.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iymn.Activity.DonationDetailsActivity
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.google.firebase.storage.FirebaseStorage

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
        holder.textView.text = ItemsViewModel.title

        val storageReference = FirebaseStorage.getInstance().reference.child("images/${ItemsViewModel.image}")
        Glide.with(holder.itemView.context)
            .load(storageReference)
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
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    fun updateData(newList: List<VegItemViewModel>) {
        mList = newList
        notifyDataSetChanged()
    }

}