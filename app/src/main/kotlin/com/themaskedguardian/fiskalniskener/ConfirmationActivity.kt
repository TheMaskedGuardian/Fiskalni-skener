package com.themaskedguardian.fiskalniskener

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
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
                copyToClipboard(it.toFullNotes())
            }
        }

        findViewById<Button>(R.id.btnRetry).setOnClickListener {
            fetchData(url)
        }

        findViewById<Button>(R.id.btnCloseError).setOnClickListener {
            finish()
        }
    }

    private fun fetchData(url: String) {
        if (!NetworkUtil.isOnline(this)) {
            showError("Nema internet veze. Proverite podešavanja mreže.")
            return
        }

        showLoading()
        uiScope.launch {
            try {
                val data = ReceiptFetcher.fetch(url)
                receipt = data
                updateUI(data)
                showContent()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Server Poreske uprave ne odgovara (Timeout)"
                    e is java.net.UnknownHostException -> 
                        "Nije moguće uspostaviti vezu sa serverom. Proverite internet."
                    else -> "Greška pri učitavanju: ${e.localizedMessage}"
                }
                showError(errorMessage)
            }
        }
    }

    private fun showLoading() {
        findViewById<View>(R.id.loadingLayout).visibility = View.VISIBLE
        findViewById<View>(R.id.contentLayout).visibility = View.GONE
        findViewById<View>(R.id.errorLayout).visibility = View.GONE
    }

    private fun showContent() {
        findViewById<View>(R.id.loadingLayout).visibility = View.GONE
        findViewById<View>(R.id.contentLayout).visibility = View.VISIBLE
        findViewById<View>(R.id.errorLayout).visibility = View.GONE
    }

    private fun showError(message: String) {
        findViewById<View>(R.id.loadingLayout).visibility = View.GONE
        findViewById<View>(R.id.contentLayout).visibility = View.GONE
        findViewById<View>(R.id.errorLayout).visibility = View.VISIBLE
        findViewById<TextView>(R.id.errorText).text = message
    }

    private fun updateUI(data: ReceiptData) {
        findViewById<TextView>(R.id.shopName).text = data.company
        findViewById<TextView>(R.id.receiptDate).text = data.formatDate()
        findViewById<TextView>(R.id.totalAmount).text = String.format("%.2f RSD", data.totalAmount)

        val container = findViewById<LinearLayout>(R.id.itemsContainer)
        container.removeAllViews()

        for (item in data.items) {
            val view = layoutInflater.inflate(R.layout.item_receipt_row, container, false)
            view.findViewById<TextView>(R.id.item_name).text = item.name
            view.findViewById<TextView>(R.id.item_details).text = 
                String.format("%.2f x %.2f = %.2f", item.quantity, item.unitPrice, item.total)
            container.addView(view)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Fiskalni Račun", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Kopirano u clipboard! ✅", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityJob.cancel()
    }
}
