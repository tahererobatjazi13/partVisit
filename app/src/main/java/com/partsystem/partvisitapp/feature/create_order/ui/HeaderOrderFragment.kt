package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ActEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerDirectionEntity
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.database.entity.FactorEntity
import com.partsystem.partvisitapp.core.database.entity.PatternEntity
import com.partsystem.partvisitapp.core.database.entity.SaleCenterEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import com.partsystem.partvisitapp.core.utils.componenet.BottomSheetChooseDialog
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDate
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

    private lateinit var patternAdapter: SpinnerAdapter
    private lateinit var customerDirectionAdapter: SpinnerAdapter
    private lateinit var invoiceCategoryAdapter: SpinnerAdapter
    private lateinit var actAdapter: SpinnerAdapter

    private lateinit var factor: FactorEntity

    data class KeyValue(val id: Int, val name: String)

    private val args: HeaderOrderFragmentArgs by navArgs()

    private val persianDate: String = getTodayPersianDate()
    private val allCustomerDirection = mutableListOf<CustomerDirectionEntity>()
    private val allPattern = mutableListOf<PatternEntity>()
    private val allSaleCenter = mutableListOf<SaleCenterEntity>()
    private val allAct = mutableListOf<ActEntity>()

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
    }

    private fun fillControls() {
        lifecycleScope.launch {
            val saleCenterId = userPreferences.saleCenterId.first() ?: 0

            // ست کردن Factor
            factor = FactorEntity(
                saleCenterId = saleCenterId,
                persianDate = getTodayPersianDate(),
                settlementKind = 0 // مقدار دلخواه
            )/* factor = Factor()
             factor.UniqueId = StringHelper.getGUID()
             factor.Id = q.getMax("Factor", "Id", null) + 1
             factor.FormKind = SaleDefine.FactorFormKind.RegisterOrderDistribute.ordinal()
             factor.FactorDetails = ArrayList<Any>()
             factor.FactorDiscounts = ArrayList<Any>()
             factor.SaleCenterId = dataHolder.saleCenterId
             factor.DefaultAnbarId = q.getActiveSaleCenterAnbar(factor.SaleCenterId)
             factor.CreateSource = 2 // TODO: Enum
             val persianDate: String = DateHelper.getPersianDate()
             factor.CreateTime = DateTimeHelper.getTimeFromCurrentDate()
             factor.PersianDate = persianDate
             factor.PersianDueDate = persianDate
             factor.DeliveryPersianDate = persianDate
             factor.CreateDate = DateHelper.getPersianToGregorian(factor.PersianDate)
             factor.DueDate = DateHelper.getPersianToGregorian(factor.PersianDueDate)
             factor.DeliveryDate = DateHelper.getPersianToGregorian(factor.PersianDueDate)
             factor.VisitorId = dataHolder.visitorId
             factor.SettlementKind = 0 // TODO: Enum
             factor.CreateUserId = dataHolder.userId
             factor.Sabt = 0
             factor.IsCanceled = false
             factor.Description = ""*/
            headerOrderViewModel.setFactor(factor)
            // dataHolder.factor = factor
            // dataHolder.isDirty = false
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
            binding.tvCustomerName.text = getString(R.string.label_please_select)
            val defaultList = mutableListOf(getString(R.string.label_please_select))
            val defaultAdapter = SpinnerAdapter(requireContext(), defaultList)
            binding.spCustomerDirection.adapter = defaultAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            val controlVisit = userPreferences.controlVisitSchedule.first() ?: false
            val userId = userPreferences.id.first() ?: 0

            if (controlVisit) {
                //  با برنامه ویزیت
                customerViewModel.loadCustomersWithSchedule(persianDate)
            } else {
                //  بدون برنامه ویزیت
                customerViewModel.loadCustomersWithoutSchedule()
            }

            headerOrderViewModel.getInvoiceCategory(userId).observe(viewLifecycleOwner) { list ->

                val items = mutableListOf(getString(R.string.label_please_select))
                items.addAll(list.map { it.name })

                invoiceCategoryAdapter = SpinnerAdapter(requireContext(), items)
                binding.spInvoiceCategory.adapter = invoiceCategoryAdapter

                binding.spInvoiceCategory.post {
                    val margin = resources.getDimensionPixelSize(R.dimen.big_size)
                    val dropDownWidth = binding.cvInvoiceCategory.width - margin
                    binding.spInvoiceCategory.dropDownWidth = dropDownWidth
                }
            }

            binding.spInvoiceCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        if (position == 0) {
                            // یعنی "لطفاً انتخاب کنید"
                            return
                        }

                        // val selectedName = items[position]
                        // پردازش انتخاب واقعی
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            headerOrderViewModel.getInvoiceCategory(userId).observe(viewLifecycleOwner) { list ->

                val items = mutableListOf(getString(R.string.label_please_select))
                items.addAll(list.map { it.name })

                invoiceCategoryAdapter = SpinnerAdapter(requireContext(), items)
                binding.spInvoiceCategory.adapter = invoiceCategoryAdapter

                binding.spInvoiceCategory.post {
                    val margin = resources.getDimensionPixelSize(R.dimen.big_size)
                    val dropDownWidth = binding.cvInvoiceCategory.width - margin
                    binding.spInvoiceCategory.dropDownWidth = dropDownWidth
                }

                binding.spInvoiceCategory.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?, position: Int, id: Long
                        ) {

                            if (position == 0) {
                                factor.invoiceCategoryId = null
                                return
                            }

                            val selectedCategory = list[position - 1]
                            factor.invoiceCategoryId = selectedCategory.id

                            // LOAD SALE CENTERS
                            headerOrderViewModel.saleCenters.observe(viewLifecycleOwner) { centers ->
                                allSaleCenter.clear()
                                allSaleCenter.addAll(centers)
                                //  mSaleCenterAdapter.notifyDataSetChanged() // اگر adapter داری
                            }

                            // وقتی دسته انتخاب شد
                            selectedCategory?.let {
                                headerOrderViewModel.loadSaleCenters(it.id)
                            }


                            // LOAD PATTERNS
                            allPattern.clear()
                            if (factor.customerId != null) {
                                val patterns = headerOrderViewModel.loadPatterns(
                                    factor.customerId!!,
                                    factor.saleCenterId,
                                    factor.invoiceCategoryId,
                                    factor.settlementKind,
                                    factor.persianDate!!
                                )
                                headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
                                    val items = list.map { it.name }.toMutableList()
                                    patternAdapter = SpinnerAdapter(requireContext(), items)
                                    binding.spPattern.adapter = patternAdapter

                                    binding.spPattern.post {
                                        val margin =
                                            resources.getDimensionPixelSize(R.dimen.big_size)
                                        val dropDownWidth = binding.cvPattern.width - margin
                                        binding.spPattern.dropDownWidth = dropDownWidth
                                    }
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            factor.invoiceCategoryId = null
                        }
                    }
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
                            factor.directionDetailId = entry.directionDetailId
                        }

                        position == 0 && factor.directionDetailId != null /*&& isLoaded */ -> {
                            factor.directionDetailId = null
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    factor.directionDetailId = null
                }
            }

        headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
            val items = mutableListOf(getString(R.string.label_please_select))
            items.addAll(list.map { it.name })

            binding.spPattern.adapter = patternAdapter

            binding.spPattern.post {
                val margin =
                    resources.getDimensionPixelSize(R.dimen.big_size)
                val dropDownWidth = binding.cvPattern.width - margin
                binding.spPattern.dropDownWidth = dropDownWidth
            }
        }

        headerOrderViewModel.getAct().observe(viewLifecycleOwner) { act ->

            val items = act.mapNotNull { it.description }
            //   val adapter = SpinnerAdapter(requireContext(), items)
            //  binding.spAct.adapter = adapter
            binding.spAct.post {
                val margin = resources.getDimensionPixelSize(R.dimen.big_size)

                val dropDownWidth = binding.cvAct.width - margin
                binding.spAct.dropDownWidth = dropDownWidth
            }
        }
        binding.spPattern.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // DataHolder.isDirty = true

                if (position == 0) {
                    // انتخاب نشده
                    factor.patternId = null
                    factor.actId = null
                    return
                }

                // دریافت Pattern انتخابی
                val entry = allPattern[position - 1]

                factor.patternId = entry.id
                // factor.actId = entry.productActId   // اگر داری → مشابه جاوا

                // 🔵 مرحله 1: لود ACT
                headerOrderViewModel.loadActs(
                    patternId = entry.id,
                    actKind = /*ActKind.Product.value*/0
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                factor.patternId = null
                factor.actId = null
            }
        }
        headerOrderViewModel.acts.observe(viewLifecycleOwner) { list ->
            //    allAct.clear()
            //   allAct.addAll(list)

            // اگر ActId قبلاً انتخاب شده بود، اضافه کن
            factor.actId?.let { actId ->
                val exists = list.any { it.id == actId }
                if (!exists) {
                    headerOrderViewModel.loadActById(actId)
                }
            }

            actAdapter.notifyDataSetChanged()
        }
        headerOrderViewModel.selectedAct.observe(viewLifecycleOwner) { act ->
            if (act != null && !allAct.any { it.id == act.id }) {
                allAct.add(0, act)
                actAdapter.notifyDataSetChanged()
                binding.spAct.setSelection(1)
            }
        }

        /*
                headerOrderViewModel.getPattern().observe(viewLifecycleOwner) { pattern ->

                    val items = pattern.map { it.name }
                    //   patternAdapter  = SpinnerAdapter(requireContext(), items)
                    // binding.spPattern.adapter = patternAdapter

                    binding.spPattern.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?, view: View?, position: Int, id: Long
                            ) {
                                val selectedPattern = pattern[position]
                                fillPaymentType(selectedPattern)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                fillPaymentType(null)
                            }
                        }
                    binding.spPattern.post {
                        val margin = resources.getDimensionPixelSize(R.dimen.big_size)

                        val dropDownWidth = binding.cvPattern.width - margin
                        binding.spPattern.dropDownWidth = dropDownWidth
                    }
                }
        */
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
            showChooseDialog()
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

    private fun loadCustomerData(customerId: Int, customerName: String) {
        binding.tvCustomerName.text = customerName
        factor.customerId = customerId

        headerOrderViewModel.loadAssignDirectionCustomerByCustomerId(customerId)

        allCustomerDirection.clear()
        allPattern.clear()

        // بارگذاری مسیرهای مشتری
        headerOrderViewModel.getCustomerDirectionsByCustomer(customerId)
            .observe(viewLifecycleOwner) { list ->
                val items = list.mapNotNull { it.fullAddress }.toMutableList()

                customerDirectionAdapter = SpinnerAdapter(requireContext(), items)
                binding.spCustomerDirection.adapter = customerDirectionAdapter

                binding.spCustomerDirection.post {
                    val margin = resources.getDimensionPixelSize(R.dimen.big_size)
                    val dropDownWidth = binding.cvCustomerDirection.width - margin
                    binding.spCustomerDirection.dropDownWidth = dropDownWidth
                }
            }

        // بارگذاری الگوها
        headerOrderViewModel.patterns.observe(viewLifecycleOwner) { list ->
            val items = list.map { it.name }.toMutableList()
            patternAdapter = SpinnerAdapter(requireContext(), items)
            binding.spPattern.adapter = patternAdapter

            binding.spPattern.post {
                val margin = resources.getDimensionPixelSize(R.dimen.big_size)
                val dropDownWidth = binding.cvPattern.width - margin
                binding.spPattern.dropDownWidth = dropDownWidth
            }
        }

        headerOrderViewModel.loadPatterns(
            customer = customerId,
            centerId = factor.saleCenterId,
            invoiceCategoryId = 0,
            settlementKind = factor.settlementKind,
            date = factor.persianDate.toString()
        )
    }

    // تابع برای پر کردن Spinner نوع پرداخت
    private fun fillPaymentType(pattern: PatternEntity?) {
        val allPaymentType = mutableListOf<KeyValue>()

        if (pattern == null || pattern.hasCash) {
            allPaymentType.add(KeyValue(0, "نقدی"))
        }
        if (pattern == null || pattern.hasMaturityCash) {
            allPaymentType.add(KeyValue(1, "نقدی در سررسید"))
        }
        if (pattern == null || pattern.hasSanadAndCash) {
            allPaymentType.add(KeyValue(2, "نقد و اسناد"))
        }
        if (pattern == null || pattern.hasSanad) {
            allPaymentType.add(KeyValue(3, "اسناد"))
        }
        if (pattern == null || pattern.hasCredit) {
            allPaymentType.add(KeyValue(4, "اعتباری"))
        }

        // Adapter برای Spinner PaymentType
        //   val adapter = SpinnerAdapter(requireContext(), allPaymentType.map { it.name })
        //   binding.spPaymentType.adapter = adapter
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

    private fun rotateArrow(isExpanded: Boolean) {
        val rotation = if (isExpanded) 180f else 0f
        binding.ivCustomerName.animate().rotation(rotation).setDuration(200).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}