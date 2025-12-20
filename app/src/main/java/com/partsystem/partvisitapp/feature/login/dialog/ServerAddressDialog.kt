package com.partsystem.partvisitapp.feature.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.core.utils.datastore.UserPreferences
import com.partsystem.partvisitapp.databinding.DialogServerAddressBinding
import kotlinx.coroutines.launch
import javax.inject.Inject


class ServerAddressDialog(
    private val onSave: (CustomerEntity) -> Unit
) : DialogFragment() {
    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var binding: DialogServerAddressBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogServerAddressBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        binding.btnSave.setOnClickListener {
            val input = binding.tieServerAddress.text.toString().trim()

            if (input.isEmpty()) {
                binding.tilServerAddress.error =
                    binding.root.context.getString(R.string.error_enter_address)
                return@setOnClickListener
            }

            val baseUrl = "http://$input/api/Android/"

            lifecycleScope.launch {
                userPreferences.saveBaseUrl(baseUrl)
                dismiss()
            }
        }


        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

