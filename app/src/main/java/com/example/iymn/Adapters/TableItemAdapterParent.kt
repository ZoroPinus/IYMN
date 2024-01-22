package com.example.iymn.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Models.TableItem
import com.example.iymn.R

class TableItemAdapterParent(private val donations: List<TableItem>) :
    RecyclerView.Adapter<TableItemAdapterParent.DonationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_table_item_parent, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donations[position]
        holder.bind(donation)
    }

    override fun getItemCount(): Int = donations.size

    inner class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val donorName: TextView = itemView.findViewById(R.id.tableDonorName)
        private val cropNameTextView: TextView = itemView.findViewById(R.id.tableCropName)
        private val ngoPartner: TextView = itemView.findViewById(R.id.tableNgoPartner)
        private val dateTextView: TextView = itemView.findViewById(R.id.tableDateTime)
        private val quantityTextView: TextView = itemView.findViewById(R.id.tableQuantity)
        private val statusTextView: TextView = itemView.findViewById(R.id.tableStatus)
        private val expandedView: RelativeLayout = itemView.findViewById(R.id.expandedView)
        init {
            itemView.setOnClickListener {
                val detailsVisibility = if (expandedView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                expandedView.visibility = detailsVisibility
            }
        }

        fun bind(donation: TableItem) {
            donorName.text = donation.name
            cropNameTextView.text = donation.cropName
            dateTextView.text = donation.date
            ngoPartner.text = donation.ngoPartner
            quantityTextView.text = donation.quantity
            statusTextView.text = donation.status
        }
    }
}