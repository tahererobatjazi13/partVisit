package com.partsystem.partvisitapp.feature.group_product.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.databinding.ItemSubGroupBinding

class SubGroupAdapter(
    private val onClick: (GroupProductEntity) -> Unit = {}
) : ListAdapter<GroupProductEntity, SubGroupAdapter.SubGroupViewHolder>(SubGroupDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class SubGroupViewHolder(private val binding: ItemSubGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subGroup: GroupProductEntity, isSelected: Boolean) = with(binding) {
            tvSubGroupName.text = subGroup.name

            // تغییر بک‌گراند بر اساس انتخاب
            val backgroundRes = if (isSelected)
                R.drawable.bg_rectangle_focused
            else
                R.drawable.bg_rectangle_default

            cvSubGroup.setBackgroundResource(backgroundRes)

            root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onClick(subGroup)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubGroupViewHolder {
        val binding =
            ItemSubGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubGroupViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun setData(data: List<GroupProductEntity>) {
        submitList(data) {
            if (selectedPosition == RecyclerView.NO_POSITION && data.isNotEmpty()) {
                selectedPosition = 0
                notifyItemChanged(0)
            }
        }
    }
}

class SubGroupDiffCallback : DiffUtil.ItemCallback<GroupProductEntity>() {
    override fun areItemsTheSame(
        oldItem: GroupProductEntity,
        newItem: GroupProductEntity
    ): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: GroupProductEntity,
        newItem: GroupProductEntity
    ): Boolean =
        oldItem == newItem
}
