package com.partsystem.partvisitapp.feature.create_order.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.getColorAttr
import com.partsystem.partvisitapp.databinding.ItemSpinnerBinding

class SpinnerAdapter(
    context: Context,
    private val items: MutableList<String>
) : ArrayAdapter<String>(context, R.layout.item_spinner, items) {

    private val inflater = LayoutInflater.from(context)
    var backColorOne: Int = 0
    var backColorTwo: Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = bindView(position, convertView, parent, false)
        val binding = ItemSpinnerBinding.bind(view)
        binding.tvName.apply {
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = bindView(position, convertView, parent, true)
        val binding = ItemSpinnerBinding.bind(view)
        binding.tvName.apply {
            isSingleLine = false
            ellipsize = null
        }
        return view
    }

    private fun bindView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        isDropdown: Boolean
    ): View {
        val binding = if (convertView == null) {
            ItemSpinnerBinding.inflate(inflater, parent, false)
        } else {
            ItemSpinnerBinding.bind(convertView)
        }

        binding.tvName.text = items[position]
        backColorOne = getColorAttr(context, R.attr.colorBasic)
        backColorTwo = getColorAttr(context, R.attr.colorBackSurface)

        if (isDropdown) {
            binding.view.show()
            if (position % 2 == 0) {
                binding.root.setBackgroundColor(backColorOne)
            } else {
                binding.root.setBackgroundColor(backColorTwo)
            }

        } else {
            binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            binding.view.gone()
        }

        return binding.root
    }

    fun setData(data: List<String>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}
