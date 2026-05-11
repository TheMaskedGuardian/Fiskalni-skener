package com.themaskedguardian.fiskalniskener

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class ConfirmationActivity : AppCompatActivity() {

    private var receipt: ReceiptData? = null
    private val activityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + activityJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val url = intent.getStringExtra("RECEIPT_URL") ?: ""
        if (url.isEmpty()) {
            finish()
            return
        }

        fetchData(url)

        findViewById<Button>(R.id.btnAddToCashew).setOnClickListener {
            receipt?.let {
                CashewLauncher.launch(this, it)
                finish()
            }
        }

        findViewById<Button>(R.id.btnCopyClipboard).setOnClickListener {
            receipt?.let {
                // KOPIRAMO PUNU VERZIJU (toFullNotes)
                copyToClipboard(it.toFullNotes())
            }
        }
    }

    private fun fetchData(url: String) {
        uiScope.launch {
            try {
                val data = ReceiptFetcher.fetch(url)
                receipt = data
                updateUI(data)
            } catch (e: Exception) {
                Toast.makeText(this@ConfirmationActivity, "Greška pri učitavanju: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun updateUI(data: ReceiptData) {
        findViewById<TextView>(R.id.shopName).text = data.company
        findViewById<TextView>(R.id.receiptDate).text = data.formatDate()
        findViewById<TextView>(R.id.totalAmount).text = "${data.formatAmount()} RSD"

        val container = findViewById<LinearLayout>(R.id.itemsContainer)
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (item in data.items) {
            val view = inflater.inflate(R.layout.item_receipt_row, container, false)
            view.findViewById<TextView>(R.id.item_name).text = item.name
            
            val qtyStr = if (item.quantity == Math.floor(item.quantity)) 
                item.quantity.toInt().toString() 
            else 
                String.format(Locale.US, "%.2f", item.quantity)
            
            val priceStr = String.format(Locale.US, "%.2f", item.unitPrice)
            val totalStr = String.format(Locale.US, "%.2f", item.total)
            
            view.findViewById<TextView>(R.id.item_details).text = "$qtyStr x $priceStr = $totalStr"
            
            container.addView(view)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Fiskalni Racun", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Tekst kopiran u clipboard!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityJob.cancel()
    }
}
