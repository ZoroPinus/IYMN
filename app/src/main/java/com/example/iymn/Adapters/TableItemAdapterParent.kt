package com.example.iymn.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Models.TableItem
import com.example.iymn.R
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class TableItemAdapterParent(private val donations: MutableList<TableItem>) :
    RecyclerView.Adapter<TableItemAdapterParent.DonationViewHolder>() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_table_item_parent, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donations[position]
        holder.bind(donation)

        holder.setAcceptClickListener {
            // Update the status in Firestore when accept button is clicked
            updateStatusInFirestore(holder.itemView.context, donation.id, "ACCEPTED") {
                // Show success modal (Toast in this case)
                showToast(holder.itemView.context, "Status updated successfully")

                // Update the list and refresh the adapter
                donation.status = "ACCEPTED"
                notifyItemChanged(position)
            }
        }

        holder.setRejectClickListener {
            // Update the status in Firestore when reject button is clicked
            updateStatusInFirestore(holder.itemView.context, donation.id, "REJECTED") {
                // Show success modal (Toast in this case)
                showToast(holder.itemView.context, "Status updated successfully")

                // Update the list and refresh the adapter
                donation.status = "REJECTED"
                notifyItemChanged(position)
            }
        }

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

        private val rejectButton: CircleImageView = itemView.findViewById(R.id.tableBtnReject)
        private val acceptButton: CircleImageView = itemView.findViewById(R.id.tableBtnAccept)

        // Function to set click listener for accept button
        fun setAcceptClickListener(listener: () -> Unit) {
            acceptButton.setOnClickListener {
                listener.invoke()
            }
        }

        // Function to set click listener for reject button
        fun setRejectClickListener(listener: () -> Unit) {
            rejectButton.setOnClickListener {
                listener.invoke()
            }
        }
        init {
            expandedView.visibility = View.GONE
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

    private fun updateStatusInFirestore(context: Context, itemId: String, newStatus: String, onSuccess: () -> Unit) {
        // Get the reference to the document in Firestore
        val documentReference = firestore.collection("donations").document(itemId)

        // Update the status field in the document
        documentReference
            .update("status", newStatus)
            .addOnSuccessListener {
                // Handle success
                onSuccess.invoke()
                Log.d("Firestore", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                // Handle error
                Log.w("Firestore", "Error updating document", e)
            }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}