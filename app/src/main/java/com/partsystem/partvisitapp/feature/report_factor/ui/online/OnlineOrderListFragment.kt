package com.partsystem.partvisitapp.feature.report_factor.ui.online

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.ReportFactorListType
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding
import com.partsystem.partvisitapp.feature.report_factor.adapter.OnlineOrderListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class OnlineOrderListFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnlineOrderListViewModel by viewModels()

    private lateinit var onlineOrderListAdapter: OnlineOrderListAdapter
    private val args: OnlineOrderListFragmentArgs by navArgs()

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupClicks()
        initAdapter()
        setupObserver()
        setupSearch()
    }

    private fun init() {
        if (args.typeList == ReportFactorListType.Visitor.value) {
            viewModel.fetchReportFactorVisitorList(0, args.id)
            binding.hfOrderList.gone()
        } else {
            binding.hfOrderList.show()
            viewModel.fetchReportFactorCustomerList(0, args.id)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClicks() {
        binding.apply {
            tryAgain.setOnClickListener {
                tryAgain.gone()
                if (args.typeList == ReportFactorListType.Visitor.value)
                    viewModel.fetchReportFactorVisitorList(0, args.id)
                else
                    viewModel.fetchReportFactorCustomerList(0, args.id)
            }

            hfOrderList.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }
            etSearch.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val endDrawable = etSearch.compoundDrawablesRelative[2]
                    val touchStart =
                        etSearch.width - etSearch.paddingEnd - (endDrawable?.intrinsicWidth ?: 0)
                    if (event.rawX >= touchStart) {
                        etSearch.text?.clear()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    private fun initAdapter() {
        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            onlineOrderListAdapter = OnlineOrderListAdapter(showSyncButton = false) { factors ->
          /*      binding.rvOrderList.isEnabled = false
                binding.rvOrderList.isClickable = false
                binding.rvOrderList.suppressLayout(true)
*/
                if (factors.finalPrice.toInt() != 0) {
                    val action =
                        OnlineOrderListFragmentDirections.actionOnlineOrderListFragmentToOnlineOrderDetailFragment(
                            factors.id
                        )
                    findNavController().navigate(action)
                } else {
                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.error_not_detail),
                        SnackBarType.Error.value
                    )?.show()
                }
            }
            adapter = onlineOrderListAdapter
        }
    }

    private fun setupObserver() {

        val liveData =
            if (args.typeList == ReportFactorListType.Visitor.value)
                viewModel.reportFactorVisitorList
            else
                viewModel.reportFactorCustomerList

        liveData.observe(viewLifecycleOwner) { result ->

            binding.apply {
                when (result) {

                    is NetworkResult.Loading -> loading.show()

                    is NetworkResult.Success -> {
                        loading.gone()

                        val orderList = result.data
                        if (orderList.isEmpty()) {
                            info.show()
                            info.message(getString(R.string.msg_no_order))
                            rvOrderList.hide()
                        } else {
                            info.gone()
                            rvOrderList.show()
                            onlineOrderListAdapter.submitList(orderList)
                        }
                    }

                    is NetworkResult.Error -> {
                        loading.gone()
                        tryAgain.show()
                        tryAgain.message = result.message
                    }
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->

            val query = editable.toString()

            binding.etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null,
                if (query.isEmpty()) searchIcon else clearIcon,
                null
            )

            if (args.typeList == ReportFactorListType.Visitor.value)
                viewModel.searchVisitorList(query)
            else
                viewModel.searchCustomerList(query)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}