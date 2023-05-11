package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class AddDrugActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_drug)

        // Uzyskanie referencji do slidów
        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)

        // Konfiguracja slidów

        // Slid dla dni
        numberPickerDay.minValue = 1
        numberPickerDay.maxValue = 31

        // Slid dla miesięcy
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 12

        // Slid dla lat
        numberPickerYear.minValue = 1900
        numberPickerYear.maxValue = 2100


        val spinnerDawkowanie = findViewById<Spinner>(R.id.spinnerDawkowanie)

        val dawkowanieOptions = arrayOf("Opcja 1", "Opcja 2", "Opcja 3", "Własne")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dawkowanieOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDawkowanie.adapter = adapter

        spinnerDawkowanie.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "Własne") {
                    showCustomValueDialog()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie wykonujemy żadnej akcji
            }
        }

        //        Lista leków
        val drugsList: ArrayList<String> = ArrayList()
        var xml_data = assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
        var factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        var parser: XmlPullParser = factory.newPullParser()

        parser.setInput(xml_data, null)

        var event: Int = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            var tag_name: String = parser.name
            when (event) {
                XmlPullParser.END_TAG -> {
                    if (tag_name == "produktLeczniczy") {
                        var name: String = "\n" + parser.getAttributeValue(0)
                        drugsList.add(name)
                    }
                }
            }
            event = parser.next()
        }

//       rozwijalne menu
        val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)

        val adapter2 = ArrayAdapter(this, R.layout.list_drugs, drugsList)

        autoComplete.setAdapter(adapter2)

        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val itemSelected = adapterView.getItemAtPosition(i)
                Toast.makeText(this, "$itemSelected",Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCustomValueDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Wprowadź własną wartość")
            .setView(editText)
            .setPositiveButton("Dodaj") { dialog, _ ->
                val customValue = editText.text.toString()
                if (customValue.isNotEmpty()) {
                    val value = customValue.toIntOrNull()
                    if (value != null && value in 1..25) {
                        val spinnerDawkowanie = findViewById<Spinner>(R.id.spinnerDawkowanie)
                        val adapter = spinnerDawkowanie.adapter as ArrayAdapter<String>
                        adapter.add(customValue)
                        spinnerDawkowanie.setSelection(adapter.count - 1)
                    } else {
                        Toast.makeText(applicationContext, "Wprowadź liczbę z zakresu 1-25", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }


}
