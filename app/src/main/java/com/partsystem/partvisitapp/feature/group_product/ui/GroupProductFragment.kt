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
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
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
    private var cartValuesCache: Map<Int, Pair<Double, Double>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupClicks()
        initAdapters()
        initRecyclerViews()
        observeMainGroups()
        observeCartBadge()
        if (args.fromFactor) {
            observeCartData()
            factorViewModel.setCurrentFactorId(args.factorId.toLong())
        }
        observeProductImages()
    }

    private fun initView() = with(binding) {
        hfGroupProduct.isShowImgOne = args.fromFactor
        collapsingToolbar.title = getString(R.string.label_product_group)
    }

    private fun setupClicks() = binding.apply {
        hfGroupProduct.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
        hfGroupProduct.setOnClickImgOneListener {
            val action =
                GroupProductFragmentDirections.actionGroupProductFragmentToOrderFragment(
                    args.factorId,
                    args.sabt
                )
            findNavController().navigate(action)
        }
    }

    private fun initAdapters() {
        mainGroupAdapter = MainGroupAdapter { group ->
            observeSubGroup(group.id)
        }

        subGroupAdapter = SubGroupAdapter { sub ->
            observeCategories(sub.id)
        }

        categoryAdapter = CategoryAdapter { category ->
            latestCategoryId = category.id
            observeProducts(category.id)
        }

        productListAdapter = ProductListAdapter(
            onClickDetail = { product ->
                val action = GroupProductFragmentDirections
                    .actionGroupProductFragmentToProductDetailFragment(productId = product.id)
                findNavController().navigate(action)

            },

            onClickDialog = { product ->
                showAddProductDialog(product)
            })
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
        rvMainGroup.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            adapter = mainGroupAdapter
        }
        rvSubGroup.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            adapter = subGroupAdapter
        }
        rvCategory.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            adapter = categoryAdapter
        }
        rvProduct.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = productListAdapter
        }
    }

    private fun observeMainGroups() = binding.apply {
        groupProductViewModel.mainGroupList.observe(viewLifecycleOwner) { mainGroup ->
            if (mainGroup.isNullOrEmpty()) {
                info.show()
                info.message(getString(R.string.msg_no_data))
                nested.gone()
            } else {
                info.gone()
                mainGroupAdapter.setData(mainGroup)
                observeSubGroup(mainGroup.first().id)
            }
        }
    }

    private fun observeCartData() {
        val validFactorId = getValidFactorId()

        if (validFactorId <= 0) return

        factorViewModel.getFactorDetails(validFactorId.toInt())
            .observe(viewLifecycleOwner) { details ->
                val nonGiftDetails = details.filter { it.isGift != 1 }

                val values = mutableMapOf<Int, Pair<Double, Double>>()
                nonGiftDetails.forEach { detail ->
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

                // ذخیره در کش کلاس
                cartValuesCache = values

                // اعمال روی آداپتر
                productListAdapter.updateProductValues(values)
            }
    }

    private fun reapplyCartValues() {
        if (cartValuesCache.isNotEmpty()) {
            productListAdapter.updateProductValues(cartValuesCache)
        }
    }

    private fun observeSubGroup(mainGroupId: Int) = binding.apply {
        latestMainGroupId = mainGroupId
        groupProductViewModel.getSubGroups(mainGroupId)
            .observe(viewLifecycleOwner) { subs ->
                if (latestMainGroupId == mainGroupId) {
                    if (subs.isNullOrEmpty()) {
                        info.show()
                        info.message(getString(R.string.msg_no_data))
                        rvSubGroup.gone()
                        rvCategory.gone()
                        rvProduct.gone()
                        infoProduct.gone()
                        tvTitleProduct.gone()
                    } else {
                        info.gone()
                        rvSubGroup.show()
                        subGroupAdapter.resetSelection()
                        subGroupAdapter.setData(subs)
                        observeCategories(subs.first().id)
                    }
                }
            }
    }

    private fun observeCategories(subGroupId: Int) = binding.apply {
        latestSubGroupId = subGroupId
        latestCategoryId = null
        groupProductViewModel.getCategories(subGroupId)
            .observe(viewLifecycleOwner) { categories ->
                if (latestSubGroupId == subGroupId) {
                    if (categories.isNullOrEmpty()) {
                        info.show()
                        info.message(getString(R.string.msg_no_data))
                        rvProduct.gone()
                        infoProduct.gone()
                        tvTitleProduct.gone()
                        rvCategory.gone()
                    } else {
                        info.gone()
                        rvCategory.show()
                        categoryAdapter.resetSelection()
                        categoryAdapter.setData(categories)
                        observeProducts(categories.first().id)
                    }
                }
            }

        productViewModel.groupProductImages.observe(viewLifecycleOwner) { imagesMap ->
            if (latestSubGroupId == subGroupId) {
                categoryAdapter.setImages(imagesMap)
            }
        }
    }

    private fun observeProducts(categoryId: Int) {
        if (args.fromFactor) {
            observeProductsWithAct(categoryId)
        } else {
            observeNormalProducts(categoryId)
        }
    }

    private fun observeProductsWithAct(categoryId: Int) {

        productViewModel.loadProductsWithAct(
            groupProductId = categoryId,
            actId = factorViewModel.factorHeader.value?.actId ?: args.actId
        )

        productViewModel.filteredWithActList.observe(viewLifecycleOwner) { list ->
            val images = productViewModel.productImages.value ?: emptyMap()
            updateUI(list, images)
            reapplyCartValues()
        }
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val list = productViewModel.filteredWithActList.value ?: emptyList()
            productListAdapter.setProductWithActData(list, imagesMap)
            reapplyCartValues()
        }
    }

    private fun observeNormalProducts(categoryId: Int) {
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
                        reapplyCartValues()
                    }
                }
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
            reapplyCartValues()
        }
    }

    private fun observeProductImages() {
        productViewModel.productImages.observe(viewLifecycleOwner) { images ->

            latestCategoryId?.let { categoryId ->

                val products =
                    groupProductViewModel
                        .getProductsByCategory(categoryId)
                        .value ?: emptyList()

                productListAdapter.setProductData(products, images)

                reapplyCartValues()
            }
        }
    }

    private fun observeCartBadge() {

        if (!args.fromFactor) return

        val factorId = getValidFactorId()

        if (factorId <= 0) return

        factorViewModel.getFactorItemCount(factorId.toInt())
            .observe(viewLifecycleOwner) { count ->

                binding.hfGroupProduct.isShowBadge = count > 0
                binding.hfGroupProduct.textBadge = count.toString()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}