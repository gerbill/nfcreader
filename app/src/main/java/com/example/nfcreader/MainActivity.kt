package com.example.nfcreader
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tagDataTextView: TextView
    private lateinit var readNfcButton: Button
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tagDataTextView = findViewById(R.id.tagDataTextView)
        readNfcButton = findViewById(R.id.readNfcButton)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Create a PendingIntent to handle the NFC intent
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // Create an IntentFilter array to filter for NFC events
        val ndefIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefIntentFilter.addDataType("*/*")
            intentFiltersArray = arrayOf(ndefIntentFilter)
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Failed to create NFC intent filter", e)
        }

        // Create a tech list array for the NFC techs you want to handle
        techListsArray = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name)
        )

        // Set an OnClickListener for the read NFC button
        readNfcButton.setOnClickListener {
            startNfcReading()
        }
        tagDataTextView.text = "Yo!"
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFC onNewIntent", "onNewIntent() called with intent: ${intent.action}")
        handleNfcIntent(intent)
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
//            handleNfcIntent(intent)
//        }
    }

    private fun startNfcReading() {
        // Start the NFC reading by enabling foreground dispatch
        Log.d("NFC startNfcReading", "startNfcReading() called")
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, intentFiltersArray, techListsArray)
    }

    private fun handleNfcIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        Log.d("NFC handleNfcIntent", "handleNfcIntent() called with tag: $tag")
        val nfcv = NfcV.get(tag)
        nfcv.connect()
        val messageLen = nfcv.maxTransceiveLength
        val message = nfcv
        nfcv.close()
        Log.d("NFC handleNfcIntent", "handleNfcIntent() called with ndef: $nfcv $messageLen")
    }
}