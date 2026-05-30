package com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.BottomSheetDirectionListBinding
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet.adapter.DirectionBottomSheetAdapter
import com.partsystem.partvisitapp.feature.report_factor.online.model.DirectionModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("UseCompatLoadingForDrawables")
class DirectionListBottomSheet(
    private val initialList: List<DirectionModel>,
    private val onDismissCallback: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private val customerViewModel: CustomerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: BottomSheetDirectionListBinding? = null
    private val binding get() = _binding!!

    private lateinit var directionBottomSheetAdapter: DirectionBottomSheetAdapter

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDirectionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
        setupClearIcon()
        initRecycler()
        observeDirections()
        showInitialData()
        setupSearch()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true

        val params = bottomSheet.layoutParams
        params.height = (resources.displayMetrics.heightPixels * 0.9).toInt()
        bottomSheet.layoutParams = params
    }

    private fun setupClicks() = binding.apply {
        ivBack.setOnClickListener {
            dismiss()
        }

        btnApplyDirection.setOnClickListener { applySelectedDirections() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearIcon() = binding.apply {
        // پاک کردن جستجو با لمس آیکون ضربدر
        etSearch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd =
                    etSearch.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

                val touchAreaStart =
                    etSearch.width - etSearch.paddingEnd - drawableEnd.intrinsicWidth

                if (event.x >= touchAreaStart) {
                    etSearch.text?.clear()
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun initRecycler() = binding.apply {
        directionBottomSheetAdapter = DirectionBottomSheetAdapter { }

        rvList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = directionBottomSheetAdapter
        }
    }

    // Apply result
    // -----------------------------
    private fun applySelectedDirections() {

        val selected = directionBottomSheetAdapter.getSelectedDirections()

        if (selected.isEmpty()) {
            CustomSnackBar.make(
                requireView(),
                getString(R.string.msg_select_direction),
                SnackBarType.Error.value
            )?.show()
            return
        }
        val names = selected.joinToString(",") { it.directionName }
        val codes = selected.joinToString(",") { it.directionCode }

        val result = Bundle().apply {
            putString("direction_names", names)
            putString("direction_codes", codes)
            putInt("direction_count", selected.size)
        }

        lifecycleScope.launch {
            mainPreferences.saveDirectionFilter(codes, names)
            mainPreferences.setDirectionCleared(false)
        }

        parentFragmentManager.setFragmentResult(
            FilterOrderBottomSheet.REQ_CLICK_DIRECTION,
            result
        )

        dismiss()
    }

    private fun showInitialData() = applyList(initialList)

    // Observe
    // -----------------------------
    private fun observeDirections() {
        viewLifecycleOwner.lifecycleScope.launch {

            val savedCodes = mainPreferences.directionCodes.firstOrNull()?.split(",")
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            // ابتدا لیست را نمایش بده
            applyList(customerViewModel.filteredDirections.value)

            // سپس انتخاب‌های ذخیره‌شده را تیک بزن
            if (savedCodes.isNotEmpty()) {
                directionBottomSheetAdapter.preselect(savedCodes)
            }

            // سپس تغییرات سرچ را گوش کن
            customerViewModel.filteredDirections.collect { list ->
                applyList(list)

                // بعد از هر فیلتر، مجدداً انتخاب‌های قبلی را اعمال کن
                if (savedCodes.isNotEmpty()) {
                    directionBottomSheetAdapter.preselect(savedCodes)
                }
            }
        }
    }

    private fun applyList(list: List<DirectionModel>) = with(binding) {
        if (list.isEmpty()) {
            info.show()
            info.message(getString(R.string.msg_no_direction))
            rvList.hide()
        } else {
            info.gone()
            rvList.show()
            directionBottomSheetAdapter.setData(list)
        }
    }

    private fun setupSearch() = binding.apply {
        etSearch.addTextChangedListener { editable ->
            val query = convertNumbersToEnglish(fixPersianChars(editable.toString()))
            etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null,
                if (query.isEmpty()) searchIcon else clearIcon,
                null
            )
            customerViewModel.filterCustomerDirections(query)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        (parentFragment as? FilterOrderBottomSheet)?.onBottomSheetDismissed()
    }

    companion object {
        const val REQ_CLICK_DIRECTION = "click_direction_request"
        fun newInstance(list: List<DirectionModel>, onDismiss: (() -> Unit)? = null) =
            DirectionListBottomSheet(list, onDismiss)
    }
}
