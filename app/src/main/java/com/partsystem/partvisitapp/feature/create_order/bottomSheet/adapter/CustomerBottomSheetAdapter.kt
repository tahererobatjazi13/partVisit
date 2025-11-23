package com.partsystem.partvisitapp.feature.create_order.bottomSheet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.databinding.ItemBottomSheetBinding

class CustomerBottomSheetAdapter(
    private val onClick: (CustomerEntity) -> Unit = {}
) : ListAdapter<CustomerEntity, CustomerBottomSheetAdapter.CustomerViewHolder>(CustomerDiffCallback()) {
    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding =
            ItemBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = getItem(position)
        holder.bind(customer)
    }

    fun setData(data: List<CustomerEntity>) {
        submitList(data)
    }

    inner class CustomerViewHolder(private val binding: ItemBottomSheetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: CustomerEntity) = with(binding) {

            tvName.text = customer.name
            ivItem.gone()

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = absoluteAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            root.setOnClickListener { onClick(customer) }
        }
    }
}

class CustomerDiffCallback : DiffUtil.ItemCallback<CustomerEntity>() {
    override fun areItemsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean {
        return oldItem == newItem
    }
}