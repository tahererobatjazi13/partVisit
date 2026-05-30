package com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.databinding.ItemDirectionBottomSheetBinding
import com.partsystem.partvisitapp.feature.report_factor.online.model.DirectionModel

class DirectionBottomSheetAdapter(
    private val onSelectionChanged: (List<DirectionModel>) -> Unit
) : ListAdapter<DirectionModel, DirectionBottomSheetAdapter.DirectionViewHolder>(
    DirectionDiffCallback()
) {
    private val selectedItems = mutableSetOf<String>()
   // private fun currentList(): List<DirectionModel> = ArrayList(currentList)

    fun getSelectedDirections(): List<DirectionModel> {
        return currentList.filter { selectedItems.contains(it.directionCode) }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectionViewHolder {
        val binding =
            ItemDirectionBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DirectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DirectionViewHolder, position: Int) {
        val customer = getItem(position)
        holder.bind(customer)
    }

    fun setData(data: List<DirectionModel>) {
        submitList(data)
    }

    inner class DirectionViewHolder(private val binding: ItemDirectionBottomSheetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(direction: DirectionModel) = with(binding) {
            tvName.text = direction.directionName
            cbSelect.isChecked = selectedItems.contains(direction.directionCode)

            root.setOnClickListener {
                toggle(direction)
            }

            cbSelect.setOnClickListener {
                toggle(direction)
            }

        }

        private fun toggle(item: DirectionModel) {
            if (selectedItems.contains(item.directionCode))
                selectedItems.remove(item.directionCode)
            else
                selectedItems.add(item.directionCode)

            notifyItemChanged(absoluteAdapterPosition)
            onSelectionChanged(getSelectedDirections())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun preselect(codes: List<String>) {
        selectedItems.clear()
        selectedItems.addAll(codes)
        notifyDataSetChanged()
    }

}

class DirectionDiffCallback : DiffUtil.ItemCallback<DirectionModel>() {
    override fun areItemsTheSame(
        oldItem: DirectionModel,
        newItem: DirectionModel
    ): Boolean {
        return oldItem.directionCode == newItem.directionCode
    }

    override fun areContentsTheSame(
        oldItem: DirectionModel,
        newItem: DirectionModel
    ): Boolean {
        return oldItem == newItem
    }
}