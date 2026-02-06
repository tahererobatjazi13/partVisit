package com.partsystem.partvisitapp.feature.group_product.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentGroupProductBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.group_product.adapter.CategoryAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.MainGroupAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.SubGroupAdapter
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import com.partsystem.partvisitapp.feature.product.dialog.AddEditProductDialog
import com.partsystem.partvisitapp.feature.product.ui.ProductListFragmentDirections
import com.partsystem.partvisitapp.feature.product.ui.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class GroupProductFragment : Fragment() {
    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentGroupProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainGroupAdapter: MainGroupAdapter
    private lateinit var subGroupAdapter: SubGroupAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productListAdapter: ProductListAdapter

    private val args: GroupProductFragmentArgs by navArgs()
    private val groupProductViewModel: GroupProductViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()

    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private var latestMainGroupId: Int? = null
    private var latestSubGroupId: Int? = null
    private var latestCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupClicks()
        initAdapters()
        initRecyclerViews()
        observeData()
        observeCartBadge()
        if (args.fromFactor) {
            observeCartData()
        }
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            latestCategoryId?.let { categoryId ->
                val products =
                    groupProductViewModel.getProductsByCategory(categoryId).value ?: emptyList()
                productListAdapter.setProductData(products, imagesMap)
            }
        }
    }

    private fun init() {
        binding.hfGroupProduct.isShowImgOne = args.fromFactor
        binding.collapsingToolbar.title = getString(R.string.label_product_group)
    }

    private fun setupClicks() {
        binding.hfGroupProduct.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
        binding.hfGroupProduct.setOnClickImgOneListener {
            val action =
                GroupProductFragmentDirections.actionGroupProductFragmentToOrderFragment(args.factorId)
            findNavController().navigate(action)
        }
    }

    private fun initAdapters() {
        mainGroupAdapter = MainGroupAdapter { group ->
            observeSubGroup(group.id)
        }

        subGroupAdapter = SubGroupAdapter { sub ->
            observeCategory(sub.id)
        }

        categoryAdapter = CategoryAdapter { category ->
            latestCategoryId = category.id
            observeProductsByCategory(category.id)
        }

        productListAdapter = ProductListAdapter(loadProduct = { productId, actId ->
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

                    val productWithRate =
                        factorViewModel.getProductRate(product.product.id, factorHeader.actId!!)
                    if (productWithRate == null) {
                        Toast.makeText(
                            requireContext(),
                            "خطا در دریافت اطلاعات محصول",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    val productRate = productWithRate

                    // بررسی وجود ردیف قبلی
                    val existingDetail = try {
                        factorViewModel.getExistingFactorDetail(
                            factorHeader.id!!,
                            product.product.id
                        )
                    } catch (e: Exception) {
                        null
                    }


                    // دریافت maxId برای ایجاد ردیف جدید
                    val maxId = if (factorViewModel.getCount().value ?: 0 > 0) {
                        factorViewModel.getMaxFactorDetailId().value ?: 0
                    } else {
                        0
                    }

                    // 2. نمایش دیالوگ با داده‌های آماده
                    val dialog = AddEditProductDialog(
                        productViewModel,
                        product
                    ) { finalUnit1, finalPackingValue, packingId, _, _ ->
                        // 3. ایجاد یا به‌روزرسانی ردیف در ViewModel
                        lifecycleScope.launch {
                            val validFactorId = factorViewModel.currentFactorId.value
                                ?: args.factorId.toLong()

                            // ایجاد entity با مقادیر محاسبه‌شده
                            val detail = FactorDetailEntity(
                                id = existingDetail?.id ?: (maxId + 1),
                                factorId = validFactorId.toInt(),
                                sortCode = existingDetail?.sortCode ?: (maxId + 1),
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

                            // محاسبه VAT
                            detail.vat =
                                Math.round(product.vatPercent * detail.getPriceAfterDiscount())
                                    .toDouble()

                       /*     // ذخیره‌سازی و محاسبه تخفیف در ViewModel
                            factorViewModel.saveProductWithDiscounts(
                                detail = detail,
                                factorHeader = factorHeader,
                                productRate = productRate*//*,
                                hasExistingDetail = existingDetail != null*//*
                            )*/
                        }
                    }

                    // نمایش دیالوگ
                    val fm = childFragmentManager
                    fm.findFragmentByTag("AddRawProductDialog")?.let {
                        fm.beginTransaction().remove(it).commitAllowingStateLoss()
                    }
                    dialog.show(fm, "AddRawProductDialog")
                }
            }
        )
    }

    private fun initRecyclerViews() {
        binding.rvMainGroup.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            adapter = mainGroupAdapter
        }
        binding.rvSubGroup.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            adapter = subGroupAdapter
        }
        binding.rvCategory.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        binding.rvProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = productListAdapter
        }
    }

    private fun observeData() {
        groupProductViewModel.mainGroupList.observe(viewLifecycleOwner) { mainGroup ->
            if (mainGroup.isNullOrEmpty()) {
                binding.info.show()
                binding.info.message(getString(R.string.msg_no_data))
                binding.nested.gone()
            } else {
                binding.info.gone()
                mainGroupAdapter.setData(mainGroup)
                val id = mainGroup[0].id
                observeSubGroup(id)
            }
        }
    }

    private fun observeCartData() {
        val validFactorId =
            factorViewModel.currentFactorId.value ?: args.factorId.toLong()
        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                val values = mutableMapOf<Int, Pair<Double, Double>>()

                details.forEach { detail ->
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

    private fun observeSubGroup(mainGroupId: Int) {
        latestMainGroupId = mainGroupId
        groupProductViewModel.getSubGroups(mainGroupId)
            .observe(viewLifecycleOwner) { subs ->
                if (latestMainGroupId == mainGroupId) {
                    if (subs.isNullOrEmpty()) {
                        binding.info.show()
                        binding.info.message(getString(R.string.msg_no_data))
                        binding.rvSubGroup.gone()
                        binding.rvCategory.gone()
                        binding.rvProduct.gone()
                        binding.infoProduct.gone()
                        binding.tvTitleProduct.gone()
                    } else {
                        binding.info.gone()
                        binding.rvSubGroup.show()
                        subGroupAdapter.resetSelection()
                        subGroupAdapter.setData(subs)
                        observeCategory(subs[0].id)
                    }
                }
            }
    }

    private fun observeCategory(subGroupId: Int) {
        latestSubGroupId = subGroupId
        latestCategoryId = null
        groupProductViewModel.getCategories(subGroupId)
            .observe(viewLifecycleOwner) { categories ->
                if (latestSubGroupId == subGroupId) {
                    if (categories.isNullOrEmpty()) {
                        binding.info.show()
                        binding.info.message(getString(R.string.msg_no_data))
                        binding.rvProduct.gone()
                        binding.infoProduct.gone()
                        binding.tvTitleProduct.gone()
                        binding.rvCategory.gone()
                    } else {
                        binding.info.gone()
                        binding.rvCategory.show()
                        categoryAdapter.resetSelection()
                        categoryAdapter.setData(categories)
                        observeProductsByCategory(categories[0].id)
                    }
                }
            }

        productViewModel.groupProductImages.observe(viewLifecycleOwner) { imagesMap ->
            if (latestSubGroupId == subGroupId) {
                categoryAdapter.setImages(imagesMap)
            }
        }
    }

    private fun observeProductsByCategory(categoryId: Int) {

        if (args.fromFactor) {
            productViewModel.loadProductsWithAct(
                groupProductId = categoryId,
                actId = factorViewModel.factorHeader.value?.actId
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

            latestCategoryId = categoryId
            groupProductViewModel.getProductsByCategory(categoryId)
                .observe(viewLifecycleOwner) { products ->
                    if (latestCategoryId == categoryId) {
                        val imagesMap = productViewModel.productImages.value ?: emptyMap()
                        if (products.isNullOrEmpty()) {
                            binding.infoProduct.show()
                            binding.tvTitleProduct.show()
                            binding.infoProduct.message(getString(R.string.msg_no_product))
                            binding.rvProduct.gone()
                        } else {
                            binding.infoProduct.gone()
                            binding.tvTitleProduct.show()
                            binding.rvProduct.show()
                            productListAdapter.setProductData(products, imagesMap)
                        }
                    }
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

    private fun observeCartBadge() {
        if (args.fromFactor) {
            val validFactorId = factorViewModel.currentFactorId.value ?: args.factorId.toLong()
            if (validFactorId <= 0) return

            factorViewModel.getFactorItemCount(validFactorId.toInt())
                .observe(viewLifecycleOwner) { count ->
                    binding.hfGroupProduct.isShowBadge = count > 0
                    binding.hfGroupProduct.textBadge = count.toString()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}