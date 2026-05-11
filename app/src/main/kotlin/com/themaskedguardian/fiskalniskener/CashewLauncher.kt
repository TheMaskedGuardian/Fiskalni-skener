package com.themaskedguardian.fiskalniskener

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CashewLauncher {
    fun launch(context: Context, data: ReceiptData) {
        try {
            val amount = -data.totalAmount
            val notes = data.toCashewNotes()
            val title = data.sanitize(data.company)
            val date = data.formatDateIso()

            val uri = Uri.parse("https://cashewapp.web.app/addTransactionRoute")
                .buildUpon()
                .appendQueryParameter("amount", amount.toString())
                .appendQueryParameter("date", date)
                .appendQueryParameter("title", title)
                .appendQueryParameter("notes", notes)
                .build()

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("app.cashew.cashew")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val generalIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(generalIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Greška pri otvaranju Cashew aplikacije", Toast.LENGTH_SHORT).show()
        }
    }
}
