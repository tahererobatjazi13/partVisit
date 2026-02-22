package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
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
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
    private var isFromCustomerDetail = false
    private var isCustomerLoaded = false

    private val isEditMode get() = args.factorId > 0
    private var editingHeader: FactorHeaderEntity? = null

    data class KeyValue(val id: Int, val name: String)

    private val args: HeaderOrderFragmentArgs by navArgs()
    private val persianDate: String = getTodayPersianDateLatin()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeaderOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        setupSpinners()
        isFromCustomerDetail = args.typeCustomer && args.customerId != 0

        // اگر از دیتیل مشتری آمده‌ایم، همین الان isCustomerLoaded را true کن
        if (isFromCustomerDetail && args.customerId != 0 && args.customerName.isNotEmpty()) {
            isCustomerLoaded = true
            loadCustomerData(args.customerId,args.customerName)
        }
        observeData()

        if (isEditMode) {
            loadEditData()
        } else {
            // همیشه هدر را ایجاد یا نمایش بده (بدون چک کردن وضعیت قبلی)
            ensureHeaderInitialized()
        }

        setupClicks()
        setWidth()
    }
    private fun handleCustomerFromArgs() {
        if (args.customerId != 0 && args.customerName.isNotEmpty()) {
            // ۱. نمایش نام در UI
            binding.tvCustomerName.text = args.customerName

            // ۲. آپدیت هدر در ViewModel
            factorViewModel.updateHeader(customerId = args.customerId)

            // ۳. لود اطلاعات تکمیلی مشتری (مسیرها و...)
            loadCustomerData(args.customerId, args.customerName)

            // ۴. ثبت وضعیت لود شدن
            isCustomerLoaded = true
        }
    }
    private fun initAdapter() {
        val defaultAdapter =
            SpinnerAdapter(requireContext(), mutableListOf(getString(R.string.label_please_select)))
        binding.spInvoiceCategory.adapter = defaultAdapter
        binding.spPattern.adapter = defaultAdapter
        binding.spAct.adapter = defaultAdapter
        binding.spCustomerDirection.adapter = defaultAdapter
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
        viewLifecycleOwner.lifecycleScope.launch {
            val current = factorViewModel.factorHeader.value

            // اگر هدر از قبل وجود دارد، فقط داده‌ها را به ویوها بیند کن
            if (current?.uniqueId != null) {
                bindHeaderToViews(current)
                return@launch
            }

            // هدر جدید ایجاد کن
            createNewHeader()
        }
    }

    private suspend fun bindHeaderToViews(header: FactorHeaderEntity) {
        // بیند کردن تاریخ‌ها
        header.createDate?.let { binding.tvDate.text = gregorianToPersian(it) }
        header.dueDate?.let { binding.tvDuoDate.text = gregorianToPersian(it) }
        header.deliveryDate?.let { binding.tvDeliveryDate.text = gregorianToPersian(it) }
        binding.etDescription.setText(header.description)

        // بارگذاری مقادیر پیش‌فرض
        saleCenterId = header.saleCenterId ?: mainPreferences.saleCenterId.firstOrNull() ?: 0
        userId = mainPreferences.id.firstOrNull() ?: 0
        visitorId = mainPreferences.personnelId.firstOrNull() ?: 0

        // بارگذاری مشتری با استفاده از observe (نه .value)
        header.customerId?.let { customerId ->
            // اولویت: اگر از دیتیل مشتری آمده‌ایم و customerId مطابقت دارد
            if (isFromCustomerDetail && args.customerId == customerId && args.customerName.isNotEmpty()) {
                binding.tvCustomerName.text = args.customerName
                loadCustomerData(args.customerId, args.customerName)
                isCustomerLoaded = true
            }
            // در غیر این صورت، از دیتابیس لود کن
            else if (!isCustomerLoaded) {
                customerViewModel.getCustomerById(customerId)
                    .observe(viewLifecycleOwner) { customer ->
                        if (customer != null) {
                            binding.tvCustomerName.text = customer.name
                            loadCustomerData(customerId, customer.name)
                            isCustomerLoaded = true
                        } else {
                            binding.tvCustomerName.text = getString(R.string.msg_no_customer)
                        }
                    }
            }
        }
        // بارگذاری دسته‌بندی صورتحساب اگر وجود دارد
        header.invoiceCategoryId?.let { categoryId ->
            // اطمینان از بارگذاری لیست دسته‌بندی‌ها
            headerOrderViewModel.getInvoiceCategory(userId).observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    allInvoiceCategory.clear()
                    allInvoiceCategory.addAll(list)
                    val items = mutableListOf(getString(R.string.label_please_select))
                    items.addAll(list.map { it.name })
                    binding.spInvoiceCategory.adapter = SpinnerAdapter(requireContext(), items)

                    // ست کردن مقدار انتخابی
                    binding.spInvoiceCategory.setSelectionById(
                        categoryId,
                        allInvoiceCategory
                    ) { it.id }

                    // بارگذاری الگوها پس از انتخاب دسته‌بندی
                    header.customerId?.let { customerId ->
                        headerOrderViewModel.loadPatterns(
                            customer = customerId,
                            centerId = saleCenterId,
                            invoiceCategoryId = categoryId,
                            settlementKind = header.settlementKind,
                            date = header.persianDate ?: getTodayPersianDateLatin()
                        )
                    }
                }
            }
        }
        // بارگذاری الگو و آکت از طریق observe (نه فراخوانی مستقیم)
        header.patternId?.let { patternId ->
            // اطمینان از بارگذاری لیست الگوها قبل از ست کردن انتخاب
            headerOrderViewModel.patterns.observe(viewLifecycleOwner) { patterns ->
                if (patterns.isNotEmpty()) {
                    allPattern.clear()
                    allPattern.addAll(patterns)
                    val items = mutableListOf(getString(R.string.label_please_select))
                    items.addAll(patterns.map { it.name })
                    binding.spPattern.adapter = SpinnerAdapter(requireContext(), items)

                    // ست کردن مقدار انتخابی
                    binding.spPattern.setSelectionById(patternId, allPattern) { it.id }

                    // بارگذاری آکت‌ها پس از انتخاب الگو
                    headerOrderViewModel.loadActs(
                        patternId = patternId,
                        actKind = ActKind.Product.ordinal
                    )

                    // لود نوع پرداخت
                    headerOrderViewModel.loadPatternById(patternId)
                    headerOrderViewModel.selectedPattern.observe(viewLifecycleOwner) { pattern ->
                        fillPaymentType(pattern)
                    }
                }
            }
        }

        // بارگذاری آکت انتخاب شده
        header.actId?.let { actId ->
            headerOrderViewModel.acts.observe(viewLifecycleOwner) { acts ->
                if (acts.isNotEmpty()) {
                    allAct.clear()
                    allAct.addAll(acts)
                    updateActSpinner()

                    // ست کردن مقدار انتخابی با تأخیر کوتاه برای اطمینان از آماده بودن اسپینر
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(100)
                        binding.spAct.setSelectionById(actId, allAct) { it.id }
                    }
                }
            }
        }
        // بارگذاری انبار پیش‌فرض اگر وجود ندارد
        if (header.defaultAnbarId == null && saleCenterId != 0) {
            headerOrderViewModel.fetchDefaultAnbarId(saleCenterId)
            viewLifecycleOwner.lifecycleScope.launch {
                headerOrderViewModel.defaultAnbarId.firstOrNull()?.let { anbarId ->
                    factorViewModel.updateHeader(defaultAnbarId = anbarId)
                }
            }
        }
        /*
      // بارگذاری داده‌های تکمیلی فقط اگر هدر کامل نیست
      if (header.invoiceCategoryId != null && header.customerId != null) {
          headerOrderViewModel.loadPatterns(
              customer = header.customerId!!,
              centerId = saleCenterId,
              invoiceCategoryId = header.invoiceCategoryId!!,
              settlementKind = header.settlementKind ?: 0,
              date = header.persianDate ?: getTodayPersianDateLatin()
          )
      }

      if (header.patternId != null) {
          headerOrderViewModel.loadActs(
              patternId = header.patternId!!,
              actKind = ActKind.Product.ordinal
          )
      }*/
    }

    private fun createNewHeader() {
        viewLifecycleOwner.lifecycleScope.launch {
            val current = factorViewModel.factorHeader.value

            // فقط اگر هدر وجود ندارد یا فاکتور جدید است (بدون uniqueId)، هدر جدید ایجاد کن
            if (current?.uniqueId != null) {
                bindHeaderToViews(current)
                return@launch
            }

            // ایجاد هدر جدید
            saleCenterId = mainPreferences.saleCenterId.firstOrNull() ?: 0
            controlVisit = mainPreferences.controlVisitSchedule.firstOrNull() ?: false
            userId = mainPreferences.id.firstOrNull() ?: 0
            visitorId = mainPreferences.personnelId.firstOrNull() ?: 0

            val initialCustomerId = if (isFromCustomerDetail && args.customerId != 0) {
                args.customerId
            } else {
                null
            }
            val newHeader = FactorHeaderEntity(
                uniqueId = getGUID(),
                saleCenterId = saleCenterId,
                settlementKind = 0,
                createSource = 2,
                formKind = FactorFormKind.RegisterOrderDistribute.ordinal,
                createDate = getTodayGregorian(),
                persianDate = getTodayPersianDateLatin(),
                dueDate = getTodayGregorian(),
                deliveryDate = getTodayGregorian(),
                createTime = getCurrentTime(),
                createUserId = userId,
                visitorId = visitorId,
                sabt = 0,
                customerId = initialCustomerId

            )

            factorViewModel.factorHeader.value = newHeader

            // بارگذاری انبار پیش‌فرض
            headerOrderViewModel.fetchDefaultAnbarId(saleCenterId)
            headerOrderViewModel.defaultAnbarId.collect { anbarId ->
                if (anbarId != null) {
                    mainPreferences.saveDefaultAnbarId(defaultAnbarId = anbarId)
                    factorViewModel.updateHeader(defaultAnbarId = anbarId)
                }
            }
            if (isFromCustomerDetail && args.customerId != 0 && args.customerName.isNotEmpty()) {
                binding.tvCustomerName.text = args.customerName
                loadCustomerData(args.customerId, args.customerName)
                isCustomerLoaded = true
            }
            // در غیر این صورت، اولین مشتری لیست را انتخاب کن (فقط برای سفارش جدید عادی)
            else if (!isCustomerLoaded && !isFromCustomerDetail) {
                customerViewModel.filteredCustomers.value?.firstOrNull()?.let { firstCustomer ->
                    binding.tvCustomerName.text = firstCustomer.name
                    loadCustomerData(firstCustomer.id, firstCustomer.name)
                    factorViewModel.updateHeader(customerId = firstCustomer.id)
                    isCustomerLoaded = true
                }
            }
        }
    }

    private fun loadCustomerData(customerId: Int, customerName: String) {
        binding.tvCustomerName.text = customerName
        factorViewModel.updateHeader(customerId = customerId)
        headerOrderViewModel.assignDirection.observe(viewLifecycleOwner) { directions ->

            directions.forEach { item ->
                if (item.isDistribution) factorViewModel.factorHeader.value!!.distributorId =
                    item.tafsiliId
                if (item.isDemands) factorViewModel.factorHeader.value!!.recipientId =
                    item.tafsiliId
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

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(100) // تأخیر برای اطمینان از آماده بودن اسپینر

                    // اولویت ۱: از هدر فعلی ویومدل بخوان
                    val directionIdFromHeader =
                        factorViewModel.factorHeader.value?.directionDetailId

                    // اولویت ۲: اگر در ویومدل نبود، از هدر ویرایشی بخوان
                    val directionId = directionIdFromHeader ?: editingHeader?.directionDetailId

                    if (directionId != null && directionId != 0) {
                        binding.spCustomerDirection.setSelectionById(
                            id = directionId,
                            items = allCustomerDirection
                        ) { it.directionDetailId } // استفاده صحیح از directionDetailId
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
            date = getTodayPersianDateLatin()
        )
    }

    private fun observeData() {
        factorViewModel.factorHeader.observe(viewLifecycleOwner) { header ->
            header.createDate?.let { binding.tvDate.text = gregorianToPersian(it) }
            header.dueDate?.let { binding.tvDuoDate.text = gregorianToPersian(it) }
            header.deliveryDate?.let { binding.tvDeliveryDate.text = gregorianToPersian(it) }
        }

        // دریافت لیست مشتریان بدون شرط اضافی
        if (controlVisit) {
            customerViewModel.loadCustomersWithSchedule(persianDate)
        } else {
            customerViewModel.loadCustomersWithoutSchedule()
        }

        // انتخاب خودکار اولین مشتری فقط در حالت جدید (نه ویرایش)
        customerViewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            if (isEditMode || isCustomerLoaded) return@observe

            // فقط اگر هنوز مشتری انتخاب نشده است
            if (customers.isNotEmpty() && factorViewModel.factorHeader.value?.customerId == null) {
                val first = customers.first()
                factorViewModel.updateHeader(customerId = first.id)
                binding.tvCustomerName.text = first.name
                loadCustomerData(first.id, first.name)
                isCustomerLoaded = true
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

        // بارگذاری آکت‌ها با ست کردن انتخاب پس از بارگذاری کامل
        headerOrderViewModel.acts.observe(viewLifecycleOwner) { acts ->
            allAct.clear()
            allAct.addAll(acts)
            updateActSpinner()

            // ست کردن مقدار انتخابی با تأخیر کوتاه
            viewLifecycleOwner.lifecycleScope.launch {
                delay(50)
                factorViewModel.factorHeader.value?.actId?.let { id ->
                    binding.spAct.setSelectionById(id, allAct) { it.id }
                }
                if (isEditMode) {
                    editingHeader?.actId?.let { id ->
                        binding.spAct.setSelectionById(id, allAct) { it.id }
                    }
                }
            }
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
        binding.spAct.adapter = SpinnerAdapter(requireContext(), items)

        // ست کردن مقدار انتخابی با تأخیر کوتاه
        viewLifecycleOwner.lifecycleScope.launch {
            delay(50)
            factorViewModel.factorHeader.value?.actId?.let { id ->
                binding.spAct.setSelectionById(id, allAct) { it.id }
            }
            if (isEditMode) {
                editingHeader?.actId?.let { id ->
                    binding.spAct.setSelectionById(id, allAct) { it.id }
                }
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
                @SuppressLint("DefaultLocale")
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
            if (isEditMode) {
                factorViewModel.currentFactorId.value = args.factorId.toLong()

                navigateToProductPage(
                    factorViewModel.factorHeader.value!!.productSelectionType,
                    factorViewModel.factorHeader.value!!.sabt,
                    args.factorId
                )
            } else {
                if (factorViewModel.enteredProductPage) {
                    if (factorViewModel.factorHeader.value!!.productSelectionType == "catalog")
                        navigateToProductPage(
                            "catalog",
                            factorViewModel.factorHeader.value!!.sabt,
                            factorViewModel.factorHeader.value?.id!!,
                        ) else
                        navigateToProductPage(
                            "group",
                            factorViewModel.factorHeader.value!!.sabt,
                            factorViewModel.factorHeader.value?.id!!
                        )

                    // به محصولات برگرد
                    /* val currentFactorId =
                         factorViewModel.currentFactorId.value ?: return@setOnClickBtnOneListener
                     val action = HeaderOrderFragmentDirections
                         .actionHeaderOrderFragmentToProductListFragment(
                             fromFactor = true,
                             factorId = currentFactorId.toInt()
                         )
                     findNavController().navigate(action)*/
                } else {
                    // اولین بار (اعتبارسنجی و نمایش دیالوگ)
                    validateHeader()
                }
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
                // اطمینان از ست شدن directionDetailId در ویومدل
                if (header.directionDetailId != null && header.directionDetailId != 0) {
                    factorViewModel.updateHeader(directionDetailId = header.directionDetailId)
                }

                binding.tvDate.text = gregorianToPersian(header.createDate.toString())
                binding.tvDuoDate.text = gregorianToPersian(header.dueDate.toString())
                binding.tvDeliveryDate.text = gregorianToPersian(header.deliveryDate.toString())
                binding.etDescription.setText(header.description)
                viewLifecycleOwner.lifecycleScope.launch {
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
                        settlementKind = header.settlementKind,
                        date = header.persianDate ?: getTodayPersianDateLatin()
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

    // متد کمکی برای هدایت به صفحه محصولات
    private fun navigateToProductPage(selectionType: String, sabt: Int, factorId: Int) {

        val action = when (selectionType) {

            "group" ->
                HeaderOrderFragmentDirections
                    .actionHeaderOrderFragmentToGroupProductFragment(
                        fromFactor = true,
                        sabt = sabt,
                        factorId = factorId
                    )

            "catalog" -> HeaderOrderFragmentDirections
                .actionHeaderOrderFragmentToProductListFragment(
                    fromFactor = true,
                    sabt = sabt,
                    factorId = factorId
                )

            else -> HeaderOrderFragmentDirections // پیش‌فرض کاتالوگ
                .actionHeaderOrderFragmentToProductListFragment(
                    fromFactor = true,
                    sabt = sabt,
                    factorId = factorId
                )
        }
        findNavController().navigate(action)
    }

    private fun <T> Spinner.setSelectionById(
        id: Int?,
        items: List<T>,
        getId: (T) -> Int?
    ) {
        if (id == null || items.isEmpty() || adapter == null) return

        val position = items.indexOfFirst { getId(it) == id }
        // position + 1 چون اولین آیتم "لطفاً انتخاب کنید" است
        if (position >= 0 && position + 1 < adapter!!.count) {
            setSelection(position + 1)
        } else {
            // اگر آیتم پیدا نشد، اولین گزینه را انتخاب کن
            setSelection(0)
        }
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
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentHeader = factorViewModel.factorHeader.value ?: return@launch

                    val updatedHeader = currentHeader.copy(
                        productSelectionType = "catalog"
                    )

                    val finalFactorId: Long =
                        if (currentHeader.id > 0) {
                            factorViewModel.updateFactorHeader(updatedHeader)
                            factorViewModel.factorHeader.postValue(updatedHeader)
                            currentHeader.id.toLong()
                        } else {
                            val newId =
                                factorViewModel.saveHeaderAndGetId(updatedHeader)
                            factorViewModel.factorHeader.postValue(updatedHeader.copy(id = newId.toInt()))
                            newId
                        }

                    factorViewModel.currentFactorId.postValue(finalFactorId)
                    factorViewModel.enteredProductPage = true

                    withContext(Dispatchers.Main) {
                        val action = HeaderOrderFragmentDirections
                            .actionHeaderOrderFragmentToProductListFragment(
                                fromFactor = true,
                                sabt = currentHeader.sabt,
                                factorId = finalFactorId.toInt()
                            )

                        findNavController().navigate(action)
                    }
                }
            }
            .addOption(R.string.label_product_group, R.drawable.ic_home_group_product) {
                pendingNavigation = "group"
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentHeader = factorViewModel.factorHeader.value ?: return@launch

                    val updatedHeader = currentHeader.copy(
                        productSelectionType = "group"
                    )

                    val finalFactorId: Long =
                        if (currentHeader.id > 0) {
                            factorViewModel.updateFactorHeader(updatedHeader)
                            factorViewModel.factorHeader.postValue(updatedHeader)
                            currentHeader.id.toLong()
                        } else {
                            val newId =
                                factorViewModel.saveHeaderAndGetId(updatedHeader)
                            factorViewModel.factorHeader.postValue(updatedHeader.copy(id = newId.toInt()))
                            newId
                        }

                    factorViewModel.currentFactorId.postValue(finalFactorId)
                    factorViewModel.enteredProductPage = true

                    withContext(Dispatchers.Main) {
                        val action = HeaderOrderFragmentDirections
                            .actionHeaderOrderFragmentToGroupProductFragment(
                                fromFactor = true,
                                sabt = currentHeader.id,
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

        val navController = findNavController()
        val nextDestinationId = navController.currentDestination?.id

        val orderFlowDestinations = listOf(
            R.id.headerOrderFragment,
            R.id.productListFragment,
            R.id.groupProductFragment,
            R.id.offlineOrderDetailFragment
        )
        // ریست کن
        if (nextDestinationId !in orderFlowDestinations) {
            factorViewModel.resetHeader()
            factorViewModel.enteredProductPage = false
            factorViewModel.currentFactorId.value = null
        }

        _binding = null
    }
}