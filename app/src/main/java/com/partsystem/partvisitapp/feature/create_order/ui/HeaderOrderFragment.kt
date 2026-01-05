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
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class HeaderOrderFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

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
    private var hasLoadedEditData = false

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

        if (isEditMode) {
            loadEditData()
        } else {
            ensureHeaderInitialized()
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
            // اطمینان از مقداردهی saleCenterId در هر حالت
            lifecycleScope.launch {
                saleCenterId = mainPreferences.saleCenterId.first() ?: 0
            }
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

    private fun ensureHeaderInitialized() {
        lifecycleScope.launch {
            // فقط یک بار اجرا شود (برای جلوگیری از تداخل در تغییرات مکرر)
            if (hasLoadedEditData) return@launch
            hasLoadedEditData = true

            val current = factorViewModel.factorHeader.value

            // اگر هدر هنوز ایجاد نشده یا uniqueId ندارد، یکی بساز
            if (current?.uniqueId == null) {
                createNewHeader()
            } else {
                // حتی اگر هدر وجود داشت، مطمئن شویم saleCenterId و سایر مقادیر ضروری ست شده‌اند
                val saleCenterIdPref = mainPreferences.saleCenterId.first() ?: 0
                val userId = mainPreferences.id.first() ?: 0
                val visitorId = mainPreferences.personnelId.first() ?: 0

                // فقط اگر فیلدهای ضروری null بودند، آپدیت کن
                if (current.saleCenterId == null || current.createUserId == null || current.visitorId == null) {
                    factorViewModel.factorHeader.value = current.copy(
                        saleCenterId = current.saleCenterId ?: saleCenterIdPref,
                        createUserId = current.createUserId ?: userId,
                        visitorId = current.visitorId ?: visitorId,
                        // اطمینان از تاریخ‌ها (اختیاری)
                        createDate = current.createDate ?: getTodayGregorian(),
                        persianDate = current.persianDate ?: getTodayPersianDate(),
                        dueDate = current.dueDate ?: getTodayGregorian(),
                        deliveryDate = current.deliveryDate ?: getTodayGregorian(),
                        createTime = current.createTime ?: getCurrentTime()
                    )
                }

                // همیشه saleCenterId را به‌روز کن (برای استفاده در loadPatterns)
                saleCenterId = current.saleCenterId ?: saleCenterIdPref

                // اگر defaultAnbarId نبود، بارگیری کن
                if (current.defaultAnbarId == null) {
                    headerOrderViewModel.fetchDefaultAnbarId(saleCenterId)
                    headerOrderViewModel.defaultAnbarId.collect { anbarId ->
                        if (anbarId != null) {
                            factorViewModel.updateHeader(defaultAnbarId = anbarId)
                        }
                    }
                }
            }
        }
    }
    private fun createNewHeader() {

        lifecycleScope.launch {

            val current = factorViewModel.factorHeader.value

            //  اگر هدر  جدید است
            if (current?.uniqueId == null) {

                saleCenterId = mainPreferences.saleCenterId.first() ?: 0
                controlVisit = mainPreferences.controlVisitSchedule.first() ?: false
                userId = mainPreferences.id.first() ?: 0
                visitorId = mainPreferences.personnelId.first() ?: 0

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
        if (!args.typeCustomer) {
            customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
                if (isEditMode) return@observe // در حالت ویرایش،این قسمت اجرا نشود

                if (customers.isNotEmpty()) {
                    val first = customers.first()
                    factorViewModel.updateHeader(customerId = first.id)
                    binding.tvCustomerName.text = first.name
                    loadCustomerData(first.id, first.name)
                }
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
            if (factorViewModel.enteredProductPage) {
                // به محصولات برگرد
                val currentFactorId =
                    factorViewModel.currentFactorId.value ?: return@setOnClickBtnOneListener
                val action = HeaderOrderFragmentDirections
                    .actionHeaderOrderFragmentToProductListFragment(
                        fromFactor = true,
                        factorId = currentFactorId.toInt()
                    )
                findNavController().navigate(action)
            } else {
                // اولین بار (اعتبارسنجی و نمایش دیالوگ)
                validateHeader()
            }
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
                    saleCenterId = header.saleCenterId ?: mainPreferences.saleCenterId.first() ?: 0
                    userId = mainPreferences.id.first() ?: 0
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
                // لود invoiceCategory
                header.invoiceCategoryId?.let { _ ->
                    // لود کردن لیست invoiceCategory برای اسپینر
                    headerOrderViewModel.getInvoiceCategory(userId)
                        .observe(viewLifecycleOwner, object :
                            Observer<List<InvoiceCategoryEntity>> {
                            override fun onChanged(list: List<InvoiceCategoryEntity>) {

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
                lifecycleScope.launch {
                    val header = factorViewModel.factorHeader.value ?: return@launch

                    var finalFactorId: Long

                    //  اگر id معتبر دارد (یعنی قبلاً ذخیره شده) دوباره ذخیره نکن
                    if (header.id != null && header.id > 0) {
                        finalFactorId = header.id.toLong()
                        // به‌روزرسانی
                        factorViewModel.currentFactorId.postValue(finalFactorId)
                    } else {
                        // اولین بار است → ذخیره کن
                        finalFactorId = factorViewModel.saveHeaderAndGetId(header)
                        // به‌روزرسانی هدر در ViewModel با id جدید
                        factorViewModel.factorHeader.postValue(
                            header.copy(id = finalFactorId.toInt())
                        )
                        factorViewModel.currentFactorId.postValue(finalFactorId)
                    }

                    factorViewModel.enteredProductPage = true

                    withContext(Dispatchers.Main) {
                        val action = HeaderOrderFragmentDirections
                            .actionHeaderOrderFragmentToProductListFragment(
                                fromFactor = true,
                                factorId = finalFactorId.toInt()
                            )
                        findNavController().navigate(action)
                    }
                }
            }
            .addOption(R.string.label_product_group, R.drawable.ic_home_group_product) {
                pendingNavigation = "group"
                lifecycleScope.launch {
                    val header = factorViewModel.factorHeader.value ?: return@launch

                    var finalFactorId: Long

                    if (header.id != null && header.id > 0) {
                        finalFactorId = header.id.toLong()
                        factorViewModel.currentFactorId.postValue(finalFactorId)
                    } else {
                        finalFactorId = factorViewModel.saveHeaderAndGetId(header)
                        factorViewModel.factorHeader.postValue(header.copy(id = finalFactorId.toInt()))
                        factorViewModel.currentFactorId.postValue(finalFactorId)
                    }

                    withContext(Dispatchers.Main) {
                        val action = HeaderOrderFragmentDirections
                            .actionHeaderOrderFragmentToGroupProductFragment(
                                fromFactor = true,
                                factorId = finalFactorId.toInt()
                            )
                        findNavController().navigate(action)
                    }
                }
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
        if (!isEditMode) {
            val navController = findNavController()
            val isGoingToHome = navController.currentDestination?.id == R.id.homeFragment
            val isGoingToAnyNonOrderScreen = navController.currentDestination?.id !in listOf(
                R.id.headerOrderFragment,
                R.id.productListFragment,
                R.id.groupProductFragment,
                R.id.orderFragment
            )
            if (isGoingToHome || isGoingToAnyNonOrderScreen) {
                factorViewModel.resetHeader()
                factorViewModel.enteredProductPage = false
            }
        }
        _binding = null
    }
}