package com.partsystem.partvisitapp.feature.group_product.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.feature.create_order.model.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentGroupProductBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.group_product.adapter.CategoryAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.MainGroupAdapter
import com.partsystem.partvisitapp.feature.group_product.adapter.SubGroupAdapter
import com.partsystem.partvisitapp.feature.product.adapter.ProductListAdapter
import com.partsystem.partvisitapp.feature.product.ui.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupProductFragment : Fragment() {
    @Inject
    lateinit var userPreferences: UserPreferences

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

        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            latestCategoryId?.let { categoryId ->
                val products =
                    groupProductViewModel.getProductsByCategory(categoryId).value ?: emptyList()
             //   productListAdapter.setProductData(products, imagesMap)
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
        val currentQuantities = mutableMapOf<Int, Int>()

        productListAdapter = ProductListAdapter(factorViewModel,
            fromFactor = args.fromFactor, factorId = args.factorId,
            onProductChanged = { item ->
                factorViewModel.addDetail(item)
            },
          //  currentQuantities = currentQuantities,
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
                            //productListAdapter.setProductData(products, imagesMap)
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
        factorViewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.hfGroupProduct.isShowBadge = count > 0
            binding.hfGroupProduct.textBadge = if (count > 0) count.toString() else ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}