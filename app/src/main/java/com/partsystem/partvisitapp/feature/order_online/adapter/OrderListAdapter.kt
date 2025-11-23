package com.partsystem.partvisitapp.feature.order_online.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.databinding.ItemOrderListBinding
import com.partsystem.partvisitapp.feature.order_online.model.OrderFake


class OrderListAdapter(
    private val orders: List<OrderFake>,
    private val onClick: (OrderFake) -> Unit = {}
) :
    RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            tvAgentName.text = order.agentName
            tvCustomerName.text = order.customerName
            tvAddress.text = order.address
            tvPhone.text = order.phone
            tvOrderNumber.text = order.orderNumber
            tvOrderDate.text = order.orderDate
            tvOrderPrice.text = order.sum
            root.setOnClickListener { onClick(order) }
        }
    }

    override fun getItemCount(): Int = orders.size
}