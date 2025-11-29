package com.partsystem.partvisitapp.feature.group_product.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentGroupProductBinding
import com.partsystem.partvisitapp.feature.create_order.ui.CartViewModel
import com.partsystem.partvisitapp.feature.group_product.adapter.CategoryAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.MainGroupAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.SubGroupAdapter
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import com.partsystem.partvisitapp.feature.product.ui.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupProductFragment : Fragment() {

    private var _binding: FragmentGroupProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainGroupAdapter: MainGroupAdapter
    private lateinit var subGroupAdapter: SubGroupAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productListAdapter: ProductListAdapter

    private val args: GroupProductFragmentArgs by navArgs()
    private val groupProductViewModel: GroupProductViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

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

        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            latestCategoryId?.let { categoryId ->
                val products =
                    groupProductViewModel.getProductsByCategory(categoryId).value ?: emptyList()
                productListAdapter.setData(products, imagesMap)
            }
        }
    }

    private fun init() {
        binding.hfGroupProduct.isShowImgOne = args.typeShow
        binding.collapsingToolbar.title = getString(R.string.label_product_group)
    }

    private fun setupClicks() {
        binding.hfGroupProduct.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        binding.hfGroupProduct.setOnClickImgOneListener {
            val action =
                GroupProductFragmentDirections.actionGroupProductFragmentToOrderFragment()
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
        val currentQuantities = mutableMapOf<Int, Int>()

        productListAdapter = ProductListAdapter(
            args.typeShow,
            onAddToCart = { item, quantity ->
                cartViewModel.addToCart(item, quantity)
            },
            currentQuantities = currentQuantities,
            onClick = { product ->
                val action = GroupProductFragmentDirections
                    .actionGroupProductFragmentToProductDetailFragment(
                        productId = product.id
                    )
                findNavController().navigate(action)
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
                        productListAdapter.setData(products, imagesMap)
                    }
                }
            }
    }

    private fun observeCartBadge() {
        cartViewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.hfGroupProduct.isShowBadge = count > 0
            binding.hfGroupProduct.textBadge = if (count > 0) count.toString() else ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}