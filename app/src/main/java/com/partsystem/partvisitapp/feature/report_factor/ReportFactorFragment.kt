package com.partsystem.partvisitapp.feature.report_factor

import android.content.res.Configuration
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
import com.partsystem.partvisitapp.core.utils.ReportFactorVisitorType
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentReportFactorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportFactorFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

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
                    binding.tabsLayout.show()
                    binding.hfOrderList.show()
                    setActiveTab(true)
                }

                // تب online فعال شود
                R.id.onlineOrderListFragment -> {
                    binding.tabsLayout.show()
                    binding.hfOrderList.show()
                    setActiveTab(false)
                }

                // صفحات دیتیل مخفی شود
                R.id.onlineOrderDetailFragment,
                R.id.offlineOrderDetailFragment -> {
                    binding.tabsLayout.gone()
                    binding.hfOrderList.gone()
                }
            }
        }
    }

    private fun setupClicks() = binding.apply {
        hfOrderList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() = binding.apply {

        tabDraftOrder.setOnClickListener {
            setActiveTab(true)
            navController.navigate(R.id.offlineOrderListFragment)
        }
        tabCurrentOrder.setOnClickListener {
            setActiveTab(false)
            lifecycleScope.launch {
                val visitorId = mainPreferences.personnelId.first() ?: 0
                navController.navigate(
                    R.id.onlineOrderListFragment,
                    Bundle().apply {
                        putString("typeList", ReportFactorListType.Visitor.value)
                        putInt("id", visitorId)
                        putString("typeVisitor", ReportFactorVisitorType.CurrentDay.value)
                    },
                    navOptions {
                        launchSingleTop = true
                        popUpTo(R.id.offlineOrderListFragment) { saveState = true }
                    }
                )
            }
        }
    }

    private fun setActiveTab(unsent: Boolean) = binding.apply {
        // تشخیص تم تاریک
        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            // در تم تاریک: هر دو تب متن سفید دارند
            tabDraftOrder.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            tabCurrentOrder.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

            // پس‌زمینه‌ها بر اساس وضعیت فعال/غیرفعال
            if (unsent) {
                tabDraftOrder.setBackgroundResource(R.drawable.bg_tab_active)
                tabCurrentOrder.setBackgroundResource(R.drawable.bg_tab_inactive)
            } else {
                tabDraftOrder.setBackgroundResource(R.drawable.bg_tab_inactive)
                tabCurrentOrder.setBackgroundResource(R.drawable.bg_tab_active)
            }
        } else {
            // در تم روشن: رفتار قبلی
            if (unsent) {
                tabDraftOrder.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                tabCurrentOrder.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                tabDraftOrder.setBackgroundResource(R.drawable.bg_tab_active)
                tabCurrentOrder.setBackgroundResource(R.drawable.bg_tab_inactive)
            } else {
                tabDraftOrder.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                tabCurrentOrder.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                tabDraftOrder.setBackgroundResource(R.drawable.bg_tab_inactive)
                tabCurrentOrder.setBackgroundResource(R.drawable.bg_tab_active)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
