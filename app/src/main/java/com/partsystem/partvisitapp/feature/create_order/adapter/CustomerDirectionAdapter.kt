package com.partsystem.partvisitapp.feature.create_order.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity

import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.databinding.ItemMainGroupBinding

class CustomerDirectionAdapter(
    private val onClick: (CustomerDirectionEntity) -> Unit
) : ListAdapter<CustomerDirectionEntity, CustomerDirectionAdapter.CustomerDirectionViewHolder>(CustomerDirectionDiffCallback()) {


    inner class CustomerDirectionViewHolder(private val binding: ItemMainGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pattern: CustomerDirectionEntity) = with(binding) {
            val context = binding.root.context


            tvMainGroupName.text = pattern.fullAddress

            root.setOnClickListener {
                onClick(pattern)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerDirectionViewHolder {
        val binding =
            ItemMainGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerDirectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerDirectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<CustomerDirectionEntity>) {
        submitList(data) {
        }

    }
}


class CustomerDirectionDiffCallback : DiffUtil.ItemCallback<CustomerDirectionEntity>() {
    override fun areItemsTheSame(
        oldItem: CustomerDirectionEntity,
        newItem: CustomerDirectionEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CustomerDirectionEntity,
        newItem: CustomerDirectionEntity
    ): Boolean {
        return oldItem == newItem
    }
}
