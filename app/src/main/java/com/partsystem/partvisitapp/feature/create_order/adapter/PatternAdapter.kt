package com.partsystem.partvisitapp.feature.create_order.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.databinding.ItemMainGroupBinding

class PatternAdapter(
    private val onClick: (PatternEntity) -> Unit
) : ListAdapter<PatternEntity, PatternAdapter.MainGroupViewHolder>(PatternDiffCallback()) {


    inner class MainGroupViewHolder(private val binding: ItemMainGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pattern: PatternEntity) = with(binding) {
            val context = binding.root.context


            tvMainGroupName.text = pattern.name

            root.setOnClickListener {
                onClick(pattern)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainGroupViewHolder {
        val binding =
            ItemMainGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainGroupViewHolder(binding)
    }

    /*    override fun onBindViewHolder(holder: MainGroupViewHolder ) {
            holder.bind(getItem(position))
        }*/
    override fun onBindViewHolder(holder: MainGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<PatternEntity>) {
        submitList(data) {
        }

    }
}


class PatternDiffCallback : DiffUtil.ItemCallback<PatternEntity>() {
    override fun areItemsTheSame(
        oldItem: PatternEntity,
        newItem: PatternEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PatternEntity,
        newItem: PatternEntity
    ): Boolean {
        return oldItem == newItem
    }
}
