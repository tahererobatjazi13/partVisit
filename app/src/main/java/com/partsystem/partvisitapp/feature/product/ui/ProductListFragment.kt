package com.partsystem.partvisitapp.feature.product.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.FragmentProductListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.product.dialog.AddEditProductDialog
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.floor

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productListAdapter: ProductListAdapter
    private val args: ProductListFragmentArgs by navArgs()

    private val productViewModel: ProductViewModel by viewModels()
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       /* if (args.fromFactor) {
            val currentHeaderId = factorViewModel.factorHeader.value?.id
            if (currentHeaderId == null || currentHeaderId != args.factorId) {
                factorViewModel.loadFactorHeader(args.factorId)
            }
        }*/
        init()
        setupClicks()
        initAdapter()
        initRecyclerViews()
        observeData()
        setupSearch()
        observeCartBadge()

        if (args.fromFactor) {
            observeCartData()
        }
    }

    private fun init() {
        binding.hfProduct.isShowImgOne = args.fromFactor
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClicks() {
        binding.apply {

            binding.hfProduct.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }
            binding.hfProduct.setOnClickImgOneListener {
                val action =
                    ProductListFragmentDirections.actionProductListFragmentToOrderFragment(
                        args.factorId,
                        args.sabt
                    )
                findNavController().navigate(action)
            }

            etSearch.addTextChangedListener { editable ->
                val query = editable.toString()
                if (query.isNotEmpty()) {
                    // نمایش ضربدر و جستجو
                    binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        clearIcon,
                        null
                    )
                } else {
                    // فقط جستجو، بدون ضربدر
                    binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        searchIcon,
                        null
                    )
                }
            }

            // پاک کردن جستجو با لمس آیکون ضربدر
            etSearch.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = binding.etSearch.compoundDrawablesRelative[2] // drawableEnd
                    drawableEnd?.let {
                        val touchAreaStart =
                            binding.etSearch.width - binding.etSearch.paddingEnd - it.intrinsicWidth
                        if (event.rawX >= touchAreaStart) {
                            binding.etSearch.text?.clear()
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
        }
    }

    private fun initAdapter() {
        productListAdapter = ProductListAdapter(
            loadProduct = { productId, actId ->
                factorViewModel.loadProduct(productId, actId!!)
            },
            factorViewModel = factorViewModel,
            factorId = args.factorId,
            onProductChanged = { item ->
                val validFactorId = factorViewModel.currentFactorId.value ?: args.factorId.toLong()
                if (validFactorId <= 0) {
                    Log.e("ProductList", "Invalid factorId: cannot save detail")
                    return@ProductListAdapter
                }

                factorViewModel.updateHeader(hasDetail = true)

                lifecycleScope.launch {
                    val updatedHeader = factorViewModel.factorHeader.value?.copy(hasDetail = true)
                    updatedHeader?.let {
                        factorViewModel.updateFactorHeader(it)
                    }
                }
                val updatedItem = item.copy(factorId = validFactorId.toInt())
                factorViewModel.addOrUpdateFactorDetail(updatedItem)
            },
            onClickDetail = { product ->
                val action = ProductListFragmentDirections
                    .actionProductListFragmentToProductDetailFragment(productId = product.id)
                findNavController().navigate(action)

            },

            onClickDialog = { product ->
                // 1. تمام داده‌های مورد نیاز را به صورت suspend جمع‌آوری کنید
                lifecycleScope.launch {
                    val factorHeader = factorViewModel.factorHeader.value ?: return@launch

                    val productRate =
                        factorViewModel.getProductRate(product.product.id, factorHeader.actId!!)
                    if (productRate == null) {
                        Toast.makeText(
                            requireContext(),
                            "خطا در دریافت اطلاعات محصول",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    //  val productRate = productWithRate

                    // بررسی وجود ردیف قبلی
                    val existingDetail = try {
                        factorViewModel.getExistingFactorDetail(
                            factorHeader.id,
                            product.product.id
                        )
                    } catch (e: Exception) {
                        null
                    }

                    // دریافت maxId برای ایجاد ردیف جدید
                    val maxId = if ((factorViewModel.getCount().value ?: 0) > 0) {
                        factorViewModel.getMaxFactorDetailId().value ?: 0
                    } else {
                        0
                    }
                    var dialogRef: AddEditProductDialog? = null

                    // 2. نمایش دیالوگ با داده‌های آماده
                    dialogRef = AddEditProductDialog(
                        productViewModel,
                        product
                    ) { finalUnit1, finalPackingValue, packingId, _, _ ->
                        // 3. ایجاد یا به‌روزرسانی ردیف در ViewModel
                        lifecycleScope.launch {
                            try {
                                val validFactorId = factorViewModel.currentFactorId.value
                                    ?: args.factorId.toLong()

                                // ایجاد entity با مقادیر محاسبه‌شده
                                val detail = FactorDetailEntity(
                                    id = existingDetail?.id ?: (maxId + 1),
                                    factorId = validFactorId.toInt(),
                                    sortCode = 0,
                                    anbarId = factorHeader.defaultAnbarId,
                                    productId = product.product.id,
                                    actId = factorHeader.actId,
                                    unit1Value = finalUnit1,
                                    packingValue = finalPackingValue,
                                    unit2Value = 0.0,
                                    price = Math.round(productRate * finalUnit1).toDouble(),
                                    packingId = packingId,
                                    vat = 0.0,
                                    unit1Rate = productRate,
                                    isGift = 0
                                )

                                // ذخیره‌سازی و محاسبه تخفیف در ViewModel

                                factorViewModel.saveProductWithDiscounts(
                                    detail = detail,
                                    factorHeader = factorHeader,
                                    vatPercent = product.vatPercent, // نیاز برای محاسبه بعدی
                                    tollPercent = product.tollPercent
                                )
                                // فقط اینجا دیالوگ بسته شود - پس از اتمام کامل تراکنش
                                dialogRef?.dismiss()
                            } catch (e: Exception) {
                                Log.e("ProductList", "Error saving product", e)
                                Toast.makeText(
                                    requireContext(),
                                    "خطا در ذخیره محصول",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    // نمایش دیالوگ
                    val fm = childFragmentManager
                    fm.findFragmentByTag("AddRawProductDialog")?.let {
                        fm.beginTransaction().remove(it).commitAllowingStateLoss()
                    }
                    dialogRef.show(fm, "AddRawProductDialog")
                }
            }
        )
    }

    private fun initRecyclerViews() {
        binding.rvProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = productListAdapter
        }
    }

    private fun observeCartData() {
        val viewModelId = factorViewModel.currentFactorId.value ?: 0L
        val validFactorId = if (viewModelId > 0) viewModelId else args.factorId.toLong()

        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                // فیلتر نهایی: فقط ردیف‌های عادی (غیر هدیه)
                val nonGiftDetails = details.filter { it.isGift != 1 }

                val values = mutableMapOf<Int, Pair<Double, Double>>()
                nonGiftDetails.forEach { detail ->
                    // فقط از کش بخوان یا تجزیه کن برای ردیف‌های عادی
                    val cached = factorViewModel.productInputCache[detail.productId]
                    if (cached != null) {
                        values[detail.productId] = cached
                    } else {
                        val packingSize = detail.packing?.unit1Value ?: 0.0
                        if (packingSize > 0) {
                            val pack = floor(detail.unit1Value / packingSize)
                            val unit = detail.unit1Value % packingSize
                            values[detail.productId] = Pair(unit, pack)
                        } else {
                            values[detail.productId] = Pair(detail.unit1Value, 0.0)
                        }
                    }
                }
                productListAdapter.updateProductValues(values)
            }
    }
    /*
        private fun observeCartData() {
            val validFactorId = factorViewModel.currentFactorId.value ?: args.factorId.toLong()
            if (validFactorId <= 0) return

            factorViewModel.getFactorDetails(validFactorId.toInt())
                .observe(viewLifecycleOwner) { details ->
                    val values = mutableMapOf<Int, Pair<Double, Double>>()
                    details.forEach { detail ->
                        // ✅ فقط از کش بخوان، اما کش را با مقادیر تجزیه‌شده آپدیت نکن!
                        val cached = factorViewModel.productInputCache[detail.productId]
                        if (cached != null) {
                            values[detail.productId] = cached
                        } else {
                            // فقط برای نمایش در لیست، تجزیه کن (کش را تغییر نده)
                            val packingSize = detail.packing?.unit1Value ?: 0.0
                            if (packingSize > 0) {
                                val pack = floor(detail.unit1Value / packingSize)
                                val unit = detail.unit1Value % packingSize
                                values[detail.productId] = Pair(unit, pack)
                            } else {
                                values[detail.productId] = Pair(detail.unit1Value, 0.0)
                            }
                        }
                    }
                    productListAdapter.updateProductValues(values)
                    // ❌ هرگز این خط را اضافه نکنید: factorViewModel.productInputCache.putAll(values)
                }
        }
    */

    private fun observeData() {
        if (args.fromFactor) {
            productViewModel.loadProductsWithAct(
                groupProductId = null,
                actId = factorViewModel.factorHeader.value?.actId ?: args.actId
            )

            productViewModel.filteredWithActList.observe(viewLifecycleOwner) { list ->
                val images = productViewModel.productImages.value ?: emptyMap()
                updateUI(list, images)
            }
            productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
                val list = productViewModel.filteredWithActList.value ?: emptyList()
                productListAdapter.setProductWithActData(list, imagesMap)
            }
        } else {
            // مشاهده محصولات
            productViewModel.filteredList.observe(viewLifecycleOwner) { filteredProducts ->
                val imagesMap = productViewModel.productImages.value ?: emptyMap()

                if (filteredProducts.isEmpty()) {
                    binding.info.show()
                    binding.info.message(requireContext().getString(R.string.msg_no_product))
                    binding.rvProduct.hide()
                } else {
                    binding.info.gone()
                    binding.rvProduct.show()
                    productListAdapter.setProductData(filteredProducts, imagesMap)
                }
            }

            // مشاهده تغییرات عکس‌ها
            productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
                val products = productViewModel.filteredList.value ?: emptyList()
                productListAdapter.setProductData(products, imagesMap)
            }
        }
    }

    private fun updateUI(
        list: List<ProductWithPacking>,
        images: Map<Int, List<ProductImageEntity>>
    ) {
        if (list.isEmpty()) {
            binding.info.show()
            binding.info.message(getString(R.string.msg_no_product))
            binding.rvProduct.hide()
        } else {
            binding.info.gone()
            binding.rvProduct.show()
            productListAdapter.setProductWithActData(list, images)
        }
    }

    private fun setupSearch() {
        if (args.fromFactor) {
            binding.etSearch.addTextChangedListener { editable ->
                val query = convertNumbersToEnglish(fixPersianChars(editable.toString()))
                productViewModel.filterProductsWithAct(query)
            }
        } else {

            binding.etSearch.addTextChangedListener { editable ->
                val query = convertNumbersToEnglish(fixPersianChars(editable.toString()))
                productViewModel.filterProducts(query)
            }
        }
    }


    private fun observeCartBadge() {
        if (args.fromFactor) {
            val viewModelId = factorViewModel.currentFactorId.value ?: 0L
            val validFactorId = if (viewModelId > 0) viewModelId else args.factorId.toLong()
            if (validFactorId <= 0) return

            factorViewModel.getFactorItemCount(validFactorId.toInt())
                .observe(viewLifecycleOwner) { count ->
                    binding.hfProduct.isShowBadge = count > 0
                    binding.hfProduct.textBadge = count.toString()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}