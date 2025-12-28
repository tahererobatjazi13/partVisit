package com.partsystem.partvisitapp.core.utils.componenet

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.databinding.CustomDialogBinding

class CustomDialog {

    //region Interface Clear Button
    interface ClickClearButton {
        fun onClick(active: Boolean, SIG: String?)
    }

    private var clickClearButton: ClickClearButton? = null

    fun setOnClickClearButton(clickClearButton: ClickClearButton?) {
        this.clickClearButton = clickClearButton
    }
    //endregion Interface Clear Button

    //region Interface Negative Button
    interface ClickNegativeButton {
        fun onClick()
    }
    private var clickNegativeButton: (() -> Unit)? = null

    fun setOnClickNegativeButton(clickNegativeButton: () -> Unit) {
        this.clickNegativeButton = clickNegativeButton
    }
    //endregion Interface Negative Button

    //region Interface Positive Button
    interface ClickPositiveButton {
        fun onClick()
    }

    private var clickPositiveButton: (() -> Unit)? = null

    fun setOnClickPositiveButton(clickPositiveButton: () -> Unit) {
        this.clickPositiveButton = clickPositiveButton
    }
    //endregion Interface Positive Button

    private var mDialog: Dialog? = null
    private var binding: CustomDialogBinding? = null // ViewBinding instance

    fun showDialog(
        context: Context?,
        message: String?,
        cancelable: Boolean,
        textNegative: String?,
        textPositive: String?,
        showPositiveButton: Boolean,
        showNegativeButton: Boolean
    ) {
        // Initialize the binding
        mDialog = Dialog(context!!)
        mDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Inflate the layout with ViewBinding
        binding = CustomDialogBinding.inflate(mDialog!!.layoutInflater)
        mDialog!!.setContentView(binding!!.root)

        // Binding views using ViewBinding
        binding!!.tvMessage.text = message
        binding!!.tvNo.text = textNegative
        binding!!.tvOk.text = textPositive

        // Handle button visibility
        if (!showNegativeButton) binding!!.rlCancel.gone()
        if (!showPositiveButton) binding!!.rlOk.gone()

        // Set dialog properties
        mDialog!!.setCancelable(cancelable)
        mDialog!!.setCanceledOnTouchOutside(cancelable)

        // Set button listeners
        binding!!.rlOk.setOnClickListener {
            clickPositiveButton?.invoke()
            mDialog?.dismiss()
        }

        binding!!.rlCancel.setOnClickListener {
            clickNegativeButton?.invoke()
            mDialog?.dismiss()
        }

        mDialog!!.show()
    }

    fun hideProgress() {
        mDialog?.dismiss()
        mDialog = null
    }

}
