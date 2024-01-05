package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.R

class CropItemAdapter(
    private val cropList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CropItemAdapter.CropViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.crop_item_layout, parent, false)
        return CropViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        val crop = cropList[position]
        holder.bind(crop)
    }

    override fun getItemCount(): Int {
        return cropList.size
    }

    inner class CropViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cropTextView: TextView = itemView.findViewById(R.id.cropTextView)

        fun bind(crop: String) {
            cropTextView.text = crop
            itemView.setOnClickListener {
                onItemClick.invoke(crop)
            }
        }
    }
}
