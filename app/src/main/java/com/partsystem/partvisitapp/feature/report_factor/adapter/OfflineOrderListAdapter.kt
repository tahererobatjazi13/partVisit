package com.partsystem.partvisitapp.feature.report_factor.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemOrderListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.HeaderOrderViewModel
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import java.text.DecimalFormat

class OfflineOrderListAdapter(
    private val showSyncButton: Boolean = false,
    private val customerViewModel: CustomerViewModel,
    private val headerOrderViewModel: HeaderOrderViewModel,
    private val onClick: (FactorHeaderEntity) -> Unit = {},
) : ListAdapter<FactorHeaderEntity, OfflineOrderListAdapter.OfflineOrderListViewHolder>(
    OfflineOrderListDiffCallback()
) {

    private val formatter = DecimalFormat("#,###")
    private var customDialog: CustomDialog? = null

    inner class OfflineOrderListViewHolder(private val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorHeaderEntity) = with(binding) {
            customDialog = CustomDialog.instance

            tvOrderNumber.text = item.id.toString()


            customerViewModel.getCustomerById(item.customerId!!).observeForever { item ->
                if (item != null) {
                    binding.tvCustomerName.text = item.name
                }
            }
            headerOrderViewModel.getPatternById(item.patternId!!).observeForever { item ->
                if (item != null) {
                    binding.tvCustomerName.text = item.name
                }
            }

            // tvCustomerName.text = item.customerName
         //  tvPatternName.text = item.patternName
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineOrderListViewHolder {
        val binding =
            ItemOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfflineOrderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfflineOrderListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<FactorHeaderEntity>) {
        submitList(data)
    }
}

class OfflineOrderListDiffCallback : DiffUtil.ItemCallback<FactorHeaderEntity>() {
    override fun areItemsTheSame(oldItem: FactorHeaderEntity, newItem: FactorHeaderEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: FactorHeaderEntity, newItem: FactorHeaderEntity) =
        oldItem == newItem
}
