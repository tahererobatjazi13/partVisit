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
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
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
        initView()
        setupClicks()
        setupClearIcon()
        initAdapter()
        initRecyclerViews()
        observeProducts()
        setupSearch()
        observeCartBadge()

        if (args.fromFactor) {
            observeCartData()
            factorViewModel.setCurrentFactorId(args.factorId.toLong())
        }
    }

    private fun initView() = with(binding) {
        hfProduct.isShowImgOne = args.fromFactor
    }

    private fun setupClicks() = binding.apply {
        hfProduct.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
        hfProduct.setOnClickImgOneListener {
            val action =
                ProductListFragmentDirections.actionProductListFragmentToOrderFragment(
                    args.factorId,
                    args.sabt
                )
            findNavController().navigate(action)
        }
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

    private fun initAdapter() {
        productListAdapter = ProductListAdapter(
            onClickDetail = { product ->
                val action = ProductListFragmentDirections
                    .actionProductListFragmentToProductDetailFragment(productId = product.id)
                findNavController().navigate(action)
            },

            onClickDialog = { product ->
                showAddProductDialog(product)
            }
        )
    }

    private fun showAddProductDialog(product: ProductWithPacking) {

        lifecycleScope.launch {

            try {

                val factorHeader = getFactorHeader()

                val productRate =
                    factorViewModel.getProductWithRateAct(
                        product.product.id,
                        factorHeader.actId!!
                    )

                if (productRate == null) {

                    Toast.makeText(
                        requireContext(),
                        "خطا در دریافت اطلاعات محصول",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val existingDetail =
                    factorViewModel.getExistingFactorDetail(
                        factorHeader.id,
                        product.product.id
                    )

                val maxId =
                    factorViewModel.getMaxFactorDetailId().value ?: 0

                var dialogRef: AddEditProductDialog? = null

                dialogRef = AddEditProductDialog(
                    productViewModel,
                    product
                ) { finalUnit1, finalPackingValue, packingId, _, _ ->

                    lifecycleScope.launch {

                        try {

                            val factorId =
                                getValidFactorId().toInt()

                            val detail = FactorDetailEntity(
                                id = existingDetail?.id ?: (maxId + 1),
                                factorId = factorId,
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

                            factorViewModel.saveProductWithDiscounts(
                                detail = detail,
                                factorHeader = factorHeader,
                                vatPercent = product.vatPercent,
                                tollPercent = product.tollPercent
                            )

                            dialogRef?.dismiss()

                        } catch (e: Exception) {

                            Log.e("GroupProduct", "Save error", e)

                            Toast.makeText(
                                requireContext(),
                                "خطا در ذخیره محصول",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                childFragmentManager
                    .findFragmentByTag("AddRawProductDialog")
                    ?.let {
                        childFragmentManager.beginTransaction()
                            .remove(it)
                            .commitAllowingStateLoss()
                    }

                dialogRef.show(
                    childFragmentManager,
                    "AddRawProductDialog"
                )

            } catch (e: Exception) {

                Log.e("GroupProduct", "Dialog error", e)
            }
        }
    }

    private fun getValidFactorId(): Long {

        val viewModelId =
            factorViewModel.currentFactorId.value ?: 0L

        return if (viewModelId > 0) {
            viewModelId
        } else {
            args.factorId.toLong()
        }
    }

    private fun isEditMode(): Boolean {
        return args.factorId > 0
    }

    private suspend fun getFactorHeader(): FactorHeaderEntity {
        return if (isEditMode()) {
            // فاکتور ذخیره شده → دیتابیس
            factorViewModel.getFactorHeaderFromDb(args.factorId)
        } else {
            // فاکتور در حال ساخت → SharedViewModel
            factorViewModel.factorHeader.value
                ?: throw IllegalStateException("FactorHeader is null")
        }
    }

    private fun initRecyclerViews() = binding.apply {
        rvProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = productListAdapter
        }
    }

    private fun observeCartData() {
        val validFactorId = getValidFactorId()

        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(args.factorId)
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

    private fun observeProducts() = binding.apply {
        if (args.fromFactor) {
            observeProductsWithAct()
        } else {
            observeNormalProducts()
        }
    }

    private fun observeProductsWithAct() {

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
    }

    private fun observeNormalProducts() = binding.apply {
        // مشاهده محصولات
        productViewModel.filteredList.observe(viewLifecycleOwner) { filteredProducts ->
            val imagesMap = productViewModel.productImages.value ?: emptyMap()

            if (filteredProducts.isEmpty()) {
                info.show()
                info.message(requireContext().getString(R.string.msg_no_product))
                rvProduct.hide()
            } else {
                info.gone()
                rvProduct.show()
                productListAdapter.setProductData(filteredProducts, imagesMap)
            }
        }

        // مشاهده تغییرات عکس‌ها
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val products = productViewModel.filteredList.value ?: emptyList()
            productListAdapter.setProductData(products, imagesMap)
        }
    }

    private fun updateUI(
        list: List<ProductWithPacking>,
        images: Map<Int, List<ProductImageEntity>>
    ) = binding.apply {
        if (list.isEmpty()) {
            info.show()
            info.message(getString(R.string.msg_no_product))
            rvProduct.hide()
        } else {
            info.gone()
            rvProduct.show()
            productListAdapter.setProductWithActData(list, images)
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

            if (args.fromFactor) {
                productViewModel.filterProductsWithAct(query)
            } else {
                productViewModel.filterProducts(query)
            }
        }
    }

    private fun observeCartBadge() {
        if (!args.fromFactor) return

        val factorId = getValidFactorId()

        if (factorId <= 0) return

        factorViewModel.getFactorItemCount(factorId.toInt())
            .observe(viewLifecycleOwner) { count ->

                binding.hfProduct.isShowBadge = count > 0
                binding.hfProduct.textBadge = count.toString()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}