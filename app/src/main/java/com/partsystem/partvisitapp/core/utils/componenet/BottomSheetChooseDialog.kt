package com.partsystem.partvisitapp.core.utils.componenet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentBottomSheetChooseBinding

class BottomSheetChooseDialog : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetChooseBinding? = null
    private val binding get() = _binding!!
    private var onDismissListener: (() -> Unit)? = null

    private var dialogTitle: Int? = null
    private val options = mutableListOf<ChooseOption>()

    data class ChooseOption(
        val title: Int,
        val iconRes: Int?,
        val onClick: () -> Unit
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.tvTitle.text =
            dialogTitle?.let { getString(it) } ?: getString(R.string.label_choose)

        val optionBindings = listOf(
            binding.firstChoice,
            binding.secondChoice,
            binding.thirdChoice
        )

        // مخفی کردن همه
        optionBindings.forEach { it.root.gone() }

        // تنظیم گزینه‌های فعال
        options.take(optionBindings.size).forEachIndexed { index, option ->
            val optBinding = optionBindings[index]
            optBinding.root.show()
            optBinding.tvName.text = getString(option.title)
            option.iconRes?.let { optBinding.ivItem.setImageResource(it) }
            optBinding.cvMain.setOnClickListener {
                option.onClick()
                dismiss()
            }
        }
    }

    fun setTitle(title: Int): BottomSheetChooseDialog {
        this.dialogTitle = title
        return this
    }

    fun addOption(title: Int, iconRes: Int? = null, onClick: () -> Unit): BottomSheetChooseDialog {
        options.add(ChooseOption(title, iconRes, onClick))
        return this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnDismissListener(listener: () -> Unit): BottomSheetChooseDialog {
        onDismissListener = listener
        return this
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    companion object {
        fun newInstance(): BottomSheetChooseDialog = BottomSheetChooseDialog()
    }
}
