package com.example.mobilepharmacy

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DiseasesActivity : AppCompatActivity() {

    private lateinit var spinnerSymptoms: Spinner
    private lateinit var buttonShowMedications: Button
    private lateinit var textViewMedications: TextView
    private lateinit var listViewMedications: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diseases)

        // Inicjalizacja widoków
        spinnerSymptoms = findViewById(R.id.spinnerSymptoms)
        buttonShowMedications = findViewById(R.id.buttonShowMedications)
        textViewMedications = findViewById(R.id.textViewMedications)
        listViewMedications = findViewById(R.id.listViewMedications)

        // Tworzenie listy dolegliwości
        val symptomsList = listOf("Katar", "Ból głowy", "Ból brzucha", "Kaszel", "Przeziębienie")

        // Utworzenie adaptera dla spinnera
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, symptomsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSymptoms.adapter = adapter

        // Obsługa kliknięcia przycisku "Pokaż leki"
        buttonShowMedications.setOnClickListener {
            showMedications()
        }

        // Obsługa wyboru elementu z spinnera
        spinnerSymptoms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Wywołanie metody przy zmianie wybranego elementu w spinnerze
                hideMedications()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Wywołanie metody, gdy nie jest wybrany żaden element w spinnerze
                hideMedications()
            }
        }
    }

    private fun showMedications() {
        // Wyświetlanie listy leków
        textViewMedications.visibility = View.VISIBLE
        listViewMedications.visibility = View.VISIBLE

        // Pobieranie leków z bazy danych lub innych źródeł
        val medicationsList = getMedicationsForSymptom(spinnerSymptoms.selectedItem.toString())

        // Utworzenie adaptera dla listy leków
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, medicationsList)
        listViewMedications.adapter = adapter
    }

    private fun hideMedications() {
        // Ukrywanie listy leków
        textViewMedications.visibility = View.GONE
        listViewMedications.visibility = View.GONE
    }

    private fun getMedicationsForSymptom(symptom: String): List<String> {
        // Implementacja logiki pobierania leków dla wybranej dolegliwości
        // Można tutaj zaimplementować zapytanie do bazy danych lub inny sposób pobierania leków

        // Przykładowa lista leków dla dolegliwości
        return when (symptom) {
            "Katar" -> listOf("Lek1", "Lek2", "Lek3")
            "Ból głowy" -> listOf("Lek4", "Lek5", "Lek6")
            "Ból brzucha" -> listOf("Lek7", "Lek8", "Lek9")
            "Kaszel" -> listOf("Lek10", "Lek11", "Lek12")
            "Przeziębienie" -> listOf("Lek13", "Lek14", "Lek15")
            else -> emptyList()
        }
    }
}
