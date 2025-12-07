package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.InvoiceCategoryEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import com.partsystem.partvisitapp.core.utils.FactorFormKind
import com.partsystem.partvisitapp.core.utils.SaleRateKind
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.BottomSheetChooseDialog
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getCurrentTime
import com.partsystem.partvisitapp.core.utils.extensions.getTodayGregorian
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.getGUID
import com.partsystem.partvisitapp.core.utils.persiancalendar.CalendarConstraints
import com.partsystem.partvisitapp.core.utils.persiancalendar.DateValidatorPointForward
import com.partsystem.partvisitapp.core.utils.persiancalendar.MaterialDatePicker
import com.partsystem.partvisitapp.core.utils.persiancalendar.MaterialPickerOnPositiveButtonClickListener
import com.partsystem.partvisitapp.core.utils.persiancalendar.Month
import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar
import com.partsystem.partvisitapp.databinding.FragmentHeaderOrderBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.bottomSheet.CustomerListBottomSheet
import com.partsystem.partvisitapp.feature.customer.ui.CustomerViewModel
import dagger.hilt.android.AndroidEntryPoint
import ir.huri.jcal.JalaliCalendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HeaderOrderFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var _binding: FragmentHeaderOrderBinding? = null
    private val binding get() = _binding!!

    private val headerOrderViewModel: HeaderOrderViewModel by viewModels()
    private val customerViewModel: CustomerViewModel by viewModels()
    private val factorViewModel: FactorViewModel by viewModels()

    private lateinit var patternAdapter: SpinnerAdapter
    private lateinit var customerDirectionAdapter: SpinnerAdapter
    private lateinit var invoiceCategoryAdapter: SpinnerAdapter
    private lateinit var actAdapter: SpinnerAdapter
    private lateinit var allPayementTypeAdapter: SpinnerAdapter

    // private lateinit var factor: FactorHeaderEntity
    private var controlVisit: Boolean = false
    private var userId: Int = 0
    private var visitorId: Int = 0
    private var saleCenterId: Int = 0

    data class KeyValue(val id: Int, val name: String)

    private val args: HeaderOrderFragmentArgs by navArgs()

    private val persianDate: String = getTodayPersianDate()
    private val allCustomerDirection =
        mutableListOf<CustomerDirectionEntity>() // فرض کلاس CustomerDirection

    private val allInvoiceCategory = mutableListOf<InvoiceCategoryEntity>()
    private val allPattern = mutableListOf<PatternEntity>()
    private val allSaleCenter = mutableListOf<SaleCenterEntity>()
    private val allAct = mutableListOf<ActEntity>()
    private val allPayementType = ArrayList<KeyValue>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeaderOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillControls()
        initDate()
        initCustomer()
        observeData()
        setupClicks()
        setWidth()
    }

    private fun fillControls() {

        lifecycleScope.launch {
            saleCenterId = userPreferences.saleCenterId.first() ?: 0
            controlVisit = userPreferences.controlVisitSchedule.first() ?: false
            userId = userPreferences.id.first() ?: 0
            visitorId = userPreferences.personnelId.first() ?: 0

            factorViewModel.factorHeader.value = factorViewModel.factorHeader.value!!.copy(
                uniqueId = getGUID(),
                saleCenterId = userPreferences.saleCenterId.first() ?: 0,
                settlementKind = 0,
                formKind = FactorFormKind.RegisterOrderDistribute.ordinal,
                createDate = getTodayGregorian(),
                dueDate = getTodayGregorian(),
                deliveryDate = getTodayGregorian(),
                createTime = getCurrentTime(),
                createUserId = userId,
                visitorId = visitorId,
                sabt = 0,
                isCanceled = 0,
            )

            headerOrderViewModel.fetchDefaultAnbarId(saleCenterId)

            headerOrderViewModel.defaultAnbarId.collect { anbarId ->
                if (anbarId != null) {
                    factorViewModel.factorHeader.value = factorViewModel.factorHeader.value!!.copy(
                        defaultAnbarId = anbarId,
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initDate() {
        val jalaliDate = JalaliCalendar()
        val today = "${jalaliDate.year}/${jalaliDate.month}/${jalaliDate.day}"
        binding.tvDate.text = today
        binding.tvDuoDate.text = today
        binding.tvDeliveryDate.text = today
    }

    private fun initCustomer() {
        if (args.typeCustomer && args.customerId != 0) {
            loadCustomerData(args.customerId, args.customerName)
        } else {
            val defaultList = mutableListOf(getString(R.string.label_please_select))
            val defaultAdapter = SpinnerAdapter(requireContext(), defaultList)

            binding.spCustomerDirection.adapter = defaultAdapter
            binding.spInvoiceCategory.adapter = defaultAdapter
            binding.spPattern.adapter = defaultAdapter
            binding.spAct.adapter = defaultAdapter
        }
    }

    private fun observeData() {
        // دریافت لیست مشتریان
        if (controlVisit) {

            //  با برنامه ویزیت
            customerViewModel.loadCustomersWithSchedule(persianDate)
        } else {
            //  بدون برنامه ویزیت
            customerViewModel.loadCustomersWithoutSchedule()
        }
        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            if (customers.isNotEmpty()) {
                val first = customers.first()
                factorViewModel.factorHeader.value!!.customerId = first.id
                binding.tvCustomerName.text = first.name
                loadCustomerData(first.id, first.name)
            }
        }
        binding.spCustomerDirection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when {
                        position > 0 -> {
                            val entry = allCustomerDirection[position - 1]
                            factorViewModel.factorHeader.value =
                                factorViewModel.factorHeader.value!!.copy(
                                    directionDetailId = entry.directionDetailId,
                                )
                        }

                        position == 0 -> {

                            factorViewModel.factorHeader.value =
                                factorViewModel.factorHeader.value!!.copy(
                                    directionDetailId = null,
                                )
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factorViewModel.factorHeader.value =
                        factorViewModel.factorHeader.value!!.copy(
                            directionDetailId = null,
                        )
                }
            }

        headerOrderViewModel.getInvoiceCategory(userId).observe(viewLifecycleOwner) { list ->
            allInvoiceCategory.clear()
            allInvoiceCategory.addAll(list)

            val items = mutableListOf<String>()
            items.add(getString(R.string.label_please_select))
            items.addAll(list.map { it.name })
            invoiceCategoryAdapter = SpinnerAdapter(requireContext(), items)
            binding.spInvoiceCategory.adapter = invoiceCategoryAdapter

            binding.spInvoiceCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        if (position == 0) {
                            factorViewModel.factorHeader.value =
                                factorViewModel.factorHeader.value!!.copy(
                                    invoiceCategoryId = null,
                                )
                            return
                        }

                        val selectedCategory = list[position - 1]
                        factorViewModel.factorHeader.value =
                            factorViewModel.factorHeader.value!!.copy(
                                invoiceCategoryId = selectedCategory.id,
                            )

                        // load sale center
                        headerOrderViewModel.saleCenters.observe(viewLifecycleOwner) { centers ->
                            allSaleCenter.clear()
                            allSaleCenter.addAll(centers)
                        }
                        selectedCategory?.let {
                            headerOrderViewModel.loadSaleCenters(it.id)
                        }


                        // load patterns
                        allPattern.clear()
                        if (factorViewModel.factorHeader.value?.customerId != null) {
                            headerOrderViewModel.loadPatterns(
                                factorViewModel.factorHeader.value!!.customerId!!,
                                factorViewModel.factorHeader.value!!.saleCenterId,
                                factorViewModel.factorHeader.value!!.invoiceCategoryId,
                                factorViewModel.factorHeader.value!!.settlementKind,
                                factorViewModel.factorHeader.value!!.persianDate!!
                            )
                            headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
                                allPattern.clear()
                                allPattern.addAll(list)
                                val items = list.map { it.name }.toMutableList()
                                patternAdapter = SpinnerAdapter(requireContext(), items)
                                binding.spPattern.adapter = patternAdapter


                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        factorViewModel.factorHeader.value =
                            factorViewModel.factorHeader.value!!.copy(
                                invoiceCategoryId = null,
                            )
                    }
                }
        }



        binding.spPattern.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // DataHolder.isDirty = true
                    if (position == 0 && factorViewModel.factorHeader.value!!.patternId != null) {
                        factorViewModel.factorHeader.value!!.patternId = null
                        factorViewModel.factorHeader.value!!.actId = null
                        return
                    }
                    if (allPattern.isEmpty() || position >= allPattern.size) {
                        return
                    }

                    // دریافت Pattern انتخابی
                    val entry = allPattern[position - 1]
                    factorViewModel.factorHeader.value!!.patternId = entry.id

                    headerOrderViewModel.loadProductActId(entry.id)
                    headerOrderViewModel.productActId.observe(viewLifecycleOwner) { actId ->
                        factorViewModel.factorHeader.value!!.actId = actId

                    }
                    // load act
                    headerOrderViewModel.loadActs(
                        patternId = entry.id,
                        actKind = ActKind.Product.ordinal
                    )
//                    headerOrderViewModel.acts.observe(viewLifecycleOwner) { list ->
//                        allAct.clear()
//                        allAct.addAll(list)
//                        val items = mutableListOf<String>()
//                        items.add(getString(R.string.label_please_select))
//
//                        items.addAll(list.map { it.description })
//
//                       // val items = list.mapNotNull { it.description }.toMutableList()
//                        actAdapter = SpinnerAdapter(requireContext(), items)
//                        binding.spAct.adapter = actAdapter
//                    }
                    if (factorViewModel.factorHeader.value!!.actId != null) {

                        val actId = factorViewModel.factorHeader.value!!.actId!!

                        // آیا این Act قبلاً داخل لیست وجود دارد؟
                        val exists = allAct.any { it.id == actId }

                        if (!exists) {
                            // از دیتابیس بیاورش
                            headerOrderViewModel.loadAct(actId)

                            headerOrderViewModel.addedAct.observe(viewLifecycleOwner) { act ->
                                act?.let {
                                    allAct.add(it)
                                    (binding.spAct.adapter as ArrayAdapter<ActEntity>).notifyDataSetChanged()
                                    binding.spAct.setSelection(1)
                                }
                            }

                            fillPaymentType()

                        } else {
                            binding.spAct.setSelection(1)
                        }
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factorViewModel.factorHeader.value!!.patternId = null
                    factorViewModel.factorHeader.value!!.actId = null
                }
            }

        headerOrderViewModel.getAct().observe(viewLifecycleOwner) { list ->

            val items = mutableListOf<String>()
            items.add(getString(R.string.label_please_select))
            items.addAll(list.map { it.description!! })
            actAdapter = SpinnerAdapter(requireContext(), items)
            binding.spAct.adapter = actAdapter


        }

        binding.spAct.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when {
                        position > 0 -> {
                            val entry = allAct[position - 1]
                            factorViewModel.factorHeader.value =
                                factorViewModel.factorHeader.value!!.copy(
                                    actId = entry.id,
                                )
                        }

                        position == 0 -> {

                            factorViewModel.factorHeader.value =
                                factorViewModel.factorHeader.value!!.copy(
                                    actId = null,
                                )
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factorViewModel.factorHeader.value =
                        factorViewModel.factorHeader.value!!.copy(
                            actId = null,
                        )
                }
            }
        fillPaymentType()

        binding.spPaymentType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val entry = allPayementType[position].id
                    factorViewModel.factorHeader.value =
                        factorViewModel.factorHeader.value!!.copy(
                            settlementKind = entry,
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factorViewModel.factorHeader.value =
                        factorViewModel.factorHeader.value!!.copy(
                            settlementKind = 0,
                        )
                }
            }
    }

    private fun setupClicks() {

        binding.hfHeaderOrder.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        // ------------------------- Date Picker Base Config -------------------------
        val calendar = PersianCalendar().apply {
            setPersian(1340, Month.FARVARDIN, 1)
        }
        val start = calendar.timeInMillis

        calendar.setPersian(1409, Month.ESFAND, 29)
        val end = calendar.timeInMillis

        val openAt = PersianCalendar.getToday().timeInMillis

        val constraints =
            CalendarConstraints.Builder().setStart(start).setEnd(end).setOpenAt(openAt)
                .setValidator(DateValidatorPointForward.from(start)).build()

        // ------------------------- Helper: Create & Handle Date Picker -------------------------
        fun showPersianDatePicker(onDateSelected: (String) -> Unit) {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.label_choose)).setCalendarConstraints(constraints)
                .build()

            picker.addOnPositiveButtonClickListener(object :
                MaterialPickerOnPositiveButtonClickListener<Long?> {
                override fun onPositiveButtonClick(selection: Long?) {
                    selection?.let {
                        val date = PersianCalendar(it).toString()
                        onDateSelected(date)
                    }
                }
            })
            picker.show(parentFragmentManager, "DatePickerTag")
        }

        // ------------------------- Click Listeners -------------------------
        binding.cvDate.setOnClickListener {
            showPersianDatePicker { date ->
                binding.tvDate.text = date
            }
        }

        binding.cvDuoDate.setOnClickListener {
            showPersianDatePicker { date ->
                binding.tvDuoDate.text = date
            }
        }

        binding.cvDeliveryDate.setOnClickListener {
            showPersianDatePicker { date ->
                binding.tvDeliveryDate.text = date
            }
        }

        binding.cvCustomer.setOnClickListener {
            rotateArrow(true)
            CustomerListBottomSheet.newInstance {
                rotateArrow(false)
            }.show(parentFragmentManager, "CustomerListBottomSheet")
        }

        binding.btnContinue.setOnClickBtnOneListener {
            factorViewModel.factorHeader.value = factorViewModel.factorHeader.value!!.copy(
                // customerId = selectedCustomerId,
                description = binding.etDescription.text.toString(),
                createDate = "2025-11-30"
            )
            showChooseDialog()

            // validateHeader()

        }

        // ------------------------- Listen to Customer Selection -------------------------

        parentFragmentManager.setFragmentResultListener(
            CustomerListBottomSheet.REQ_CLICK_ITEM, viewLifecycleOwner
        ) { _, bundle ->
            val customerName = bundle.getString(CustomerListBottomSheet.ARG_CUSTOMER_NAME)
            val customerId = bundle.getInt(CustomerListBottomSheet.ARG_CUSTOMER_ID)
            loadCustomerData(customerId, customerName!!)
        }
    }

    private fun validateHeader() {

        val factor = factorViewModel.factorHeader.value ?: return

        // خطای دسته‌بندی فاکتور (خارج از ViewModel)
        if (factor.invoiceCategoryId == null) {
            CustomSnackBar.make(
                requireView(),
                getString(R.string.error_selecting_invoice_category_mandatory),
                SnackBarType.Error.value
            )?.show()
            return
        }

        // اجرای اعتبارسنجی ViewModel
        headerOrderViewModel.validateHeader(
            saleCenterId = saleCenterId,
            factor = factor
        )

        // دریافت نتیجه اعتبارسنجی
        headerOrderViewModel.validationResult.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                showChooseDialog()
            }
        }

        // دریافت پیام خطا و نمایش با CustomSnackBar
        headerOrderViewModel.errorMessageRes.observe(viewLifecycleOwner) { msgResId ->
            msgResId?.let {
                CustomSnackBar.make(
                    requireView(),
                    getString(it),
                    SnackBarType.Error.value
                )?.show()
            }
        }
    }

    private fun loadCustomerData(customerId: Int, customerName: String) {
        binding.tvCustomerName.text = customerName
        factorViewModel.factorHeader.value!!.customerId = customerId

        headerOrderViewModel.assignDirection.observe(viewLifecycleOwner) { factor ->
            factor?.let {
                factorViewModel.factorHeader.value = factorViewModel.factorHeader.value!!.copy(
                    distributorId = factor.distributorId,
                    recipientId = factor.recipientId,
                    customerId = customerId,
                )
            }
        }

        headerOrderViewModel.loadAssignDirectionCustomerByCustomerId(customerId)

        // بارگذاری مسیرهای مشتری
        headerOrderViewModel.getCustomerDirectionsByCustomer(customerId)
            .observe(viewLifecycleOwner) { list ->
                allCustomerDirection.clear()
                allCustomerDirection.addAll(list)
                val items = mutableListOf<String>()
                items.add(getString(R.string.label_please_select))

                //val items = list.map { it.fullAddress }.toMutableList()
                items.addAll(list.map { it.fullAddress })

                customerDirectionAdapter = SpinnerAdapter(requireContext(), items)
                binding.spCustomerDirection.adapter = customerDirectionAdapter
            }

        // بارگذاری الگوها
        headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
            allPattern.clear()
            allPattern.addAll(list)
            val items = list.map { it.name }.toMutableList()
            patternAdapter = SpinnerAdapter(requireContext(), items)
            binding.spPattern.adapter = patternAdapter
        }

        headerOrderViewModel.loadPatterns(
            customer = customerId,
            centerId = saleCenterId,
            invoiceCategoryId = 0,
            settlementKind = 0,
            date = getTodayPersianDate()
        )
    }


    private fun fillPaymentType() {
        allPayementType.clear()
        val items = allPayementType.map { it.name }.toMutableList()
        allPayementTypeAdapter = SpinnerAdapter(requireContext(), items)
        binding.spPaymentType.adapter = allPayementTypeAdapter

        if (factorViewModel.factorHeader.value!!.patternId != null) {
            headerOrderViewModel.loadPatternById(factorViewModel.factorHeader.value!!.patternId!!)

        }
        headerOrderViewModel.pattern.observe(viewLifecycleOwner) { pattern ->
            if (pattern == null || pattern.hasCash) allPayementType.add(KeyValue(0, "نقدی"))
            if (pattern == null || pattern.hasMaturityCash) allPayementType.add(
                KeyValue(
                    1,
                    "نقدی در سررسید"
                )
            )
            if (pattern == null || pattern.hasSanadAndCash) allPayementType.add(
                KeyValue(
                    2,
                    "نقد و اسناد"
                )
            )
            if (pattern == null || pattern.hasSanad) allPayementType.add(KeyValue(3, "اسناد"))
            if (pattern == null || pattern.hasCredit) allPayementType.add(
                KeyValue(
                    4,
                    "اعتباری"
                )
            )
        }


    }

    private fun showChooseDialog() {
        BottomSheetChooseDialog.newInstance().setTitle(R.string.label_choose)
            .addOption(R.string.label_product_catalog, R.drawable.ic_home_catalog) {
                val action =
                    HeaderOrderFragmentDirections.actionHeaderOrderFragmentToProductListFragment(
                        true
                    )
                findNavController().navigate(action)
            }.addOption(R.string.label_product_group, R.drawable.ic_home_group_product) {
                val action =
                    HeaderOrderFragmentDirections.actionHeaderOrderFragmentToGroupProductFragment(
                        true
                    )
                findNavController().navigate(action)
            }.show(childFragmentManager, "chooseDialog")
    }

    private fun setWidth() {

        binding.spCustomerDirection.post {
            val margin =
                resources.getDimensionPixelSize(R.dimen.big_size)
            val dropDownWidth = binding.cvCustomerDirection.width - margin
            binding.spCustomerDirection.dropDownWidth = dropDownWidth
        }
        binding.spInvoiceCategory.post {
            val margin =
                resources.getDimensionPixelSize(R.dimen.big_size)
            val dropDownWidth = binding.cvInvoiceCategory.width - margin
            binding.spInvoiceCategory.dropDownWidth = dropDownWidth
        }
        binding.spPattern.post {
            val margin =
                resources.getDimensionPixelSize(R.dimen.big_size)
            val dropDownWidth = binding.cvPattern.width - margin
            binding.spPattern.dropDownWidth = dropDownWidth
        }
        binding.spAct.post {
            val margin =
                resources.getDimensionPixelSize(R.dimen.big_size)
            val dropDownWidth = binding.cvAct.width - margin
            binding.spAct.dropDownWidth = dropDownWidth
        }
        binding.spPaymentType.post {
            val margin =
                resources.getDimensionPixelSize(R.dimen.big_size)
            val dropDownWidth = binding.cvPaymentType.width - margin
            binding.spPaymentType.dropDownWidth = dropDownWidth
        }

    }

    private fun rotateArrow(isExpanded: Boolean) {
        val rotation = if (isExpanded) 180f else 0f
        binding.ivCustomerName.animate().rotation(rotation).setDuration(200).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}