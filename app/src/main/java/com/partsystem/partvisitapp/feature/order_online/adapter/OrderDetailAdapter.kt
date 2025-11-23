package com.partsystem.partvisitapp.feature.order_online.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.order_online.model.OrderDetailFake
import java.text.DecimalFormat

class OrderDetailAdapter :
    ListAdapter<OrderDetailFake, OrderDetailAdapter.OrderViewHolder>(OrderDiffCallback()) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class OrderViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderDetailFake: OrderDetailFake) = with(binding) {
            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_light)
                )
            } else {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_dark)
                )
            }
            // tvOrderName.text = "${bindingAdapterPosition + 1}_ ${orderDetail.name}"
            // tvOrderPrice.text = formatter.format(orderDetail.price) + " ریال"
            // tvOrderAmount.text = "${orderDetail.amount} عدد"
            if (bindingAdapterPosition < itemCount - 1) {
                vOrder.show()
            } else vOrder.gone()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<OrderDetailFake>() {
    override fun areItemsTheSame(oldItem: OrderDetailFake, newItem: OrderDetailFake): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: OrderDetailFake, newItem: OrderDetailFake): Boolean {
        return oldItem == newItem
    }
}
