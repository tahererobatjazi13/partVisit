package com.partsystem.partvisitapp.feature.product.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.databinding.FragmentProductDetailBinding
import java.io.File

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ProductDetailFragmentArgs by navArgs()
    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setupClicks()
    }

    private fun observeData() {
        productViewModel.getProductById(args.productId).observe(viewLifecycleOwner) { product ->
            if (product != null) {
                binding.tvProductName.text = product.name ?: ""

                if (!product.description.isNullOrBlank()) {
                    binding.clProductDescription.show()
                    binding.tvProductDescription.text = product.description
                } else {
                    binding.clProductDescription.gone()
                }
            }
        }

        // مشاهده عکس
        productViewModel.productImages.observe(viewLifecycleOwner) { imagesMap ->
            val images = imagesMap[args.productId]
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(binding.ivProduct.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(binding.ivProduct)
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_placeholder)
            }
        }
    }

    private fun setupClicks() {
        binding.apply {
            hfProductDetail.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
