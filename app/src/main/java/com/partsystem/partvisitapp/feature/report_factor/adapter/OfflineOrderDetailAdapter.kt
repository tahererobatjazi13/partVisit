package com.partsystem.partvisitapp.feature.report_factor.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.modelDto.FactorDetailOfflineModel
import com.partsystem.partvisitapp.core.utils.extensions.clean
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorFromAttr
import com.partsystem.partvisitapp.databinding.ItemOrderDetailBinding
import java.text.DecimalFormat

class OfflineOrderDetailAdapter :
    ListAdapter<FactorDetailOfflineModel, OfflineOrderDetailAdapter.OfflineOrderDetailViewHolder>(
        OfflineOrderDetailDiffCallback()
    ) {
    private val formatter = DecimalFormat("#,###,###,###")

    inner class OfflineOrderDetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FactorDetailOfflineModel) = with(binding) {

            if (bindingAdapterPosition % 2 == 0) {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorBasic)
                )
            } else {
                binding.root.setBackgroundColor(
                    itemView.context.getColorFromAttr(R.attr.colorRow)
                )
            }
            tvProductName.text = "${bindingAdapterPosition + 1}_ ${item.productName}"
            tvPackingName.text = item.packingName
            tvPackingValue.text = item.packingValue.clean()
            tvUnitName.text = item.unit1Name
            tvRate1.text = formatter.format(item.unit1Rate) + " ریال"
            tvUnitValue.text = item.unit1Value.clean()
            val total = item.unit1Rate * item.unit1Value
            tvPrice.text = formatter.format(total) + " ریال"

            /*       if (item.discountPrice != null && item.discountPrice > 0) {
                       tvDiscountPrice.show()
                       tvPriceAfterDiscount.show()
                       tvPrice.show()

                       tvDiscountPrice.text = formatter.format(item.discountPrice) + " تخفیف"
                       tvPriceAfterDiscount.text =
                           formatter.format(item.priceAfterDiscount) + " م.بعداز تخفیف"
                       // قیمت اصلی → خط بخورد
                       tvPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                       tvPrice.setTextColor(Color.GRAY)
                       tvPrice.text = formatter.format(item.price) + " ریال"

                   } else {

                       tvPrice.show()
                       tvDiscountPrice.gone()
                       tvPriceAfterDiscount.gone()
                       tvPrice.paintFlags = tvPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                       tvPrice.text = formatter.format(item.price) + " ریال"
                   }*/

            if (item.vat != null && item.vat > 0) {
                tvVat.show()
                tvPriceAfterVat.show()

                tvVat.text = formatter.format(item.vat) + " مالیات"
                // tvPriceAfterVat.text = formatter.format(item.priceAfterVat) + " م.بعداز مالیات"

            } else {
                tvVat.gone()
                tvPriceAfterVat.gone()
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

    fun setData(data: List<FactorDetailOfflineModel>) {
        submitList(data)
    }
}

class OfflineOrderDetailDiffCallback : DiffUtil.ItemCallback<FactorDetailOfflineModel>() {
    override fun areItemsTheSame(
        oldItem: FactorDetailOfflineModel,
        newItem: FactorDetailOfflineModel
    ): Boolean {
        return oldItem.factorId == newItem.factorId
    }

    override fun areContentsTheSame(
        oldItem: FactorDetailOfflineModel,
        newItem: FactorDetailOfflineModel
    ): Boolean {
        return oldItem == newItem
    }
}
