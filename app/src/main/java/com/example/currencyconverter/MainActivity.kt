package com.example.currencyconverter

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var editTextFrom: EditText
    private lateinit var editTextTo: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var textViewRate: TextView

    private val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

    private val exchangeRates = mapOf(
        "USD" to mapOf("EUR" to 0.9261, "JPY" to 110.0, "VND" to 24000.0, "GBP" to 0.75),
        "EUR" to mapOf("USD" to 1.08, "JPY" to 119.0, "VND" to 26000.0, "GBP" to 0.81),
        "JPY" to mapOf("USD" to 0.0091, "EUR" to 0.0084, "VND" to 218.0, "GBP" to 0.0068),
        "VND" to mapOf("USD" to 0.000042, "EUR" to 0.000038, "JPY" to 0.0046, "GBP" to 0.000031),
        "GBP" to mapOf("USD" to 1.33, "EUR" to 1.23, "JPY" to 147.0, "VND" to 32000.0)
    )

    private var isUpdatingFrom = false
    private var isUpdatingTo = false
    private var isFromFocused = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextFrom = findViewById(R.id.editTextFrom)
        editTextTo = findViewById(R.id.editTextTo)
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        textViewRate = findViewById(R.id.textViewRate)

        val currencies = arrayOf("USD", "EUR", "JPY", "VND", "GBP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        // Thêm OnClickListener để tự động chọn tất cả text
        editTextFrom.setOnClickListener {
            editTextFrom.selectAll()
        }

        editTextTo.setOnClickListener {
            editTextTo.selectAll()
        }

        // Thiết lập OnFocusChangeListener để chọn tất cả và định dạng in đậm cho EditText được chọn
        editTextFrom.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isFromFocused = true
                editTextFrom.selectAll() // Tự động chọn text khi được focus
                editTextFrom.setTypeface(null, Typeface.BOLD) // In đậm EditText được chọn
                editTextTo.setTypeface(null, Typeface.NORMAL) // Bình thường cho EditText còn lại
                editTextTo.removeTextChangedListener(textWatcherTo)
                editTextFrom.addTextChangedListener(textWatcherFrom)
            }
        }

        editTextTo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isFromFocused = false
                editTextTo.selectAll() // Tự động chọn text khi được focus
                editTextTo.setTypeface(null, Typeface.BOLD) // In đậm EditText được chọn
                editTextFrom.setTypeface(null, Typeface.NORMAL) // Bình thường cho EditText còn lại
                editTextFrom.removeTextChangedListener(textWatcherFrom)
                editTextTo.addTextChangedListener(textWatcherTo)
            }
        }

        editTextFrom.addTextChangedListener(textWatcherFrom)

        spinnerFrom.onItemSelectedListener = spinnerListener
        spinnerTo.onItemSelectedListener = spinnerListener
    }

    private fun updateConversionFrom() {
        if (isUpdatingTo) return

        isUpdatingFrom = true
        val amountFrom = editTextFrom.text.toString().toDoubleOrNull() ?: 0.0
        val currencyFrom = spinnerFrom.selectedItem.toString()
        val currencyTo = spinnerTo.selectedItem.toString()

        val rate = exchangeRates[currencyFrom]?.get(currencyTo) ?: 1.0
        val result = amountFrom * rate
        editTextTo.setText(decimalFormat.format(result))

        textViewRate.text = "1 $currencyFrom = ${decimalFormat.format(rate)} $currencyTo"

        isUpdatingFrom = false
    }

    private fun updateConversionTo() {
        if (isUpdatingFrom) return

        isUpdatingTo = true
        val amountTo = editTextTo.text.toString().toDoubleOrNull() ?: 0.0
        val currencyFrom = spinnerFrom.selectedItem.toString()
        val currencyTo = spinnerTo.selectedItem.toString()

        val rate = exchangeRates[currencyTo]?.get(currencyFrom) ?: 1.0
        val result = amountTo * rate
        editTextFrom.setText(decimalFormat.format(result))

        textViewRate.text = "1 $currencyTo = ${decimalFormat.format(rate)} $currencyFrom"

        isUpdatingTo = false
    }

    private val textWatcherFrom = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!isUpdatingFrom) {
                updateConversionFrom()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val textWatcherTo = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!isUpdatingTo) {
                updateConversionTo()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val spinnerListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: android.widget.AdapterView<*>?,
            view: android.view.View?,
            position: Int,
            id: Long
        ) {
            if (isFromFocused) {
                updateConversionFrom()
            } else {
                updateConversionTo()
            }
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
}
