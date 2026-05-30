package com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.CalendarConstraints
import com.partsystem.partvisitapp.core.utils.persiancalendar.DateValidatorPointForward
import com.partsystem.partvisitapp.core.utils.persiancalendar.MaterialDatePicker
import com.partsystem.partvisitapp.core.utils.persiancalendar.MaterialPickerOnPositiveButtonClickListener
import com.partsystem.partvisitapp.core.utils.persiancalendar.Month
import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.convertPersianDigitsToLatin
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.BottomSheetFilterOrderBinding
import com.partsystem.partvisitapp.feature.customer.bottomSheet.CustomerListBottomSheet
import com.partsystem.partvisitapp.feature.customer.bottomSheet.adapter.CustomerBottomSheetAdapter
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import com.partsystem.partvisitapp.feature.report_factor.online.bottomSheet.adapter.DirectionBottomSheetAdapter
import com.partsystem.partvisitapp.feature.report_factor.online.ui.OnlineOrderListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("UseCompatLoadingForDrawables")
class FilterOrderBottomSheet(
    private val onDismissCallback: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var customerId = 0
    private var customerName = ""

    private var directionCode = ""
    private var directionName = ""

    private var condition = ""

    @Inject
    lateinit var mainPreferences: MainPreferences
    private val customerViewModel: CustomerViewModel by viewModels()

    private var _binding: BottomSheetFilterOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var customerBottomSheetAdapter: CustomerBottomSheetAdapter
    private lateinit var directionBottomSheetAdapter: DirectionBottomSheetAdapter
    private var listener: OnFilterAppliedListener? = null
    private var isCustomerSheetOpen = false
    private var isDirectionSheetOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupClicks()
        initAdapter()
        loadFilterState()
    }

    fun setOnFilterAppliedListener(listener: OnFilterAppliedListener) {
        this.listener = listener
    }

    // --------------------- Load Stored Filters ---------------------
    private fun loadFilterState() = lifecycleScope.launch {
        binding.apply {

            val savedFromDate = mainPreferences.fromDate.firstOrNull() ?: ""
            val savedToDate = mainPreferences.toDate.firstOrNull() ?: ""
            val savedCustomerName = mainPreferences.customerName.firstOrNull() ?: ""
            val savedCustomerId = mainPreferences.customerId.firstOrNull() ?: 0
            val savedDirectionCodes = mainPreferences.directionCodes.firstOrNull() ?: ""
            val savedDirectionNames = mainPreferences.directionNames.firstOrNull() ?: ""


            // Assign memory values
            customerId = savedCustomerId
            customerName = savedCustomerName
            directionCode = savedDirectionCodes
            directionName = savedDirectionNames

            updateCustomerUI(customerId, customerName)
            updateDateUI(savedFromDate, savedToDate)
            updateDirectionUI(savedDirectionCodes, savedDirectionNames)
        }
    }

    // --------------------- UI Update Helpers ---------------------
    private fun updateCustomerUI(id: Int, name: String) = binding.apply {
        if (id == 0) {
            tvCustomerName.text = getString(R.string.label_please_select)
            ivCustomerName.show()
            ivClearCustomer.gone()
            clearCard(cvCustomer)
        } else {
            tvCustomerName.text = name
            ivCustomerName.gone()
            ivClearCustomer.show()
            highlightCard(cvCustomer)
        }
    }

    private fun updateDateUI(from: String, to: String) = binding.apply {
        val today = getTodayPersianDate()

        val finalFrom = if (from.isBlank()) today else from
        val finalTo = if (to.isBlank()) today else to

        tvFromDate.text = finalFrom
        tvToDate.text = finalTo

        if (finalFrom == today) clearCard(cvFromDate) else highlightCard(cvFromDate)
        if (finalTo == today) clearCard(cvToDate) else highlightCard(cvToDate)
    }


    private fun updateDirectionUI(code: String, names: String, count: Int = -1) = binding.apply {
        if (code.isBlank()) {
            tvDirectionName.text = getString(R.string.label_please_select)
            ivDirectionName.show()
            ivClearDirection.gone()
            clearCard(cvDirection)
            return@apply
        }

        val title = if (count == 1) names else if (count > 1) "$count مسیر"
        else {
            val list = names.split(",").filter { it.isNotBlank() }
            if (list.size == 1) list[0] else "${list.size} مسیر"
        }

        tvDirectionName.text = title
        ivDirectionName.gone()
        ivClearDirection.show()
        highlightCard(cvDirection)
    }

    private fun highlightCard(card: View) {
        card.backgroundTintList = null
        if (card is com.google.android.material.card.MaterialCardView) {
            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.red_FF0E0E)
            card.strokeWidth = resources.getDimensionPixelSize(R.dimen.bit_size)
        }
    }

    private fun clearCard(card: View) {
        if (card is com.google.android.material.card.MaterialCardView) {
            card.strokeColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            card.strokeWidth = 0
        }
    }

    // --------------------- Setup Clicks ---------------------
    private fun setupClicks() = binding.apply {

        ivBack.setOnClickListener { dismiss() }

        setupDatePickerClicks()
        setupCustomerClicks()
        setupDirectionClicks()

        btnApplyFilter.setOnClickListener {
            applyFilters()
        }

        btnDeleteFilter.setOnClickListener {
            clearAllFilters()
        }

        listenCustomerSelection()
        listenDirectionSelection()
    }

    // --------------------- Listener: Customer ---------------------
    private fun listenCustomerSelection() {
        parentFragmentManager.setFragmentResultListener(
            CustomerListBottomSheet.REQ_CLICK_CUSTOMER, viewLifecycleOwner
        ) { _, b ->
            val name = b.getString(CustomerListBottomSheet.ARG_CUSTOMER_NAME).orEmpty()
            val id = b.getInt(CustomerListBottomSheet.ARG_CUSTOMER_ID, 0)

            customerId = id
            customerName = name

            updateCustomerUI(id, name)
        }
    }

    // --------------------- Listener: Direction ---------------------
    private fun listenDirectionSelection() {
        parentFragmentManager.setFragmentResultListener(
            DirectionListBottomSheet.REQ_CLICK_DIRECTION, viewLifecycleOwner
        ) { _, b ->

            directionName = b.getString("direction_names") ?: ""
            directionCode = b.getString("direction_codes") ?: ""
            val count = b.getInt("direction_count", 0)

            if (count == 0) {
                updateDirectionUI("", "")
                return@setFragmentResultListener
            }

            updateDirectionUI(directionCode, directionName, count)
        }
    }

    // --------------------- Date Picker Config ---------------------
    private fun setupDatePickerClicks() = binding.apply {

        val calendar = PersianCalendar().apply {
            setPersian(1350, Month.FARVARDIN, 1)
        }
        val start = calendar.timeInMillis

        calendar.setPersian(1420, Month.ESFAND, 29)
        val end = calendar.timeInMillis

        val openAt = PersianCalendar.getToday().timeInMillis

        val constraints =
            CalendarConstraints.Builder().setStart(start).setEnd(end).setOpenAt(openAt)
                .setValidator(DateValidatorPointForward.from(start)).build()

        // ------------------------- Helper: Create & Handle Date Picker -------------------------
        fun showPersianDatePicker(onDateSelected: (String) -> Unit) {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.label_choose))
                .setCalendarConstraints(constraints)
                .build()

            picker.addOnPositiveButtonClickListener(object :
                MaterialPickerOnPositiveButtonClickListener<Long?> {
                @SuppressLint("DefaultLocale")
                override fun onPositiveButtonClick(selection: Long?) {
                    selection?.let {
                        val date = PersianCalendar(it)
                        val year = date.year
                        val month = date.month + 1
                        val day = date.day

                        val dateFinal = String.format(
                            "%04d/%02d/%02d",
                            year,
                            month,
                            day
                        )
                        onDateSelected(dateFinal)
                    }
                }
            })
            picker.show(parentFragmentManager, "DatePickerTag")
        }

        cvFromDate.setOnClickListener {
            showPersianDatePicker { date ->
                val today = getTodayPersianDate()

                binding.tvFromDate.text = date

                if (date == today)
                    clearCard(binding.cvFromDate)
                else
                    highlightCard(binding.cvFromDate)
            }
        }

        cvToDate.setOnClickListener {
            showPersianDatePicker { date ->
                val today = getTodayPersianDate()

                binding.tvToDate.text = date

                if (date == today)
                    clearCard(binding.cvToDate)
                else
                    highlightCard(binding.cvToDate)
            }
        }
    }


    // --------------------- Customer Clicks ---------------------
    private fun setupCustomerClicks() = binding.apply {
        ivClearCustomer.setOnClickListener {
            customerId = 0
            customerName = ""

            updateCustomerUI(0, "")
        }

        cvCustomer.setOnClickListener {
            if (isCustomerSheetOpen) return@setOnClickListener

            isCustomerSheetOpen = true
            rotateIcon(binding.ivCustomerName, true)

            CustomerListBottomSheet.newInstance {
                isCustomerSheetOpen = false
                rotateIcon(binding.ivCustomerName, false)
            }.show(parentFragmentManager, "CustomerSheet")
        }
    }

    // --------------------- Direction Clicks ---------------------
    private fun setupDirectionClicks() = binding.apply {
        ivClearDirection.setOnClickListener {

            directionCode = ""
            directionName = ""
            updateDirectionUI("", "")

            lifecycleScope.launch {
                mainPreferences.clearDirectionFilter()
                mainPreferences.setDirectionCleared(true)
            }
        }

        cvDirection.setOnClickListener {
            if (isDirectionSheetOpen) return@setOnClickListener

            isDirectionSheetOpen = true
            rotateIcon(binding.ivDirectionName, true)

            lifecycleScope.launch {
                customerViewModel.ensureDirectionsLoaded()
                DirectionListBottomSheet
                    .newInstance(customerViewModel.filteredDirections.value) {
                        isDirectionSheetOpen = false
                        rotateIcon(binding.ivDirectionName, false)
                    }
                    .show(parentFragmentManager, "DirectionSheet")
            }
        }
    }

    private fun rotateIcon(view: View, expanded: Boolean) {
        view.animate().rotation(if (expanded) 180f else 0f).setDuration(200).start()
    }

    fun onBottomSheetDismissed() {
        isCustomerSheetOpen = false
        isDirectionSheetOpen = false
    }

    // --------------------- Apply Filters ---------------------
    private fun applyFilters() = lifecycleScope.launch {

        val fromDate = binding.tvFromDate.text.toString()
        val toDate = binding.tvToDate.text.toString()
        val today = getTodayPersianDate()

        val fromDateFinal = convertPersianDigitsToLatin(fromDate)
        val toDateFinal = convertPersianDigitsToLatin(toDate)

        val conditionBuilder = StringBuilder()

        if (customerId != 0)
            conditionBuilder.append(" AND CustomerId=$customerId")

        val isDateChanged = fromDate != today || toDate != today

        if (isDateChanged)
            conditionBuilder.append(" AND PersianDate >= '$fromDateFinal' AND PersianDate <= '$toDateFinal'")

        if (directionCode.isNotEmpty())
            conditionBuilder.append(" AND DirectionCode IN ($directionCode)")

        condition = conditionBuilder.toString()

        if (condition.isBlank()) {
            mainPreferences.saveFilterConditionInfo("", "", "", 0, "")
            mainPreferences.clearDirectionFilter()
            listener?.onFilterCleared()
        } else {

            val finalFromToSave = if (isDateChanged) fromDateFinal else ""
            val finalToToSave = if (isDateChanged) toDateFinal else ""

            mainPreferences.saveFilterConditionInfo(
                filterCondition = condition,
                fromDate = finalFromToSave,
                toDate = finalToToSave,
                customerId = customerId,
                customerName = customerName
            )

            if (directionCode.isNotEmpty())
                mainPreferences.saveDirectionFilter(directionCode, directionName)
            else {
                mainPreferences.clearDirectionFilter()
                mainPreferences.setDirectionCleared(true)
            }

            listener?.onFilterApplied()
        }

        dismiss()
    }

    // --------------------- Full Clear ---------------------
    private fun clearAllFilters() = lifecycleScope.launch {
        mainPreferences.clearFilterConditionInfo()
        mainPreferences.clearDirectionFilter()
        listener?.onFilterCleared()
        dismiss()
    }

    private fun initAdapter() {
        customerBottomSheetAdapter = CustomerBottomSheetAdapter(
            onClick = { customer ->
                val result = Bundle().apply {
                    putInt(ARG_CUSTOMER_ID, customer.id)
                }
                parentFragmentManager.setFragmentResult(REQ_CLICK_CUSTOMER, result)
                dismiss()
            }
        )

        directionBottomSheetAdapter = DirectionBottomSheetAdapter {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        (parentFragment as? OnlineOrderListFragment)?.onBottomSheetDismissed()
    }

    interface OnFilterAppliedListener {
        fun onFilterApplied()
        fun onFilterCleared()
    }

    companion object {
        const val REQ_CLICK_CUSTOMER = "click_customer_request"
        const val REQ_CLICK_DIRECTION = "click_direction_request"
        const val ARG_CUSTOMER_ID = "customerId"
        fun newInstance(onDismiss: (() -> Unit)? = null) =
            FilterOrderBottomSheet(onDismiss)
    }
}
