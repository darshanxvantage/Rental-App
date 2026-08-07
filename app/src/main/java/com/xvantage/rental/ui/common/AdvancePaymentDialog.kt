package com.xvantage.rental.ui.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.utils.AmountFormatter

object AdvancePaymentDialog {

    fun show(
        context: Context,
        tenantName: String,
        amount: Double,
        onConfirm: () -> Unit
    ) {
        val amountText = AmountFormatter.format(amount)

        val dialog = android.app.Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_advance_payment_confirm, null)

        dialog.setContentView(view)
        dialog.setCancelable(true)

        dialog.window?.apply {
            setDimAmount(0.6f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        view.findViewById<TextView>(R.id.tvAdvanceDialogMessage).text =
            "$tenantName has no dues this cycle. $amountText will be added " +
                    "as advance and auto-adjusted next month."

//        view.findViewById<TextView>(R.id.tvAdvanceDialogAmount).text =
//            "Collecting $amountText"

        view.findViewById<MaterialButton>(R.id.btnAdvanceDialogCancel)
            .setOnClickListener { dialog.dismiss() }

        view.findViewById<MaterialButton>(R.id.btnAdvanceDialogConfirm)
            .setOnClickListener {
                dialog.dismiss()
                onConfirm()
            }

        dialog.show()

        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}