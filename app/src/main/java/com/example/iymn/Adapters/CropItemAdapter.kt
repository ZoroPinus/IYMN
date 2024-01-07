package com.example.iymn.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Models.CropItemViewModel
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R

class CropItemAdapter : ListAdapter<CropItemViewModel, CropItemAdapter.ViewHolder>(FlowerDiffCallback) {
    private var onItemClickListener: ((CropItemViewModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (CropItemViewModel) -> Unit) {
        onItemClickListener = listener
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cropName: Button = itemView.findViewById(R.id.btnCropName)
        private var currentCrop: CropItemViewModel? = null

        init {
            cropName.setOnClickListener {
                currentCrop?.let {
                    Log.v("CropItemAdapter", "Button clicked: ${it.cropName}")
                    onItemClickListener?.invoke(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(crop: CropItemViewModel) {
            currentCrop = crop
            cropName.text = crop.cropName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.crop_item_layout, parent, false)
        return ViewHolder(view)
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


