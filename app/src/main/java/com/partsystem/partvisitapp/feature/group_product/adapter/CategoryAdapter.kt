package com.partsystem.partvisitapp.feature.group_product.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.GroupProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.databinding.ItemCategoryBinding
import java.io.File

class CategoryAdapter(
    private val onClick: (GroupProductEntity) -> Unit = {}
) : ListAdapter<GroupProductEntity, CategoryAdapter.CategoryViewHolder>(
    CategoryDiffCallback()
) {
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: GroupProductEntity, isSelected: Boolean) = with(binding) {
            tvCategoryName.text = category.name

            val backgroundRes = if (isSelected)
                R.drawable.bg_rectangle_focused
            else
                R.drawable.bg_rectangle_default

            cvImageCategory.setBackgroundResource(backgroundRes)

            val images = images[category.id]
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(ivCategory.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivCategory)
            } else {
                ivCategory.setImageResource(R.drawable.ic_placeholder)
            }

            root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onClick(category)
            }
        }
    }

    fun setData(data: List<GroupProductEntity>) {
        submitList(data) {
            if (selectedPosition == RecyclerView.NO_POSITION && data.isNotEmpty()) {
                selectedPosition = 0
                notifyItemChanged(0)
            }
        }
    }

    fun resetSelection() {
        selectedPosition = RecyclerView.NO_POSITION
    }

    private var images: Map<Int, List<ProductImageEntity>> = emptyMap()

    fun setImages(map: Map<Int, List<ProductImageEntity>>) {
        images = map
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<GroupProductEntity>() {
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
