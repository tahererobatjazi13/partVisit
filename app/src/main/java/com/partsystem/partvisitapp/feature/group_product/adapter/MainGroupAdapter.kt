package com.partsystem.partvisitapp.feature.group_product.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.utils.getColorAttr
import com.partsystem.partvisitapp.databinding.ItemMainGroupBinding

class MainGroupAdapter(
    private val onClick: (GroupProductEntity) -> Unit
) : ListAdapter<GroupProductEntity, MainGroupAdapter.MainGroupViewHolder>(MainGroupDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class MainGroupViewHolder(private val binding: ItemMainGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: GroupProductEntity, isSelected: Boolean) = with(binding) {
            val context = binding.root.context
            val selectedColor = context.getColor(R.color.colorPrimary)
            val defaultColor =
                getColorAttr(context, com.google.android.material.R.attr.colorOnPrimarySurface)
            val transparentColor = context.getColor(R.color.transparent)

            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = context.resources.getDimension(R.dimen.medium_size)
                setColor(if (isSelected) selectedColor else transparentColor)
            }

            vMainGroup.background = backgroundDrawable
            tvMainGroupName.text = group.name
            tvMainGroupName.setTextColor(if (isSelected) selectedColor else defaultColor)

            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onClick(group)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainGroupViewHolder {
        val binding =
            ItemMainGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainGroupViewHolder, position: Int) {
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


class MainGroupDiffCallback : DiffUtil.ItemCallback<GroupProductEntity>() {
    override fun areItemsTheSame(
        oldItem: GroupProductEntity,
        newItem: GroupProductEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: GroupProductEntity,
        newItem: GroupProductEntity
    ): Boolean {
        return oldItem == newItem
    }
}
