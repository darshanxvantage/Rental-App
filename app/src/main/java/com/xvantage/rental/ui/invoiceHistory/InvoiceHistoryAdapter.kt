package com.xvantage.rental.ui.invoiceHistory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.xvantage.rental.databinding.PaymentHistoryCardBinding
import com.xvantage.rental.network.response.InvoiceHistoryEntry
import com.xvantage.rental.utils.AmountFormatter
import com.xvantage.rental.utils.constants.Constant
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceHistoryAdapter(
    private val invoiceList: List<InvoiceHistoryEntry>,
    val context: Context
) : RecyclerView.Adapter<InvoiceHistoryAdapter.InvoiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = PaymentHistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoiceList[position]

        holder.binding.roomId.text =
            listOf(invoice.roomNo, invoice.tenantName)
                .filter { !it.isNullOrBlank() }
                .joinToString(" • ")
                .ifEmpty { invoice.invoiceNumber }

        holder.binding.tvAddress.text =
            invoice.propertyName ?: invoice.propertyAddress ?: "—"

        holder.binding.tvPayment.text = AmountFormatter.format(invoice.amount)
        holder.binding.tvPaymentDate.text = formatDate(invoice.createdAt)


        holder.binding.labelNote.text = "Invoice:"
        holder.binding.tvNote.text =
            listOfNotNull(invoice.monthLabel, invoice.invoiceNumber)
                .joinToString(" • ")

        holder.binding.paymentHistoryCard.setOnClickListener {
            openInvoicePdf(invoice.filePath)
        }
    }

    private fun openInvoicePdf(relativePath: String) {
        try {
            val fullUrl = Constant.SERVER_ROOT_URL + relativePath
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(fullUrl), "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {

            try {
                val fullUrl = Constant.SERVER_ROOT_URL + relativePath
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                )
            } catch (e2: Exception) {
//                Toast.makeText(
//                    context,
//                    "Couldn't open the invoice. Please try again.",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val output = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
            output.format(input.parse(isoDate) ?: return isoDate)
        } catch (e: Exception) {
            isoDate
        }
    }

    override fun getItemCount(): Int = invoiceList.size

    class InvoiceViewHolder(val binding: PaymentHistoryCardBinding) : RecyclerView.ViewHolder(binding.root)
}