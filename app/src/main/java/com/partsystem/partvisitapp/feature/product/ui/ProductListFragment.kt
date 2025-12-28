package com.partsystem.partvisitapp.feature.product.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentProductListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.floor

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
                    ProductListFragmentDirections.actionProductListFragmentToOrderFragment(args.factorId)
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
            factorViewModel = factorViewModel,
            fromFactor = args.fromFactor,
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

            onClick = { product ->
                val action = ProductListFragmentDirections
                    .actionProductListFragmentToProductDetailFragment(productId = product.id)
                findNavController().navigate(action)
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


    private fun observeData() {
        if (args.fromFactor) {
            productViewModel.loadProductsWithAct(
                groupProductId = null,
                actId = factorViewModel.factorHeader.value?.actId
            )
            Log.d("factoractId", factorViewModel.factorHeader.value?.actId.toString())

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
                val query = editable.toString()
                productViewModel.filterProductsWithAct(query)
            }
        } else {
            binding.etSearch.addTextChangedListener { editable ->
                val query = editable.toString()
                productViewModel.filterProducts(query)
            }
        }
    }


    private fun observeCartBadge() {
        if (args.fromFactor) {

        val validFactorId = factorViewModel.currentFactorId.value ?: args.factorId.toLong()
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