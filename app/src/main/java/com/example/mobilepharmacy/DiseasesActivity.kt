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
import android.content.res.XmlResourceParser
import android.graphics.Color
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

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
        spinnerSymptoms.setBackgroundColor(Color.WHITE) // Ustaw tło na biały

        // Tworzenie listy dolegliwości
        val symptomsList = listOf("Katar", "Ból głowy", "Ból brzucha", "Kaszel")

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
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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

        if (medicationsList.isNotEmpty()) {
            // Tworzenie adaptera dla listy leków
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, medicationsList)
            listViewMedications.adapter = adapter
            textViewMedications.text = "Lista leków dla ${spinnerSymptoms.selectedItem}:"
        } else {
            // Jeżeli nie znaleziono leków, wyświetlamy odpowiedni komunikat
            textViewMedications.text = "Brak leków dla ${spinnerSymptoms.selectedItem}."
            listViewMedications.adapter = null
        }
    }


    private fun hideMedications() {
        // Ukrywanie listy leków
        textViewMedications.visibility = View.GONE
        listViewMedications.visibility = View.GONE
    }

    private fun getMedicationsForSymptom(symptom: String): List<String> {
        return when (symptom) {
            "Katar" -> {
                val drugsList: ArrayList<String> = ArrayList()

                try {
                    val pullParserFactory = XmlPullParserFactory.newInstance()
                    val parser = pullParserFactory.newPullParser()
                    val inputStream =
                        applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    parser.setInput(inputStream, null)

                    val targetSubstances = listOf("Xylometazolini hydrochloridum")
                    val drugs = parseXml(parser, targetSubstances)

                    for (drug in drugs!!) {
                        drugsList.add(drug.nazwaProduktu)
                    }
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                drugsList
            }

            "Ból głowy" -> {
                val drugsList: ArrayList<String> = ArrayList()

                try {
                    val pullParserFactory = XmlPullParserFactory.newInstance()
                    val parser = pullParserFactory.newPullParser()
                    val inputStream =
                        applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    parser.setInput(inputStream, null)

                    val targetSubstances = listOf("Paracetamolum")
                    val drugs = parseXml(parser, targetSubstances)

                    for (drug in drugs!!) {
                        drugsList.add(drug.nazwaProduktu)
                    }
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                drugsList
            }

            "Ból brzucha" -> {
                val drugsList: ArrayList<String> = ArrayList()

                try {
                    val pullParserFactory = XmlPullParserFactory.newInstance()
                    val parser = pullParserFactory.newPullParser()
                    val inputStream =
                        applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    parser.setInput(inputStream, null)

                    val targetSubstances = listOf("Drotaverini hydrochloridum")
                    val drugs = parseXml(parser, targetSubstances)

                    for (drug in drugs!!) {
                        drugsList.add(drug.nazwaProduktu)
                    }
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                drugsList
            }

            "Kaszel" -> {
                val drugsList: ArrayList<String> = ArrayList()

                try {
                    val pullParserFactory = XmlPullParserFactory.newInstance()
                    val parser = pullParserFactory.newPullParser()
                    val inputStream =
                        applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    parser.setInput(inputStream, null)

                    val targetSubstances = listOf("Codeini phosphas")
                    val drugs = parseXml(parser, targetSubstances)

                    for (drug in drugs!!) {
                        drugsList.add(drug.nazwaProduktu)
                    }
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                drugsList
            }

            else -> emptyList()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser, targetSubstances: List<String>): ArrayList<Drugs>? {
        var drugs: ArrayList<Drugs>? = null
        var eventType = parser.eventType
        var drug: Drugs? = null
        var isOTC = false
        var isSub = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name: String
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> drugs = ArrayList()
                XmlPullParser.START_TAG -> {
                    name = parser.name
                    if (name == "produktLeczniczy") {
                        drug = Drugs()
                        drug.nazwaProduktu = parser.getAttributeValue(null, "nazwaProduktu")
                    } else if (name == "opakowanie") {
                        val kategoriaDostepnosci =
                            parser.getAttributeValue(null, "kategoriaDostepnosci")
                        isOTC = kategoriaDostepnosci == "OTC"
                    } else if (name == "substancjaCzynna") {
                        val substancja = parser.getAttributeValue(null, "nazwaSubstancji")
                        isSub = targetSubstances.contains(substancja)
                    }
                }

                XmlPullParser.END_TAG -> {
                    name = parser.name
                    if (name.equals(
                            "produktLeczniczy",
                            ignoreCase = true
                        ) && drug != null && isOTC && isSub
                    ) {
                        drugs!!.add(drug)
                    }
                }
            }
            eventType = parser.next()
        }
        return drugs
    }
}