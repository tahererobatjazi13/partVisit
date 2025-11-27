package com.partsystem.partvisitapp.feature.report_factor.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.databinding.ItemReportFactorListBinding
import java.text.DecimalFormat

class ReportFactorListAdapter(
    private val onClick: (ReportFactorDto) -> Unit = {},
) : ListAdapter<ReportFactorDto, ReportFactorListAdapter.ReportFactorListViewHolder>(
    ReportFactorListDiffCallback()
) {

    private val formatter = DecimalFormat("#,###")

    inner class ReportFactorListViewHolder(private val binding: ItemReportFactorListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {

            tvCustomerName.text = item.customerName
            tvDateTime.text = item.persianDate + " _ " + item.createTime
            tvPatternName.text = item.patternName
            tvFactorNumber.text = item.id.toString()
            tvFinalPrice.text = formatter.format(item.finalPrice) + " ریال"

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportFactorListViewHolder {
        val binding =
            ItemReportFactorListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportFactorListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportFactorListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ReportFactorDto>) {
        submitList(data)
    }
}

class ReportFactorListDiffCallback : DiffUtil.ItemCallback<ReportFactorDto>() {
    override fun areItemsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem == newItem
}
