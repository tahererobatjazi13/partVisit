package com.partsystem.partvisitapp.feature.report_factor.offline.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import com.partsystem.partvisitapp.core.utils.extensions.clean
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.report_factor.offline.model.getPackingValueFormatted
import java.text.DecimalFormat

class OfflineOrderDetailAdapter(
    private val factorViewModel: FactorViewModel,
) :
    ListAdapter<FactorDetailUiModel, OfflineOrderDetailAdapter.OfflineOrderDetailViewHolder>(
        OfflineOrderDetailDiffCallback()
    ) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class OfflineOrderDetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailUiModel) = with(binding) {
            binding.clDelete.gone()

            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                )
            } else {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorRow)
                )
            }

            if (item.isGift == 1) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.yellow_FFEB3B
                    )
                )
            }

            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvPackingName.text = item.packingName

            tvPackingValue.text = item.getPackingValueFormatted()
            tvUnitName.text = item.unit1Name
            tvRate1.text = formatter.format(item.unit1Rate) + " ریال"
            tvUnitValue.text = item.unit1Value.clean()
            val total = item.unit1Rate * item.unit1Value

            clDiscountPrice.gone()
            if (item.vat != null && item.vat > 0) {
                clVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"
                tvPriceAfterVat.text = formatter.format(item.vat + total) + " م.بعداز مالیات"

            } else {
                clVat.gone()
            }

                    if (item.discountPrice > 0) {

                        clDiscountPrice.show()
                        tvSumPrice.show()

                        tvDiscountPrice.text =
                            formatter.format(item.discountPrice) + " تخفیف"

                        tvPriceAfterDiscount.text =
                            formatter.format(total - item.discountPrice) + " م.بعداز تخفیف"
                        // قیمت اصلی → خط بخورد
                        tvSumPrice.paintFlags = tvSumPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvSumPrice.setTextColor(Color.GRAY)
                        tvSumPrice.text = formatter.format(total) + " ریال"

                    } else {

                        binding.clDiscountPrice.gone()
                        tvSumPrice.show()
                        tvSumPrice.paintFlags =
                            tvSumPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        tvSumPrice.text = formatter.format(total) + " ریال"
                    }


            if (bindingAdapterPosition < itemCount - 1) {
                view.show()
            } else view.gone()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OfflineOrderDetailViewHolder {
        val binding =
            ItemOrderDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return OfflineOrderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfflineOrderDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<FactorDetailUiModel>) {
        submitList(data)
    }
}

class OfflineOrderDetailDiffCallback : DiffUtil.ItemCallback<FactorDetailUiModel>() {
    override fun areItemsTheSame(
        oldItem: FactorDetailUiModel,
        newItem: FactorDetailUiModel
    ): Boolean {
        return oldItem.factorId == newItem.factorId
    }

    override fun areContentsTheSame(
        oldItem: FactorDetailUiModel,
        newItem: FactorDetailUiModel
    ): Boolean {
        return oldItem == newItem
    }
}
