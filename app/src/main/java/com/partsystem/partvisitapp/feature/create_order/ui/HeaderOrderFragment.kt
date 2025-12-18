package com.partsystem.partvisitapp.feature.create_order.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Observer
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
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.BottomSheetChooseDialog
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getCurrentTime
import com.partsystem.partvisitapp.core.utils.extensions.getTodayGregorian
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
import com.partsystem.partvisitapp.core.utils.extensions.gregorianToPersian
import com.partsystem.partvisitapp.core.utils.extensions.persianToGregorian
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
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private val allCustomerDirection = mutableListOf<CustomerDirectionEntity>()
    private val allInvoiceCategory = mutableListOf<InvoiceCategoryEntity>()
    private val allPattern = mutableListOf<PatternEntity>()
    private val allSaleCenter = mutableListOf<SaleCenterEntity>()
    private val allAct = mutableListOf<ActEntity>()
    private val allPaymentType = ArrayList<KeyValue>()

    private var controlVisit: Boolean = false
    private var userId: Int = 0
    private var visitorId: Int = 0
    private var saleCenterId: Int = 0
    private var pendingNavigation: String = ""
    private var isBottomSheetShowing = false

    private val isEditMode get() = args.factorId > 0
    private var editingHeader: FactorHeaderEntity? = null

    data class KeyValue(val id: Int, val name: String)

    private val args: HeaderOrderFragmentArgs by navArgs()
    private val persianDate: String = getTodayPersianDate()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeaderOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initCustomer()
        setupSpinners()
        observeData()

        if (!factorViewModel.enteredProductPage) {
            // ورود اولیه
            factorViewModel.resetHeader()
        }
        if (factorViewModel.factorHeader.value?.createDate == null) {
            factorViewModel.setDefaultDates()
        }

        if (isEditMode) {
            loadEditData()
        } else {
            createNewHeader()
        }
        setupClicks()
        setWidth()
    }

    private fun initAdapter() {
        val defaultAdapter =
            SpinnerAdapter(requireContext(), mutableListOf(getString(R.string.label_please_select)))
        binding.spInvoiceCategory.adapter = defaultAdapter
        binding.spPattern.adapter = defaultAdapter
        binding.spAct.adapter = defaultAdapter
        binding.spCustomerDirection.adapter = defaultAdapter
    }

    private fun initCustomer() {
        if (args.typeCustomer && args.customerId != 0) {
            loadCustomerData(args.customerId, args.customerName)
        }
    }

    private fun setupSpinners() {
        binding.apply {
            cvCustomerDirection.setOnClickListener {
                spCustomerDirection.performClick()
            }
            cvInvoiceCategory.setOnClickListener {
                spInvoiceCategory.performClick()
            }
            cvPattern.setOnClickListener {
                spPattern.performClick()
            }
            cvAct.setOnClickListener {
                spAct.performClick()
            }
            cvPaymentType.setOnClickListener {
                spPaymentType.performClick()
            }
            spCustomerDirection.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when {
                            position > 0 -> {
                                val selectedCustomerDirection = allCustomerDirection[position - 1]
                                factorViewModel.updateHeader(directionDetailId = selectedCustomerDirection.directionDetailId)
                            }

                            position == 0 -> {
                                factorViewModel.updateHeader(directionDetailId = null)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        factorViewModel.updateHeader(directionDetailId = null)
                    }
                }

            spInvoiceCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        if (position == 0) {
                            Log.d("DEBUG", "Setting invoiceCategoryId to NULL")
                            factorViewModel.updateHeader(invoiceCategoryId = null)
                            return
                        }

                        val selectedCategory = allInvoiceCategory[position - 1]

                        factorViewModel.updateHeader(
                            invoiceCategoryId = selectedCategory.id
                        )

                        // load sale centers
                        headerOrderViewModel.loadSaleCenters(selectedCategory.id)

                        val header = factorViewModel.factorHeader.value ?: return

                        if (header.customerId != null && header.persianDate != null) {
                            headerOrderViewModel.loadPatterns(
                                customer = header.customerId!!,
                                centerId = saleCenterId,
                                invoiceCategoryId = selectedCategory.id,
                                settlementKind = 0,
                                date = header.persianDate!!
                            )
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        factorViewModel.updateHeader(invoiceCategoryId = null)
                    }
                }

            spPattern.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        if (position == 0) {
                            factorViewModel.updateHeader(patternId = null, actId = null)
                            fillPaymentType(null)
                            return
                        }
                        val selectedPattern = allPattern[position - 1]
                        factorViewModel.updateHeader(patternId = selectedPattern.id)

                        // val selectedPattern = allPattern.getOrNull(position - 1) ?: return
                        // factorViewModel.factorHeader.value?.patternId = selectedPattern.id

                        // load product ActId
                        headerOrderViewModel.loadProductActId(selectedPattern.id)

                        // load acts
                        headerOrderViewModel.loadActs(
                            patternId = selectedPattern.id,
                            actKind = ActKind.Product.ordinal
                        )
                        fillPaymentType(selectedPattern)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        factorViewModel.updateHeader(patternId = null, actId = null)
                        fillPaymentType(null)
                    }
                }

            spAct.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when {
                            position > 0 -> {
                                val act = allAct[position - 1]
                                factorViewModel.updateHeader(actId = act.id)
                                Log.d("factorHeaderctId1", act.id!!.toString())

                                // fillPaymentType()
                            }

                            position == 0 -> {
                                factorViewModel.updateHeader(actId = null)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        factorViewModel.updateHeader(actId = null)
                    }
                }
        }
    }

    private fun createNewHeader() {

        lifecycleScope.launch {

            val current = factorViewModel.factorHeader.value

            // فقط اگر هدر واقعاً جدید است
            if (current?.uniqueId == null) {

                saleCenterId = userPreferences.saleCenterId.first() ?: 0
                controlVisit = userPreferences.controlVisitSchedule.first() ?: false
                userId = userPreferences.id.first() ?: 0
                visitorId = userPreferences.personnelId.first() ?: 0

                factorViewModel.factorHeader.value = current?.copy(
                    uniqueId = getGUID(),
                    saleCenterId = saleCenterId,
                    settlementKind = 0,
                    formKind = FactorFormKind.RegisterOrderDistribute.ordinal,
                    createDate = getTodayGregorian(),
                    persianDate = getTodayPersianDate(),
                    dueDate = getTodayGregorian(),
                    deliveryDate = getTodayGregorian(),
                    createTime = getCurrentTime(),
                    createUserId = userId,
                    visitorId = visitorId,
                    sabt = 0,
                    isCanceled = 0,
                ) ?: FactorHeaderEntity(
                    uniqueId = getGUID(),
                    saleCenterId = saleCenterId,
                    settlementKind = 0,
                    formKind = FactorFormKind.RegisterOrderDistribute.ordinal,
                    createDate = getTodayGregorian(),
                    persianDate = getTodayPersianDate(),
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
                        factorViewModel.updateHeader(defaultAnbarId = anbarId)
                    }
                }
            }
        }

    }

    private fun loadCustomerData(customerId: Int, customerName: String) {
        binding.tvCustomerName.text = customerName
        factorViewModel.updateHeader(customerId = customerId)
        headerOrderViewModel.assignDirection.observe(viewLifecycleOwner) { factor ->
            factor?.let {
                factorViewModel.factorHeader.value = factorViewModel.factorHeader.value!!.copy(
                    distributorId = factor.distributorId,
                    recipientId = factor.recipientId,
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

                items.addAll(list.map { it.fullAddress })
                binding.spCustomerDirection.adapter =
                    SpinnerAdapter(requireContext(), items)

                factorViewModel.factorHeader.value?.directionDetailId?.let { id ->
                    binding.spCustomerDirection.setSelectionById(
                        id,
                        allCustomerDirection
                    ) { it.directionDetailId }
                }
                if (isEditMode) {
                    Log.d("isEditModedirectionDetailId", "ok")

                    editingHeader?.directionDetailId?.let { id ->
                        binding.spCustomerDirection.setSelectionById(
                            id = id,
                            items = allCustomerDirection
                        ) { it.id }
                    }
                }
            }

        // بارگذاری الگوها
        headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
            allPattern.clear()
            allPattern.addAll(list)

            val items = mutableListOf(getString(R.string.label_please_select))
            items.addAll(list.map { it.name })

            binding.spPattern.adapter =
                SpinnerAdapter(requireContext(), items)
            // مقدار انتخاب‌شده را ست کن
            factorViewModel.factorHeader.value?.patternId?.let { id ->
                binding.spPattern.setSelectionById(id, allPattern) { it.id }
            }
            if (isEditMode) {
                Log.d("isEditModepatternId", "ok")

                editingHeader?.patternId?.let { id ->
                    binding.spPattern.setSelectionById(
                        id = id,
                        items = allPattern
                    ) { it.id }
                }
            }
        }

        headerOrderViewModel.loadPatterns(
            customer = customerId,
            centerId = saleCenterId,
            invoiceCategoryId = factorViewModel.factorHeader.value!!.invoiceCategoryId,
            settlementKind = 0,
            date = getTodayPersianDate()
        )
    }

    private fun observeData() {
        factorViewModel.factorHeader.observe(viewLifecycleOwner) { header ->
            header.createDate?.let { binding.tvDate.text = gregorianToPersian(it) }
            header.dueDate?.let { binding.tvDuoDate.text = gregorianToPersian(it) }
            header.deliveryDate?.let { binding.tvDeliveryDate.text = gregorianToPersian(it) }
        }

        // دریافت لیست مشتریان
        if (controlVisit) {

            //  با برنامه ویزیت
            customerViewModel.loadCustomersWithSchedule(persianDate)
        } else {
            //  بدون برنامه ویزیت
            customerViewModel.loadCustomersWithoutSchedule()
        }
        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            if (isEditMode) return@observe // در حالت ویرایش، این قسمت اجرا نشود

            if (customers.isNotEmpty()) {
                val first = customers.first()
                factorViewModel.updateHeader(customerId = first.id)
                binding.tvCustomerName.text = first.name
                loadCustomerData(first.id, first.name)
            }
        }

        headerOrderViewModel.getInvoiceCategory(userId)
            .observe(viewLifecycleOwner) { list ->
                allInvoiceCategory.clear()
                allInvoiceCategory.addAll(list)

                val items = mutableListOf(getString(R.string.label_please_select))
                items.addAll(list.map { it.name })

                binding.spInvoiceCategory.adapter =
                    SpinnerAdapter(requireContext(), items)

                factorViewModel.factorHeader.value?.invoiceCategoryId?.let { id ->
                    binding.spInvoiceCategory.setSelectionById(
                        id,
                        allInvoiceCategory
                    ) { it.id }
                }
                // در حالت ویرایش
                if (isEditMode) {
                    Log.d("isEditModeinvoiceCategoryId", "ok")

                    editingHeader?.invoiceCategoryId?.let { id ->
                        binding.spInvoiceCategory.setSelectionById(
                            id = id,
                            items = allInvoiceCategory
                        ) { it.id }
                    }

                }
            }

        headerOrderViewModel.saleCenters.observe(viewLifecycleOwner) { centers ->
            allSaleCenter.clear()
            allSaleCenter.addAll(centers)
        }

        headerOrderViewModel.productActId.observe(viewLifecycleOwner) { actId ->
            factorViewModel.updateHeader(actId = actId)
            setActSpinnerSelection(actId)
        }

        headerOrderViewModel.acts.observe(viewLifecycleOwner) { acts ->
            allAct.clear()
            allAct.addAll(acts)

            Log.d("factorHeaderctId2", acts.toString())
            Log.d("factorHeaderctId2", acts.toString())
            updateActSpinner()
        }

        headerOrderViewModel.addedAct.observe(viewLifecycleOwner) { act ->
            act?.let {
                allAct.add(it)
                updateActSpinner()
                factorViewModel.factorHeader.value?.actId?.let { id ->
                    setActSpinnerSelection(id)
                }
            }
        }

        headerOrderViewModel.getAct().observe(viewLifecycleOwner) { list ->
            allAct.clear()
            allAct.addAll(list)
            val items = mutableListOf<String>()
            items.add(getString(R.string.label_please_select))
            items.addAll(list.map { it.description!! })

            binding.spAct.adapter =
                SpinnerAdapter(requireContext(), items)
        }

        //  fillPaymentType()

        binding.spPaymentType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val entry = allPaymentType[position].id
                    factorViewModel.updateHeader(settlementKind = entry)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factorViewModel.updateHeader(settlementKind = 0)
                }
            }

        factorViewModel.headerId.observe(viewLifecycleOwner) { id ->
            if (id == null) return@observe
            factorViewModel.enteredProductPage = true

            if (pendingNavigation == "catalog") {

                val action =
                    HeaderOrderFragmentDirections.actionHeaderOrderFragmentToProductListFragment(
                        true, id
                    )
                findNavController().navigate(action)
            } else if (pendingNavigation == "group") {
                val action =
                    HeaderOrderFragmentDirections.actionHeaderOrderFragmentToGroupProductFragment(
                        true, id
                    )
                findNavController().navigate(action)
            }

            // جلوگیری از اجرای مجدد ناوبری
            factorViewModel.headerId.value = null
            pendingNavigation = ""
        }

        // دریافت نتیجه اعتبارسنجی
        headerOrderViewModel.validationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                showChooseDialog()
            }
        }

        // دریافت پیام خطا و نمایش با CustomSnackBar
        headerOrderViewModel.errorMessageRes.observe(viewLifecycleOwner) { msgResId ->
            msgResId?.let {
                CustomSnackBar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(it),
                    SnackBarType.Error.value
                )?.show()
            }
        }
    }

    // تابع برای آپدیت adapter و spinner
    private fun updateActSpinner() {
        val items = mutableListOf(getString(R.string.label_please_select))
        items.addAll(allAct.map { it.description ?: "" })
        binding.spAct.adapter =
            SpinnerAdapter(requireContext(), items)
        Log.d("factorHeaderctId", factorViewModel.factorHeader.value?.actId.toString())
        factorViewModel.factorHeader.value?.actId?.let { id ->
            binding.spAct.setSelectionById(id, allAct) { it.id }
        }
        if (isEditMode) {
            Log.d("factorHeaderctId4", factorViewModel.factorHeader.value?.actId.toString())
            Log.d("isEditModeinvoiceactId", "ok")

            editingHeader?.actId?.let { id ->
                binding.spAct.setSelectionById(
                    id = id,
                    items = allAct
                ) { it.id }
            }
        }
    }

    // تابع برای ست کردن ActId انتخاب شده در spinner
    private fun setActSpinnerSelection(actId: Int?) {
        if (actId == null) return
        val index = allAct.indexOfFirst { it.id == actId }
        if (index >= 0) {
            binding.spAct.setSelection(index + 1)
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
                        val date = PersianCalendar(it)
                        val year = date.year
                        val month = date.month
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

        // ------------------------- Click Listeners -------------------------
        binding.cvDate.setOnClickListener {
            showPersianDatePicker { date ->
                val gregorianDate = persianToGregorian(date)
                binding.tvDate.text = date
                factorViewModel.updateHeader(createDate = gregorianDate)
            }
        }

        binding.cvDuoDate.setOnClickListener {
            showPersianDatePicker { date ->
                val gregorianDate = persianToGregorian(date)
                binding.tvDuoDate.text = date
                factorViewModel.updateHeader(dueDate = gregorianDate)
            }
        }

        binding.cvDeliveryDate.setOnClickListener {
            showPersianDatePicker { date ->
                val gregorianDate = persianToGregorian(date)
                binding.tvDeliveryDate.text = date
                factorViewModel.updateHeader(deliveryDate = gregorianDate)
            }
        }

        binding.cvCustomer.setOnClickListener {
            rotateArrow(true)
            CustomerListBottomSheet.newInstance {
                rotateArrow(false)
            }.show(parentFragmentManager, "CustomerListBottomSheet")
        }

        binding.btnContinue.setOnClickBtnOneListener {
            factorViewModel.updateHeader(description = binding.etDescription.text.toString())
            validateHeader()
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

    private fun loadEditData() {
        factorViewModel.getHeaderById(args.factorId)
            .observe(viewLifecycleOwner) { header ->
                editingHeader = header
                factorViewModel.factorHeader.value = header

                binding.tvDate.text = gregorianToPersian(header.createDate.toString())
                binding.tvDuoDate.text = gregorianToPersian(header.dueDate.toString())
                binding.tvDeliveryDate.text = gregorianToPersian(header.deliveryDate.toString())
                binding.etDescription.setText(header.description)
                lifecycleScope.launch {

                    // ست کردن مقادیر اولیه
                    saleCenterId = header.saleCenterId ?: userPreferences.saleCenterId.first() ?: 0
                    userId = userPreferences.id.first() ?: 0
                }

                // دریافت نام مشتری بر اساس ID
                customerViewModel.getCustomerById(header.customerId!!)
                    .observe(viewLifecycleOwner) { customer ->
                        if (customer != null) {
                            loadCustomerData(header.customerId!!, customer.name)
                        } else {
                            binding.tvCustomerName.text = getString(R.string.msg_no_customer)
                            loadCustomerData(header.customerId!!, "")
                        }
                    }
                // لود invoiceCategory (مشاهده‌گر برای انتخاب اسپینر)
                header.invoiceCategoryId?.let { _ ->
                    // لود کردن لیست invoiceCategory برای اسپینر
                    headerOrderViewModel.getInvoiceCategory(userId)
                        .observe(viewLifecycleOwner, object :
                            Observer<List<InvoiceCategoryEntity>> {
                            override fun onChanged(list: List<InvoiceCategoryEntity>) {
                                // فقط یک‌بار اجرا شود
                                headerOrderViewModel.getInvoiceCategory(userId).removeObserver(this)

                                allInvoiceCategory.clear()
                                allInvoiceCategory.addAll(list)

                                val items = mutableListOf(getString(R.string.label_please_select))
                                items.addAll(list.map { it.name })
                                binding.spInvoiceCategory.adapter =
                                    SpinnerAdapter(requireContext(), items)

                                // ست کردن مقدار انتخابی
                                header.invoiceCategoryId?.let { id ->
                                    binding.spInvoiceCategory.setSelectionById(
                                        id = id,
                                        items = allInvoiceCategory
                                    ) { it.id }
                                }
                            }
                        })
                }

                // لود الگوها بر اساس invoiceCategoryId و customerId
                header.invoiceCategoryId?.let { categoryId ->
                    headerOrderViewModel.loadPatterns(
                        customer = header.customerId!!,
                        centerId = saleCenterId,
                        invoiceCategoryId = categoryId,
                        settlementKind = header.settlementKind ?: 0,
                        date = header.persianDate ?: getTodayPersianDate()
                    )
                }

                // لود Acts بر اساس patternId
                header.patternId?.let { patternId ->
                    headerOrderViewModel.loadActs(
                        patternId = patternId,
                        actKind = ActKind.Product.ordinal
                    )
                }
                header.patternId?.let { patternId ->
                    // لود Acts
                    headerOrderViewModel.loadActs(patternId, ActKind.Product.ordinal)

                    // لود Pattern و نمایش payment type
                    headerOrderViewModel.loadPatternById(patternId)
                    headerOrderViewModel.selectedPattern.observe(viewLifecycleOwner) { pattern ->
                        fillPaymentType(pattern)
                    }
                }

            }
    }

    private fun <T> Spinner.setSelectionById(
        id: Int?,
        items: List<T>,
        getId: (T) -> Int?
    ) {
        if (id == null) return

        val position = items.indexOfFirst { getId(it) == id }
        if (position >= 0) setSelection(position + 1)
    }


    /*
        private fun fillPaymentType() {
            allPaymentType.clear()
            // ابتدا تمام موارد را اضافه کن
            allPaymentType.add(KeyValue(0, "نقدی"))
            allPaymentType.add(KeyValue(1, "نقدی در سررسید"))
            allPaymentType.add(KeyValue(2, "نقد و اسناد"))
            allPaymentType.add(KeyValue(3, "اسناد"))
            allPaymentType.add(KeyValue(4, "اعتباری"))

            val items = allPaymentType.map { it.name }.toMutableList()
            binding.spPaymentType.adapter =
                SpinnerAdapter(requireContext(), items)

            if (factorViewModel.factorHeader.value!!.patternId != null) {
                headerOrderViewModel.getPatternById(factorViewModel.factorHeader.value!!.patternId!!)

            }
            headerOrderViewModel.pattern.observe(viewLifecycleOwner) { pattern ->
                if (pattern == null || pattern.hasCash) allPaymentType.add(KeyValue(0, "نقدی"))
                if (pattern == null || pattern.hasMaturityCash) allPaymentType.add(
                    KeyValue(
                        1,
                        "نقدی در سررسید"
                    )
                )
                if (pattern == null || pattern.hasSanadAndCash) allPaymentType.add(
                    KeyValue(
                        2,
                        "نقد و اسناد"
                    )
                )
                if (pattern == null || pattern.hasSanad) allPaymentType.add(KeyValue(3, "اسناد"))
                if (pattern == null || pattern.hasCredit) allPaymentType.add(
                    KeyValue(
                        4,
                        "اعتباری"
                    )
                )
            }
        }
    */
// تابع جدید
    private fun fillPaymentType(pattern: PatternEntity?) {
        allPaymentType.clear()
        if (pattern == null || pattern.hasCash) allPaymentType.add(KeyValue(0, "نقدی"))
        if (pattern == null || pattern.hasMaturityCash) allPaymentType.add(
            KeyValue(
                1,
                "نقدی در سررسید"
            )
        )
        if (pattern == null || pattern.hasSanadAndCash) allPaymentType.add(
            KeyValue(
                2,
                "نقد و اسناد"
            )
        )
        if (pattern == null || pattern.hasSanad) allPaymentType.add(KeyValue(3, "اسناد"))
        if (pattern == null || pattern.hasCredit) allPaymentType.add(KeyValue(4, "اعتباری"))

        val items = allPaymentType.map { it.name }.toMutableList()
        binding.spPaymentType.adapter = SpinnerAdapter(requireContext(), items)

        // ست کردن مقدار ذخیره‌شده
        factorViewModel.factorHeader.value?.settlementKind?.let { saved ->
            val index = allPaymentType.indexOfFirst { it.id == saved }
            if (index >= 0) binding.spPaymentType.setSelection(index)
        }
    }

    private fun validateHeader() {
        val factor = factorViewModel.factorHeader.value ?: return

        // Invoice Category
        if (binding.spInvoiceCategory.selectedItemPosition == 0) {
            showError(R.string.error_selecting_invoice_category_mandatory)
            return
        }

        // Pattern
        if (binding.spPattern.selectedItemPosition == 0) {
            showError(R.string.error_selecting_pattern_mandatory)
            return
        }

        // Act
        if (binding.spAct.selectedItemPosition == 0) {
            showError(R.string.error_selecting_act_mandatory)
            return
        }
        //AnbarId
        if (factor.defaultAnbarId == null) {
            showError(R.string.error_there_not_default_warehouse_sales_center)
            return
        }
        // اگر UI معتبر بود → بفرست به ViewModel
        headerOrderViewModel.validateHeader(
            saleCenterId = saleCenterId,
            factor = factor
        )
    }

    private fun showChooseDialog() {
        if (isBottomSheetShowing) return

        val dialog = BottomSheetChooseDialog.newInstance()
            .setTitle(R.string.label_choose)
            .addOption(R.string.label_product_catalog, R.drawable.ic_home_catalog) {
                pendingNavigation = "catalog"
                Log.d("factorHeader", factorViewModel.factorHeader.value!!.toString())
                factorViewModel.createHeader(factorViewModel.factorHeader.value)
            }
            .addOption(R.string.label_product_group, R.drawable.ic_home_group_product) {
                pendingNavigation = "group"
                factorViewModel.createHeader(factorViewModel.factorHeader.value)
            }

        dialog.show(childFragmentManager, "chooseDialog")
        isBottomSheetShowing = true

        dialog.setOnDismissListener {
            isBottomSheetShowing = false
        }
    }

    private fun showError(@StringRes resId: Int) {
        CustomSnackBar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(resId),
            SnackBarType.Error.value
        )?.show()
    }

    private fun setWidth() {
        binding.apply {
            val margin =
                resources.getDimensionPixelSize(R.dimen.x_big_size)
            spCustomerDirection.post {
                val dropDownWidth = cvCustomerDirection.width - margin
                spCustomerDirection.dropDownWidth = dropDownWidth
            }
            spInvoiceCategory.post {
                val dropDownWidth = cvInvoiceCategory.width - margin
                spInvoiceCategory.dropDownWidth = dropDownWidth
            }
            spPattern.post {
                val dropDownWidth = cvPattern.width - margin
                spPattern.dropDownWidth = dropDownWidth
            }
            spAct.post {
                val dropDownWidth = cvAct.width - margin
                spAct.dropDownWidth = dropDownWidth
            }
            spPaymentType.post {
                val dropDownWidth = cvPaymentType.width - margin
                spPaymentType.dropDownWidth = dropDownWidth
            }
        }
    }

    private fun rotateArrow(isExpanded: Boolean) {
        val rotation = if (isExpanded) 180f else 0f
        binding.ivCustomerName.animate().rotation(rotation).setDuration(200).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val navController = findNavController()
        val isGoingToProduct =
            navController.currentDestination?.id ==
                    R.id.productListFragment

        if (!isGoingToProduct) {
            factorViewModel.resetHeader()
            factorViewModel.enteredProductPage = false
        }
    }
}