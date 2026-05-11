package com.themaskedguardian.fiskalniskener

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object CashewLauncher {
    fun launch(context: Context, receipt: ReceiptData) {
        val amountStr = String.format(Locale.US, "%.2f", receipt.totalAmount)

        // Naslov je sada ime prodavnice, ali procišceno od cilirice i kvačica
        val cleanTitle = receipt.sanitize(receipt.company)

        val uri = Uri.Builder()
            .scheme("https")
            .authority("cashewapp.web.app")
            .path("/addTransaction")
            .appendQueryParameter("amount", "-$amountStr")
            .appendQueryParameter("date", receipt.formatDateIso())
            .appendQueryParameter("title", cleanTitle)
            .appendQueryParameter("notes", receipt.toCashewNotes())
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("app.cashew.cashew")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Greška pri otvaranju Cashew aplikacije", Toast.LENGTH_LONG).show()
            }
        }
    }
}
