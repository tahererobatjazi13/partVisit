package com.partsystem.partvisitapp.feature.report_factor.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemOrderListBinding
import java.text.DecimalFormat

class OrderListAdapter(
    private val showSyncButton: Boolean = false,
    private val onClick: (ReportFactorDto) -> Unit = {},
) : ListAdapter<ReportFactorDto, OrderListAdapter.OrderListViewHolder>(
    OrderListDiffCallback()
) {

    private val formatter = DecimalFormat("#,###")
    private var customDialog: CustomDialog? = null

    inner class OrderListViewHolder(private val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {
            customDialog = CustomDialog.instance

            tvOrderNumber.text = item.id.toString()
            tvCustomerName.text = item.customerName
            tvPatternName.text = item.patternName
            tvDateTime.text = item.persianDate + " _ " + item.createTime
            tvFinalPrice.text = formatter.format(item.finalPrice) + " ریال"
            root.setOnClickListener { onClick(item) }
            if (showSyncButton) {
                tvSyncOrder.show()
            } else {
                tvSyncOrder.gone()
            }
            val context = binding.root.context
            tvSyncOrder.setOnClickListener {
                customDialog!!.showDialog(
                    context,
                  context.getString(R.string.msg_sure_send_order),
                    true,
                   context.getString(R.string.label_no),
                  context.getString(R.string.label_ok),
                    true,
                    true
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListViewHolder {
        val binding =
            ItemOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ReportFactorDto>) {
        submitList(data)
    }
}

class OrderListDiffCallback : DiffUtil.ItemCallback<ReportFactorDto>() {
    override fun areItemsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem == newItem
}
