package com.partsystem.partvisitapp.feature.sync

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.isInternetAvailable
import com.partsystem.partvisitapp.databinding.DialogLoadingBinding
import com.partsystem.partvisitapp.databinding.FragmentSyncSelectionBinding
import com.partsystem.partvisitapp.feature.sync.adapter.SyncListAdapter
import com.partsystem.partvisitapp.feature.sync.ui.SyncSelectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SyncSelectionFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentSyncSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyncSelectionViewModel by viewModels()
    private lateinit var adapter: SyncListAdapter

    private lateinit var loadingDialog: Dialog
    private lateinit var loadingBinding: DialogLoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler()
        setupLoadingDialog()
        setupClicks()
        observeUI()
    }

    private fun setupRecycler() {
        adapter = SyncListAdapter { index, checked ->
            viewModel.updateCheck(index, checked)
        }
        binding.rvSyncList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSyncList.adapter = adapter
    }

    private fun setupClicks() = binding.apply {

        hfSync.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        cbSelectAll.setOnCheckedChangeListener { _, checked ->
            viewModel.selectAll(checked)
            adapter.notifyAllCheckedChanged()
        }

        clSelectAll.setOnClickListener {
            val newState = !cbSelectAll.isChecked
            cbSelectAll.isChecked = newState
        }

        btnSync.setOnClickBtnOneListener {
            val selectedCount = viewModel.items.value?.count { it.isChecked } ?: 0

            if (selectedCount == 0) {
                CustomSnackBar.make(
                    requireView(),
                    getString(R.string.error_select_one_item),
                    SnackBarType.Error.value
                )?.show()
                return@setOnClickBtnOneListener
            }

            lifecycleScope.launch {
                if (!isInternetAvailable(requireContext())) {
                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.error_network_internet),
                        SnackBarType.Error.value
                    )?.show()
                    return@launch
                }

                showOrUpdateLoading("در حال آماده‌سازی دریافت اطلاعات ...")
                viewModel.startSync()
            }
        }
    }

    private fun observeUI() {
        // بروزرسانی لیست
        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // نمایش پیام لودینگ مرحله‌ای
        viewModel.loading.observe(viewLifecycleOwner) { message ->
            showOrUpdateLoading(message)
        }
        viewModel.finished.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                delay(100)
                findNavController().navigateUp()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingBinding = DialogLoadingBinding.inflate(layoutInflater)

        loadingDialog.apply {
            setContentView(loadingBinding.root)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showOrUpdateLoading(message: String) {
        if (!::loadingDialog.isInitialized) setupLoadingDialog()

        loadingBinding.tvLoadingMessage.text = message

        if (!loadingDialog.isShowing) {
            loadingDialog.show()
            loadingDialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.8).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::loadingDialog.isInitialized && loadingDialog.isShowing)
            loadingDialog.dismiss()
    }
}
