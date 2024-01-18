package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Models.DonatedItem
import com.example.iymn.R

class DonatedItemAdapter(private var donatedItem: List<DonatedItem>): RecyclerView.Adapter<DonatedItemAdapter.MyViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(documentId: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.donated_item_layout, parent, false)

        return MyViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val ItemsViewModel = donatedItem[position]

        // sets the text to the textview from our itemHolder class
        holder.title.text = ItemsViewModel.name
        holder.item1.text = ItemsViewModel.ngoPartner
        holder.item2.text = ItemsViewModel.date


        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(ItemsViewModel.id)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return donatedItem.size
    }

    // Holds the views for adding it to image and text
    class MyViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val item1: TextView = itemView.findViewById(R.id.tvItem1)
        val item2: TextView = itemView.findViewById(R.id.tvItem2)
    }

    fun updateData(newList: List<DonatedItem>) {
        donatedItem = newList
        notifyDataSetChanged()
    }
}