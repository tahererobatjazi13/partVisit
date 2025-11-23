package com.partsystem.partvisitapp.feature.customer.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.CustomerEntity
import com.partsystem.partvisitapp.databinding.DialogAddEditCustomerBinding

class AddEditCustomerDialog(
    private val customer: CustomerEntity? = null,
    private val onSave: (CustomerEntity) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddEditCustomerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddEditCustomerBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        binding.tvTitleDialog.text = if (customer == null) {
            getString(R.string.label_add_customer)
        } else {
            getString(R.string.label_edit_customer)
        }
        customer?.let {
            binding.etCustomerName.setText(it.name)
           // binding.etCustomerPhone.setText(it.phone)
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etCustomerName.text.toString()
            val phone = binding.etCustomerPhone.text.toString()

            if (name.isNotBlank() && phone.isNotBlank()) {
                dismiss()
            } else {
                Toast.makeText(context, R.string.error_request_fields, Toast.LENGTH_SHORT).show()
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

