package com.partsystem.partvisitapp.feature.main.home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemHomeMenuBinding
import com.partsystem.partvisitapp.feature.main.home.model.HomeMenuItem

class HomeMenuAdapter(
    private val items: List<HomeMenuItem>,
    private val onClick: (HomeMenuItem) -> Unit
) : RecyclerView.Adapter<HomeMenuAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemHomeMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeMenuItem) {

            if (item.isLoading) {
                binding.ivHomeMenuItem.hide()
                binding.pbHomeMenuItem.show()
            } else {
                binding.ivHomeMenuItem.show()
                binding.pbHomeMenuItem.gone()
                binding.ivHomeMenuItem.setImageResource(item.icon)
            }

            binding.tvHomeMenuItem.text = binding.root.context.getString(item.titleRes)
            binding.root.setOnClickListener {
                if (!item.isLoading) onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHomeMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}
