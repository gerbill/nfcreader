package com.example.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcDataTextView: TextView
    private lateinit var readNfcButton: Button
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcDataTextView = findViewById(R.id.nfcDataTextView)
        readNfcButton = findViewById(R.id.readNfcButton)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            nfcDataTextView.text = "NFC is not available on this device."
            readNfcButton.isEnabled = false
            return
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Failed to add MIME type", e)
            }
        }
        intentFiltersArray = arrayOf(ndef)

        readNfcButton.setOnClickListener {
            enableNfcReading()
        }
    }

    private fun enableNfcReading() {
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null)
        nfcDataTextView.text = "Tap an NFC tag to read it."
        Log.d("NFCReader", "Foreground NFC reading enabled")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            enableNfcReading()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFCReader", "New NFC intent received")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.also { tag ->
                val nfcContent = "Tag info: ${tag.id.toHexString()}"
                nfcDataTextView.text = nfcContent
                Log.d("NFCReader", "NFC Tag Read: $nfcContent")
            }
        }
    }

    private fun ByteArray.toHexString() = joinToString(separator = " ") { byte -> "%02x".format(byte) }
}