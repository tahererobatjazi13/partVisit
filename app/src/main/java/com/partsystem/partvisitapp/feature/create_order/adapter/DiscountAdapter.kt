package com.partsystem.partvisitapp.feature.create_order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.core.database.entity.Discount
import com.partsystem.partvisitapp.databinding.ItemDiscountBinding


class DiscountAdapter(
    private val discounts: List<Discount>,
    private val onItemChecked: (Discount) -> Unit
) : RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

    private var selectedDiscount: Discount? = null

    inner class DiscountViewHolder(val binding: ItemDiscountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: Discount) = with(binding) {
            cbDiscount.text = item.name
            cbDiscount.isChecked = (item == selectedDiscount)

            cbDiscount.setOnCheckedChangeListener(null)
            cbDiscount.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDiscount = item
                    onItemChecked(item)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = ItemDiscountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(discounts[position])
    }

    override fun getItemCount(): Int = discounts.size
}
