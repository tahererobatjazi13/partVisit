package com.partsystem.partvisitapp.feature.report_factor.ui

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
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentReportFactorListBinding
import com.partsystem.partvisitapp.feature.report_factor.adapter.ReportFactorListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class ReportFactorListFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var _binding: FragmentReportFactorListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportFactorListViewModel by viewModels()

    private lateinit var reportFactorListAdapter: ReportFactorListAdapter
    private val args: ReportFactorListFragmentArgs by navArgs()

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFactorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        setupClicks()
        setupObserver()
        setupSearch()
        initVisitorId()
    }

    private fun initVisitorId() {

        if (args.typeList == ReportFactorListType.Visitor.value) {
            viewModel.fetchReportFactorVisitorList(0, args.id)
        } else {
            viewModel.fetchReportFactorCustomerList(0, args.id)

        }
    }

    private fun initAdapter() {
        binding.rvFactorList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            reportFactorListAdapter = ReportFactorListAdapter { factors ->
                val action =
                    ReportFactorListFragmentDirections.actionReportFactorListFragmentToReportFactorDetailFragment(
                        factors.id
                    )
                findNavController().navigate(action)
            }
            adapter = reportFactorListAdapter
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

            hfFactorList.setOnClickImgTwoListener {
                findNavController().navigateUp()
            }

            etSearch.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val endDrawable = etSearch.compoundDrawablesRelative[2]
                    val touchStart = etSearch.width - etSearch.paddingEnd - (endDrawable?.intrinsicWidth ?: 0)
                    if (event.rawX >= touchStart) {
                        etSearch.text?.clear()
                        return@setOnTouchListener true
                    }
                }
                false
            }
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

                            val data = result.data
                            if (data.isEmpty()) {
                                info.show()
                                info.message(getString(R.string.msg_no_factor))
                                rvFactorList.hide()
                            } else {
                                info.gone()
                                rvFactorList.show()
                                reportFactorListAdapter.submitList(data)
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
