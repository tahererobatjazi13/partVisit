package com.partsystem.partvisitapp.feature.product.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentProductListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.CartViewModel
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productListAdapter: ProductListAdapter
    private val args: ProductListFragmentArgs by navArgs()

    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

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
    }

    private fun init() {
        binding.hfProduct.isShowImgOne = args.typeShow
    }

    private fun setupClicks() {
        binding.apply {

            binding.hfProduct.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }
            binding.hfProduct.setOnClickImgOneListener {
                val action =
                    ProductListFragmentDirections.actionProductListFragmentToOrderFragment()
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
        val currentQuantities = mutableMapOf<Int, Int>()

        productListAdapter = ProductListAdapter(typeShow = args.typeShow,

            onAddToCart = { item, quantity ->
                cartViewModel.addToCart(item, quantity)
            },
            currentQuantities = currentQuantities,

            onClick = { product ->
                val action = ProductListFragmentDirections
                    .actionProductListFragmentToProductDetailFragment(
                        productId = product.id
                    )
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

/*
    private fun observeData() {
        // لیست محصولات فیلترشده
        productViewModel.filteredList.observe(viewLifecycleOwner) { filteredProducts ->
            if (filteredProducts.isEmpty()) {
                binding.info.show()
                binding.info.message(requireContext().getString(R.string.msg_no_product))
                binding.rvProduct.hide()
            } else {
                binding.info.gone()
                binding.rvProduct.show()
                productListAdapter.setData(
                    filteredProducts,
                    productViewModel.productImages.value ?: emptyMap()
                )

                // بارگذاری عکس‌ها برای این محصولات
                productViewModel.loadProductImages(filteredProducts)
            }
        }

        // مشاهده تغییرات عکس‌ها
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val products = productViewModel.filteredList.value ?: emptyList()
            productListAdapter.setData(products, imagesMap)
        }
    }
*/
    private fun observeData() {

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
                productListAdapter.setData(filteredProducts, imagesMap)

            }
        }

        // مشاهده تغییرات عکس‌ها
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val products = productViewModel.filteredList.value ?: emptyList()
            productListAdapter.setData(products, imagesMap)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString()
            productViewModel.filterProducts(query)
        }
    }

    private fun observeCartBadge() {
        cartViewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.hfProduct.isShowBadge = count > 0
            binding.hfProduct.textBadge = if (count > 0) count.toString() else ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}