package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Models.CropItemViewModel
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R

class CropItemAdapter(private var onClick:(CropItemViewModel) -> Unit) : ListAdapter<CropItemViewModel, CropItemAdapter.ViewHolder>(FlowerDiffCallback)   {
    // Click listener interface
    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    class ViewHolder(itemView: View, val onClick: (CropItemViewModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val cropName: TextView = itemView.findViewById(R.id.tvCropName)
        private var currentCrop: CropItemViewModel? = null

        init {
            itemView.setOnClickListener {
                currentCrop?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(flower: CropItemViewModel) {
            currentCrop = flower

            cropName.text = flower.cropName
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.crop_item_layout, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)
    }
}
    object FlowerDiffCallback : DiffUtil.ItemCallback<CropItemViewModel>() {
        override fun areItemsTheSame(oldItem: CropItemViewModel, newItem: CropItemViewModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CropItemViewModel, newItem: CropItemViewModel): Boolean {
            return oldItem.id == newItem.id
        }
    }



