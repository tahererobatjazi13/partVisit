package com.partsystem.partvisitapp.feature.report_factor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentReportFactorListBinding
import com.partsystem.partvisitapp.feature.report_factor.adapter.ReportFactorListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportFactorListFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var _binding: FragmentReportFactorListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportFactorListViewModel by viewModels()

    private lateinit var reportFactorListAdapter: ReportFactorListAdapter
    private var visitorId = 0

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
        initVisitorIdAndFetchData()
    }

    private fun initVisitorIdAndFetchData() {
        lifecycleScope.launch {
            visitorId = userPreferences.personnelId.first() ?: 0
            viewModel.fetchReportFactor(0, visitorId)
        }
    }

    private fun initAdapter() {
        binding.rvFactorList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)

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

    private fun setupClicks() {
        binding.tryAgain.setOnClickListener {
            viewModel.fetchReportFactor(0, visitorId)
            binding.tryAgain.gone()
        }

        binding.hfFactorList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObserver() {
        viewModel.reportFactors.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when (result) {
                    is NetworkResult.Loading -> {
                        loading.show()
                    }

                    is NetworkResult.Success -> {
                        loading.gone()
                        val groups = result.data
                        if (groups.isEmpty()) {
                            info.show()
                            info.message(getString(R.string.msg_no_data))
                            rvFactorList.gone()
                        } else {
                            info.gone()
                            rvFactorList.show()
                            reportFactorListAdapter.submitList(groups)
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
