package com.partsystem.partvisitapp.feature.customer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.databinding.ItemCustomerDirectionBinding


class CustomerDirectionAdapter(
    private val items: List<String>,
) : RecyclerView.Adapter<CustomerDirectionAdapter.CustomerDirectionViewHolder>() {

    inner class CustomerDirectionViewHolder(val binding: ItemCustomerDirectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: String) = with(binding) {
            tvAddress.text = address
            tvAddress.text = "${bindingAdapterPosition + 1}_  ${address}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDirectionViewHolder {
        val binding = ItemCustomerDirectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerDirectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerDirectionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

