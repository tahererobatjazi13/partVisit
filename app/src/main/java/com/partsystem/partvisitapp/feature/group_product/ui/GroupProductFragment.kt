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
import com.partsystem.partvisitapp.feature.product.ui.ProductListFragmentDirections
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

        // 🟢 ابتدا آداپترها را مقداردهی کن
        mainGroupAdapter = MainGroupAdapter { group ->
            observeSubGroup(group.id)
        }

        subGroupAdapter = SubGroupAdapter { sub ->
            observeCategory(sub.id)
        }
        categoryAdapter = CategoryAdapter { category ->
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
        groupProductViewModel.getSubGroups(mainGroupId)
            .observe(viewLifecycleOwner) { subs ->
         /*       if (subs.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(getString(R.string.msg_no_data))
                    binding.rvProduct.gone()
                } else {*/
                 //   binding.info.gone()
                    subGroupAdapter.setData(subs)
                    val id = subs[0].id
                    observeCategory(id)
                //}
            }
    }

    private fun observeCategory(subGroupId: Int) {
        groupProductViewModel.getCategories(subGroupId)
            .observe(viewLifecycleOwner) { categories ->
//                if (categories.isNullOrEmpty()) {
//                    binding.info.show()
//                    binding.info.message(getString(R.string.msg_no_data))
//                    binding.rvProduct.gone()
//                } else {
                 //   binding.info.gone()
                    categoryAdapter.setData(categories)
                    val id = categories[0].id
                    observeProductsByCategory(id)
               // }
            }
    }

/*    private fun observeProductsByCategory(categoryId: Int) {
        groupProductViewModel.getProductsByCategory(categoryId)
            .observe(viewLifecycleOwner) { products ->
                if (products.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(getString(R.string.msg_no_product))
                    binding.rvProduct.gone()
                } else {
                    binding.info.gone()
                    binding.rvProduct.show()
                 //   productListAdapter.setData(products)
                }
            }
    }*/

    private fun observeProductsByCategory(categoryId: Int) {
        groupProductViewModel.getProductsByCategory(categoryId)
            .observe(viewLifecycleOwner) { products ->
                val imagesMap = productViewModel.productImages.value ?: emptyMap()

                if (products.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(getString(R.string.msg_no_product))
                    binding.rvProduct.gone()
                } else {
                    binding.info.gone()
                    binding.rvProduct.show()
                    productListAdapter.setData(products, imagesMap)
                }
            }

        // این بخش برای آپدیت شدن عکس‌ها بعد از لود شدن
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val products = groupProductViewModel.getProductsByCategory(categoryId).value ?: emptyList()
            productListAdapter.setData(products, imagesMap)
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