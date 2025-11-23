package com.partsystem.partvisitapp.feature.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemCustomerListBinding

class CustomerListAdapter(
    private val onClick: (CustomerEntity) -> Unit = {},
    private val onEdit: (CustomerEntity) -> Unit = {}
) : ListAdapter<CustomerEntity, CustomerListAdapter.CustomerListViewHolder>(CustomerListDiffCallback()) {
    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerListViewHolder {
        val binding =
            ItemCustomerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerListViewHolder, position: Int) {
        val customer = getItem(position)
        holder.bind(customer)
    }

    fun setData(data: List<CustomerEntity>) {
        submitList(data)
    }

    inner class CustomerListViewHolder(private val binding: ItemCustomerListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: CustomerEntity) = with(binding) {

            tvCustomerName.text = "${bindingAdapterPosition + 1}_  ${customer.name}"

            val details = listOfNotNull(
                customer.tafsiliPhone1?.takeIf { it.isNotBlank() },
                customer.tafsiliPhone2?.takeIf { it.isNotBlank() },
                customer.tafsiliMobile?.takeIf { it.isNotBlank() }
            )


            if (details.isNotEmpty()) {
                binding.tvCustomerPhone.text = details.joinToString(" | ")
                binding.clCustomerPhone.show()
            } else {
                binding.clCustomerPhone.gone()
            }

            llEdit.setOnClickListener { onEdit(customer) }

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

class CustomerListDiffCallback : DiffUtil.ItemCallback<CustomerEntity>() {
    override fun areItemsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomerEntity, newItem: CustomerEntity): Boolean {
        return oldItem == newItem
    }
}