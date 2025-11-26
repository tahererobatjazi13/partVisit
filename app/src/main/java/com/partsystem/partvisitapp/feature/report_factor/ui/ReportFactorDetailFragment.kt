package com.partsystem.partvisitapp.feature.report_factor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.network.modelDto.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentReportFactorDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.adapter.ReportFactorDetailAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class ReportFactorDetailFragment : Fragment() {
    @Inject
    lateinit var userPreferences: UserPreferences
    private var _binding: FragmentReportFactorDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportFactorListViewModel by viewModels()

    private lateinit var reportFactorDetailAdapter: ReportFactorDetailAdapter
    private var visitorId = 0
    private val args: ReportFactorDetailFragmentArgs by navArgs()

    private val formatter = DecimalFormat("#,###,###,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFactorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        setupClicks()
        setupObserver()
        initVisitorIdAndFetchData()
    }

    private fun initVisitorIdAndFetchData() {
        lifecycleScope.launch {
            visitorId = userPreferences.personnelId.first() ?: 0
            viewModel.fetchReportFactor(1, visitorId)
        }
    }



    private fun initAdapter() {
        binding.rvFactorDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
            reportFactorDetailAdapter = ReportFactorDetailAdapter()
            adapter = reportFactorDetailAdapter
        }
    }

    private fun setupClicks() {
        binding.apply {
            hfFactorDetail.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            tryAgain.setOnClickListener {
                viewModel.fetchReportFactor(1, visitorId)
                binding.tryAgain.gone()
            }
        }
    }

    private fun setupObserver() {
        viewModel.reportFactors.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when (result) {
                    is NetworkResult.Loading -> {
                        loading.show()
                        svMain.gone()
                    }

                    is NetworkResult.Success -> {
                        loading.gone()
                        svMain.show()

                        val groups = result.data

                        // فیلتر بر اساس ID
                        val filteredList = groups.filter { it.id == args.id }

                        if (filteredList.isEmpty()) {
                            rvFactorDetail.gone()
                            info.show()
                            info.message(getString(R.string.msg_no_data))
                        } else {
                            info.gone()

                            rvFactorDetail.show()
                            reportFactorDetailAdapter.submitList(filteredList)

                            tvNumber.text= filteredList[0].id.toString()
                            tvNumber.text=filteredList[0].customerName
                            tvDateTime.text=filteredList[0].persianDate+" _ "+filteredList[0].createTime
                            tvFinalPrice.text=formatter.format(filteredList[0].finalPrice)
                        }
                    }

                    is NetworkResult.Error -> {
                        loading.gone()
                        tryAgain.show()
                        tryAgain.message = result.message
                    }

                    else -> {
                        loading.gone()
                    }
                }
            }
        }
    }


    private fun calculateTotalPrices(items: List<ReportFactorDto>?) {
        items ?: return
        //  val total = items.sumOf { it.price }
        with(binding) {
            //  tvTotalOrder.text = "${formatter.format(total)} ریال"
            //  tvDiscountOrder.text = formatter.format(0) // جایگزین با مقدار واقعی
            //  tvTotalDiscount.text = formatter.format(0) // جایگزین با مقدار واقعی
            //  tvTotalPrice.text = formatter.format(total) + " ریال"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
