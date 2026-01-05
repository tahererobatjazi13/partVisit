package com.partsystem.partvisitapp.feature.report_factor.online.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.online.adapter.OnlineOrderDetailAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class OnlineOrderDetailFragment : Fragment() {
    @Inject
    lateinit var mainPreferences: MainPreferences
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnlineOrderListViewModel by viewModels()

    private lateinit var onlineOrderDetailAdapter: OnlineOrderDetailAdapter
    private val args: OnlineOrderDetailFragmentArgs by navArgs()

    private val formatter = DecimalFormat("#,###,###,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initAdapter()
        setupClicks()
        viewModel.fetchReportFactorDetail(1, args.id)
        setupObserver()
    }

    private fun init() {
        binding.btnEditOrder.gone()
    }

    private fun initAdapter() {
        binding.rvOrderDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            onlineOrderDetailAdapter = OnlineOrderDetailAdapter()
            adapter = onlineOrderDetailAdapter
        }
    }

    private fun setupClicks() {
        binding.apply {
            hfOrderDetail.setOnClickImgTwoListener {
                binding.hfOrderDetail.gone()
                binding.svMain.gone()
                findNavController().navigateUp()
            }

            tryAgain.setOnClickListener {
                viewModel.fetchReportFactorDetail(1, args.id)
                binding.tryAgain.gone()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setupObserver() {

        viewModel.reportFactorDetail.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when (result) {
                    is NetworkResult.Loading -> {
                        loading.show()
                        svMain.gone()
                    }

                    is NetworkResult.Success -> {
                        loading.gone()
                        svMain.show()
                        val orderDetailList = result.data

                        if (orderDetailList.isEmpty()) {
                            svMain.gone()
                            info.show()
                            info.message(getString(R.string.msg_no_data))
                        } else {
                            info.gone()
                            svMain.show()
                            onlineOrderDetailAdapter.submitList(orderDetailList)

                            tvOrderNumber.text = orderDetailList[0].id.toString()
                            tvCustomerName.text = orderDetailList[0].customerName
                            tvDateTime.text =
                                orderDetailList[0].persianDate + " _ " + orderDetailList[0].createTime
                            tvSumPrice.text =
                                formatter.format(orderDetailList[0].sumPrice) + " ریال"
                            tvSumDiscountPrice.text =
                                formatter.format(orderDetailList[0].sumDiscountPrice) + " ریال"
                            tvSumVat.text = formatter.format(orderDetailList[0].sumVat) + " ریال"
                            tvFinalPrice.text =
                                formatter.format(orderDetailList[0].finalPrice) + " ریال"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}