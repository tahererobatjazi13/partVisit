package com.partsystem.partvisitapp.feature.report_factor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.ReportFactorListType
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.databinding.FragmentReportFactorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportFactorFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var _binding: FragmentReportFactorBinding? = null
    private val binding get() = _binding!!


    private val navController by lazy {
        childFragmentManager.findFragmentById(R.id.reportFactorNavHost)!!
            .findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFactorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!navController.popBackStack()) {
                findNavController().navigateUp()
            }
        }

        setupClicks()
        setupTabs()
        setActiveTab(true)

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {

                // تب offline فعال شود
                R.id.offlineOrderListFragment -> {
                    binding.tabsLayout.visibility = View.VISIBLE
                    binding.hfOrderList.visibility = View.VISIBLE
                    setActiveTab(true)
                }

                // تب online فعال شود
                R.id.onlineOrderListFragment -> {
                    binding.tabsLayout.visibility = View.VISIBLE
                    binding.hfOrderList.visibility = View.VISIBLE
                    setActiveTab(false)
                }

                // صفحات دیتیل مخفی شود
                R.id.onlineOrderDetailFragment,
                R.id.offlineOrderDetailFragment -> {
                    binding.tabsLayout.visibility = View.GONE
                    binding.hfOrderList.visibility = View.GONE
                }
            }
        }

    }

    private fun setupClicks() {
        binding.hfOrderList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() {
        binding.tabUnsent.setOnClickListener {
            setActiveTab(true)
            navController.navigate(R.id.offlineOrderListFragment)
        }
        binding.tabSent.setOnClickListener {
            setActiveTab(false)
            lifecycleScope.launch {
                val visitorId = userPreferences.personnelId.first() ?: 0
                navController.navigate(
                    R.id.onlineOrderListFragment,
                    Bundle().apply {
                        putString("typeList",ReportFactorListType.Visitor.value)
                        putInt("id", visitorId)
                    },
                    navOptions {
                        launchSingleTop = true
                        popUpTo(R.id.offlineOrderListFragment) { saveState = true }
                    }
                )
            }
        }
    }

    private fun setActiveTab(unsent: Boolean) {
        if (unsent) {
            binding.tabUnsent.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabSent.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.tabUnsent.setBackgroundResource(R.drawable.bg_tab_active)
            binding.tabSent.setBackgroundResource(R.drawable.bg_tab_inactive)
        } else {
            binding.tabUnsent.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
            binding.tabSent.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabUnsent.setBackgroundResource(R.drawable.bg_tab_inactive)
            binding.tabSent.setBackgroundResource(R.drawable.bg_tab_active)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
