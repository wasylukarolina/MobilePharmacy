package com.example.mobilepharmacy

import android.content.ContentValues.TAG
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class AddDrugActivity : AppCompatActivity() {
    private var quantity: Int = 0
    private lateinit var timePickerLayout: TextInputLayout

    private val drugsList: ArrayList<String> = ArrayList()
    private val filteredDrugsList: ArrayList<String> = ArrayList()
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_drug)
        var databaseRef = FirebaseDatabase.getInstance().reference

        fun saveDataToFirebase() {
            val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
            val nazwaProduktu = autoComplete.text.toString()

            val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
            val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
            val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
            val day = numberPickerDay.value
            val month = numberPickerMonth.value
            val year = numberPickerYear.value
            val dataWaznosci = String.format("%02d-%02d-%d", day, month, year)

            val timePickerLayout = findViewById<TextInputLayout>(R.id.timePickerLayout)
            val dawkowanie = ArrayList<String>()

            val customCheckbox = findViewById<CheckBox>(R.id.customCheckBox)
            val customEditText = findViewById<EditText>(R.id.customEditText)

            if (customCheckbox.isChecked) {
                val timePicker = timePickerLayout.getChildAt(0) as TimePicker
                val hour = timePicker.hour
                val minute = timePicker.minute
                val timeString = String.format("%02d:%02d", hour, minute)
                dawkowanie.add(timeString)

            } else {
                val customQuantity = customEditText.text.toString().toIntOrNull()
                if (customQuantity != null) {
                    for (i in 0 until timePickerLayout.childCount) {
                        val timePicker = timePickerLayout.getChildAt(i) as TimePicker
                        val hour = timePicker.hour
                        val minute = timePicker.minute
                        val timeString = String.format("%02d:%02d", hour, minute)
                        dawkowanie.add(timeString)
                    }

                    val firstHour = dawkowanie[0].substring(0, 2).toInt()
                    val firstMinute = dawkowanie[0].substring(3, 5).toInt()

                    for (i in 1 until customQuantity) {
                        val newHour = (firstHour + customQuantity * i) % 24
                        val newMinute = firstMinute
                        val timeString = String.format("%02d:%02d", newHour, newMinute)
                        dawkowanie.add(timeString)
                    }



                } else {
                    val timePicker = timePickerLayout.getChildAt(0) as TimePicker
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    val timeString = String.format("%02d:%02d", hour, minute)
                    dawkowanie.add(timeString)
                }
            }


            val firestoreDB = FirebaseFirestore.getInstance()


            // Pobieranie ID aktualnie zalogowanego użytkownika z SharedPreferences
            val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
            val userId = sharedPreferences.getString("userID", "") ?: ""

            // Tworzenie mapy z danymi
            val dataMap = hashMapOf<String, Any>()
            dataMap["userId"] = userId
            dataMap["nazwaProduktu"] = nazwaProduktu
            dataMap["dataWaznosci"] = dataWaznosci
            dataMap["dawkowanie"] = dawkowanie


            // Dodawanie danych do Firestore
            firestoreDB.collection("leki")
                .add(dataMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dane zostały dodane do Firestore.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Błąd podczas dodawania danych do Firestore", e)
                    Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
                }
        }


        val buttonZapisz = findViewById<Button>(R.id.buttonDodaj)
        buttonZapisz.setOnClickListener {
            saveDataToFirebase()
        }

        autoComplete = findViewById(R.id.auto_complete_txt)
        adapter = ArrayAdapter(this, R.layout.list_drugs, filteredDrugsList)
        autoComplete.setAdapter(adapter)

        autoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Niepotrzebne
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterDrugs(s.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable) {
                // Niepotrzebne
            }
        })

        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(this, "$itemSelected", Toast.LENGTH_SHORT).show()
        }


        // Uzyskanie referencji do slidów
        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)

        // Konfiguracja slidów

        // Slide dla dni
        numberPickerDay.minValue = 1
        numberPickerDay.maxValue = 31

        // Slide dla miesięcy
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 12

        // Slide dla lat
        numberPickerYear.minValue = 2023
        numberPickerYear.maxValue = 2035

        numberPickerMonth.setOnValueChangedListener { _, _, newVal ->
            val selectedMonth = newVal
            val daysInMonth = when (selectedMonth) {
                2 -> if (isLeapYear(numberPickerYear.value)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            numberPickerDay.maxValue = daysInMonth
        }

        //        Lista leków
        val drugsList: ArrayList<String> = ArrayList()

        var pullParserFactory: XmlPullParserFactory
        try {
            pullParserFactory = XmlPullParserFactory.newInstance()
            val parser = pullParserFactory.newPullParser()
            val inputStream = applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)

            val drugs = parseXml(parser)

            for (drug in drugs!!) {
                drugsList.add(drug.nazwaProduktu)
            }

            val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
            val adapter2 = ArrayAdapter(this, R.layout.list_drugs, drugsList)
            autoComplete.setAdapter(adapter2)

            autoComplete.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // Niepotrzebne
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    adapter2.filter.filter(s)
                }

                override fun afterTextChanged(s: Editable) {
                    // Niepotrzebne
                }
            })

            autoComplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val itemSelected = adapterView.getItemAtPosition(i)
                Toast.makeText(this, "$itemSelected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        //obsługa wyboru dawkowania

        val quantityOptions = resources.getStringArray(R.array.quantity_options)
        val quantityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityOptions)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val quantitySpinner = findViewById<Spinner>(R.id.quantitySpinner)

        quantitySpinner.adapter = quantityAdapter

        timePickerLayout = findViewById(R.id.timePickerLayout)

        val customCheckbox = findViewById<CheckBox>(R.id.customCheckBox)
        val customEditText = findViewById<EditText>(R.id.customEditText)
        customEditText.visibility = View.VISIBLE

        quantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                quantity = position + 1
                updateTimeWindows()

                if (customCheckbox.isChecked) {
                    customEditText.visibility = View.GONE
                    customEditText.setText("")

                } else {

                    if (timePickerLayout.childCount > 1) {
                        timePickerLayout.removeAllViews()
                        val customQuantity = customEditText.text.toString().toIntOrNull()
                        for (i in 1..(customQuantity ?: quantity)) {
                            val newTimePicker = TimePicker(this@AddDrugActivity)
                            newTimePicker.setIs24HourView(true)
                            timePickerLayout.addView(newTimePicker)
                        }
                        if (customQuantity != null && customQuantity > 1) {
                            customEditText.visibility = View.GONE
                            customEditText.setText("")
                        } else {
                            customEditText.visibility = View.VISIBLE
                        }
                        timePickerLayout.removeViews(1, timePickerLayout.childCount - 1)
                    } else {
                        customEditText.visibility = View.GONE
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie wykonujemy żadnej akcji
            }
        }

        customCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customEditText.visibility = View.GONE
            } else {
                customEditText.visibility = View.VISIBLE
                customEditText.setText("")
            }
        }



    }

    private fun filterDrugs(query: String) {
        filteredDrugsList.clear()
        for (drug in drugsList) {
            if (drug.contains(query, ignoreCase = true)) {
                filteredDrugsList.add(drug)
            }
        }
    }

    // Funkcja sprawdzająca, czy rok jest przestępny
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    private fun updateTimeWindows() {
        timePickerLayout.removeAllViews()

        for (i in 1..quantity) {
            val timePicker = TimePicker(this)
            timePicker.setIs24HourView(true)

            timePickerLayout.addView(timePicker)
        }

        timePickerLayout.visibility = View.VISIBLE
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser): ArrayList<Drugs>? {
        var drugs: ArrayList<Drugs>? = null
        var eventType = parser.eventType
        var drug: Drugs? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name: String
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> drugs = ArrayList()
                XmlPullParser.START_TAG -> {
                    name = parser.name
                    if (name == "produktLeczniczy") {
                        drug = Drugs()
                        drug.nazwaProduktu = parser.getAttributeValue(null, "nazwaProduktu")
                    }
                }
                XmlPullParser.END_TAG -> {
                    name = parser.name
                    if (name.equals("produktLeczniczy", ignoreCase = true) && drug != null) {
                        drugs!!.add(drug)
                    }
                }
            }
            eventType = parser.next()
        }
        return drugs
    }



}