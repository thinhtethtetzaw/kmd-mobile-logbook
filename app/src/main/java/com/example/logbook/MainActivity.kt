package com.example.logbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    private lateinit var inputLayout: TextInputLayout
    private lateinit var inputEditText: TextInputEditText
    private lateinit var fromValueSpinner: Spinner
    private lateinit var toValueSpinner: Spinner
    private lateinit var convertButton: Button
    private lateinit var resultCard: CardView
    private lateinit var resultTextView: TextView
    private lateinit var formulaTextView: TextView

    private val units = arrayOf("Metre", "Millimetre", "Mile", "Foot")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputLayout = findViewById(R.id.inputLayout)
        inputEditText = findViewById(R.id.inputEditText)
        fromValueSpinner = findViewById(R.id.fromValueSpinner)
        toValueSpinner = findViewById(R.id.toValueSpinner)
        convertButton = findViewById(R.id.convertButton)
        resultCard = findViewById(R.id.conversionResultCard)
        resultTextView = findViewById(R.id.conversionResultTextView)
        formulaTextView = findViewById(R.id.formulaTextView)

        setupSpinners()

        convertButton.setOnClickListener {
            convertUnits()
        }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fromValueSpinner.adapter = adapter
        toValueSpinner.adapter = adapter

        fromValueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateToSpinner(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set initial selection
        fromValueSpinner.setSelection(0)
        toValueSpinner.setSelection(1)
    }

    private fun updateToSpinner(fromPosition: Int) {
        val toAdapter = ArrayAdapter(this, R.layout.spinner_item, units.filterIndexed { index, _ -> index != fromPosition })
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toValueSpinner.adapter = toAdapter
    }

    private fun convertUnits() {
        val input = inputEditText.text.toString()
        if (input.isEmpty()) {
            inputLayout.error = "Please enter a value"
            return
        }

        val value = input.toDoubleOrNull()
        if (value == null) {
            inputLayout.error = "Invalid input"
            return
        }

        inputLayout.error = null

        val fromUnit = fromValueSpinner.selectedItem.toString()
        val toUnit = toValueSpinner.selectedItem.toString()
        val result = convert(value, fromUnit, toUnit)

        if (result != null) {
            resultTextView.text = String.format(Locale.US, "%.2f %s = %.2f %s", value, fromUnit, result, toUnit)
            resultCard.visibility = View.VISIBLE
            updateFormulaText(fromUnit, toUnit)
        } else {
            resultCard.visibility = View.GONE
            formulaTextView.visibility = View.GONE
        }
    }

    private fun updateFormulaText(fromUnit: String, toUnit: String) {
        val formula = when {
            fromUnit == "Metre" && toUnit == "Millimetre" -> "multiply by 1000"
            fromUnit == "Millimetre" && toUnit == "Metre" -> "divide by 1000"
            fromUnit == "Metre" && toUnit == "Mile" -> "divide by 1609.34"
            fromUnit == "Mile" && toUnit == "Metre" -> "multiply by 1609.34"
            fromUnit == "Metre" && toUnit == "Foot" -> "divide by 0.3048"
            fromUnit == "Foot" && toUnit == "Metre" -> "multiply by 0.3048"
            fromUnit == "Millimetre" && toUnit == "Mile" -> "divide by 1,609,340"
            fromUnit == "Mile" && toUnit == "Millimetre" -> "multiply by 1,609,340"
            fromUnit == "Millimetre" && toUnit == "Foot" -> "divide by 304.8"
            fromUnit == "Foot" && toUnit == "Millimetre" -> "multiply by 304.8"
            fromUnit == "Mile" && toUnit == "Foot" -> "multiply by 5280"
            fromUnit == "Foot" && toUnit == "Mile" -> "divide by 5280"
            else -> ""
        }

        if (formula.isNotEmpty()) {
            formulaTextView.text = "* Formula: $formula"
            formulaTextView.visibility = View.VISIBLE
        } else {
            formulaTextView.visibility = View.GONE
        }
    }

    private fun convert(value: Double, fromUnit: String, toUnit: String): Double {
        val inMetres = when (fromUnit) {
            "Metre" -> value
            "Millimetre" -> value / 1000
            "Mile" -> value * 1609.34
            "Foot" -> value * 0.3048
            else -> throw IllegalArgumentException("Unknown unit: $fromUnit")
        }

        return when (toUnit) {
            "Metre" -> inMetres
            "Millimetre" -> inMetres * 1000
            "Mile" -> inMetres / 1609.34
            "Foot" -> inMetres / 0.3048
            else -> throw IllegalArgumentException("Unknown unit: $toUnit")
        }
    }
}