package com.partsystem.partvisitapp.feature.report_factor.offline.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorAttr
import com.partsystem.partvisitapp.core.utils.getColorAttrSafe
import com.partsystem.partvisitapp.databinding.ItemOrderListBinding
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import java.text.DecimalFormat

class OfflineOrderListAdapter(
    private val showSyncButton: Boolean = false,
    private val onDelete: (FactorHeaderUiModel) -> Unit,
    private val onSync: (FactorHeaderUiModel) -> Unit,
    private val onClick: (FactorHeaderUiModel) -> Unit = {}
) : ListAdapter<FactorHeaderUiModel, OfflineOrderListAdapter.OfflineOrderListViewHolder>(
    OfflineOrderListDiffCallback()
) {
    private val formatter = DecimalFormat("#,###")

    inner class OfflineOrderListViewHolder(private val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorHeaderUiModel) = with(binding) {
            val context = binding.root.context

            if (item.isSending) {
                tvSyncOrder.hide()
                pbSyncOrder.show()
            } else {
                pbSyncOrder.gone()
                tvSyncOrder.show()
            }

            tvOrderNumber.text = item.factorId.toString()
            tvCustomerName.text = item.customerName ?: "-"
            tvPatternName.text = item.patternName ?: "-"
            Log.d("finalPriceadapter", item.finalPrice.toString())

            tvFinalPrice.text = formatter.format(item.finalPrice) + " ریال"
            tvDateTime.text = "${item.persianDate} _ ${item.createTime}"
            ivDelete.show()

            tvStatus.text = when (item.sabt) {
                1 -> context.getString(R.string.label_completed_order_item)
                else -> context.getString(R.string.label_not_completed_order_item)
            }

            tvStatus.setTextColor(
                context.getColorAttrSafe(
                    if (item.sabt == 1) R.attr.colorSuccess else R.attr.colorWarning,
                    if (item.sabt == 1) R.color.green_21BF73 else R.color.orange_FF5722
                )
            )

            root.setOnClickListener { onClick(item) }

            if (showSyncButton && item.hasDetail && item.sabt == 1) {
                clSyncOrder.show()
            } else {
                clSyncOrder.gone()
            }

            ivDelete.setOnClickListener {
                onDelete(item)
            }

            clSyncOrder.setOnClickListener {
                if (!item.isSending) {
                    onSync(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineOrderListViewHolder {
        val binding =
            ItemOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfflineOrderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfflineOrderListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<FactorHeaderUiModel>) {
        submitList(data)
    }
}

class OfflineOrderListDiffCallback : DiffUtil.ItemCallback<FactorHeaderUiModel>() {
    override fun areItemsTheSame(oldItem: FactorHeaderUiModel, newItem: FactorHeaderUiModel) =
        oldItem.factorId == newItem.factorId

    override fun areContentsTheSame(oldItem: FactorHeaderUiModel, newItem: FactorHeaderUiModel) =
        oldItem == newItem
}
