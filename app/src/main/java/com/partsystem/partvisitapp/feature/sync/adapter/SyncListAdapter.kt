package com.partsystem.partvisitapp.feature.sync.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.databinding.ItemSyncRowBinding
import com.partsystem.partvisitapp.feature.sync.model.SyncItem


class SyncListAdapter(
    private val onCheckChanged: (Int, Boolean) -> Unit
) : ListAdapter<SyncItem, SyncListAdapter.SyncViewHolder>(DiffCallback()) {

    inner class SyncViewHolder(val binding: ItemSyncRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncItem) {
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.lastUpdate

            binding.cbSelectItem.setOnCheckedChangeListener(null)
            binding.cbSelectItem.isChecked = item.isChecked

            binding.cbSelectItem.setOnCheckedChangeListener { _, checked ->
                onCheckChanged(bindingAdapterPosition, checked)
            }

            binding.root.setOnClickListener {
                val newState = !binding.cbSelectItem.isChecked
                binding.cbSelectItem.isChecked = newState
                onCheckChanged(bindingAdapterPosition, newState)
            }
        }

    }
    fun notifyAllCheckedChanged() {
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncViewHolder {
        val binding = ItemSyncRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SyncViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SyncViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SyncItem>() {
        override fun areItemsTheSame(old: SyncItem, new: SyncItem) = old.id == new.id
        override fun areContentsTheSame(old: SyncItem, new: SyncItem) = old == new

    }

}

