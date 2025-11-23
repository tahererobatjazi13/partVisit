/*
package ir.kitgroup.store.feature.main.createOrder.ui.single_distribution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.kitgroup.store.databinding.BottomSheetCarInfoBinding
import com.partsystem.partvisitapp.feature.order_online.model.Order

// EditItemBottomSheet.kt
class CarInfoBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCarInfoBinding? = null
    private val binding get() = _binding!!
//
//    override fun getTheme(): Int =
//        com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCarInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val id = requireArguments().getString(ARG_ID)
        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val value = requireArguments().getString(ARG_VALUE).orEmpty()
        val qty = requireArguments().getString(ARG_QTY)

        binding.tvHeader.text = "نام مشتری: $title"
        //  binding.etValue.setText(value)
        //if (qty != 0.0) binding.etQuantity.setText(qty.toString())

        binding.btnSave.setOnClickListener {
            val driverName = binding.etDriverName.text?.toString()?.trim().orEmpty()
            val driverPhone = binding.etDriverPhone.text?.toString()?.trim().orEmpty()
            val vehicleType = binding.etVehicleType.text?.toString()?.trim().orEmpty()

            if (driverName.isBlank()) {
                binding.etDriverName.error = "این فیلد الزامی است"
                return@setOnClickListener
            }
            if (driverPhone.isBlank()) {
                binding.etDriverName.error = "این فیلد الزامی است"
                return@setOnClickListener
            }
            if (vehicleType.isBlank()) {
                binding.etDriverName.error = "این فیلد الزامی است"
                return@setOnClickListener
            }

            // ارسال نتیجه به والد
            val result = Bundle().apply {
              //  putString(ARG_ID, id)
                putString(ARG_VALUE, driverName)
                //   putString(ARG_QTY, newQty)
            }
            parentFragmentManager.setFragmentResult(REQ_EDIT_ITEM, result)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQ_EDIT_ITEM = "edit_item_request"
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_VALUE = "value"
        private const val ARG_QTY = "qty"

        fun newInstance(item: Order) = CarInfoBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, item.agentName)
                putString(ARG_TITLE, item.customerName)
                putString(ARG_VALUE, item.address)
                putString(ARG_QTY, item.phone)
            }
        }
    }
}
*/
