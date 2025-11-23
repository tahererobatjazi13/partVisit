package com.partsystem.partvisitapp.feature.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.databinding.DialogSettingLoginBinding

class AddSettingLoginDialog(
    private val onSave: (CustomerEntity) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogSettingLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSettingLoginBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        binding.btnSave.setOnClickListener {

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

