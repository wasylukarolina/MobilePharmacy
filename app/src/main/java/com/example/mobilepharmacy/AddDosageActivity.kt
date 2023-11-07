package com.example.mobilepharmacy

import android.content.ContentValues.TAG
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.collections.ArrayList


class AddDosageActivity : AppCompatActivity() {
    private var quantity: Int = 0
    private lateinit var timePickerLayout: TextInputLayout
    private lateinit var adapter: ArrayAdapter<String>
    private val medicinesList: ArrayList<Drugs> = ArrayList()
    private lateinit var customEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dosage)
        customEditText = findViewById(R.id.customEditText)

        // informacje o użytkowniku
        val firestoreDB = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""

        // odczytanie bazy danych leków
        val assetManager = assets
        val xmlInputStream = assetManager.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlInputStream, null)

        parseXml(parser)?.let { medicinesList.addAll(it) }

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.auto_complete_txt)
        // Tworzenie adaptera i przypisanie go do AutoCompleteTextView
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicinesList.map { it.nazwaProduktu })
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnClickListener{
            autoCompleteTextView.showDropDown()
        }

        // Ustawienie minimalnej ilości znaków, po których mają się pojawiać sugestie
        autoCompleteTextView.threshold = 1

        // Nasłuchiwanie zmian w AutoCompleteTextView
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Przed zmianą tekstu
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Podczas zmiany tekstu
            }

            override fun afterTextChanged(s: Editable?) {
                // Po zmianie tekstu
                filterMedicines(s.toString())
            }
        })

        // Nasłuchiwacz wyboru leku z listy
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedMedicineName = adapter.getItem(position)
            if (selectedMedicineName != null) {
                checkMedicineInDatabase(selectedMedicineName, firestoreDB, email)
            }
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

        // obecna dala
        val currentDate = Calendar.getInstance()
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        numberPickerDay.value = currentDay
        numberPickerMonth.value = currentDate.get(Calendar.MONTH) + 1
        numberPickerYear.value = currentDate.get(Calendar.YEAR)


        numberPickerMonth.setOnValueChangedListener { _, _, newVal ->
            val selectedMonth = newVal
            val daysInMonth = when (selectedMonth) {
                2 -> if (isLeapYear(numberPickerYear.value)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            numberPickerDay.maxValue = daysInMonth
        }

        val buttonZapisz = findViewById<Button>(R.id.buttonDodaj)
        buttonZapisz.setOnClickListener {
            val nazwaProduktu = autoCompleteTextView.text.toString()
            val customQuantity = customEditText.text.toString().toIntOrNull()
            if (nazwaProduktu.isEmpty()) {
                Toast.makeText(this, "Nazwa leku nie może być pusta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                if (customQuantity in 1..23) {
                    saveDataToFirebase(firestoreDB, email)
                } else {
                    Toast.makeText(this, "Niepoprawne dawkowanie", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Filtruj leki na podstawie wprowadzonego tekstu
    private fun filterMedicines(query: String) {
        val filteredMedicines = medicinesList.map { it.nazwaProduktu }.filter { it.contains(query, ignoreCase = true) }
        adapter.clear()
        adapter.addAll(filteredMedicines)
        adapter.notifyDataSetChanged()
    }

        // Sprawdzenie, czy dany lek znajduje się już w apteczce użytkownika
        private fun checkMedicineInDatabase(selectedMedicineName: String, firestoreDB: FirebaseFirestore, email: String) {

            val lekiRef = firestoreDB.collection("leki")
            lekiRef
                .whereEqualTo("email", email)
                .whereEqualTo("nazwaProduktu", selectedMedicineName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Toast.makeText(this, "$email, $selectedMedicineName", Toast.LENGTH_SHORT).show()
                    if (!querySnapshot.isEmpty) {
                        // Lek został znaleziony w bazie dla danego użytkownika
                        val alertDialogBuilder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
                        alertDialogBuilder.setTitle("Lek jest już w bazie danych")
                        alertDialogBuilder.setMessage("Czy zakupiłeś nowe opakowanie czy chcesz skorzystać ze starego opakowania?")

                        // Dodaj opcję "Nowe opakowanie"
                        alertDialogBuilder.setPositiveButton("Nowe opakowanie") { dialog, which ->
                            // Obsługa wyboru "Nowe opakowanie"
                            allVisibleNewDrug()
                        }

                        // Dodaj opcję "Stare opakowanie"
                        alertDialogBuilder.setNegativeButton("Stare opakowanie") { dialog, which ->
                            // Obsługa wyboru "Stare opakowanie"
                            // Pobranie pojemności opakowania z bazy
                            val capacity = querySnapshot.documents[0].get("pojemnosc")
                            val expirationDate = querySnapshot.documents[0].get("dataWaznosci")
                            val dosage = querySnapshot.documents[0].get("dawkowanie")
                            val frequency = querySnapshot.documents[0].get("czestotliwosc")
                            allVisibleFromDB(capacity, expirationDate, dosage, frequency)
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()

                    } else {
                        // Lek nie został znaleziony w bazie dla danego użytkownika
                        allVisibleNewDrug()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
                }
    }

    // wyświetla wszystko
    private fun allVisibleNewDrug() {
        val iloscTabletekTextView = findViewById<TextView>(R.id.capacityTextView)
        iloscTabletekTextView.visibility = View.VISIBLE

        val dateTextView = findViewById<TextView>(R.id.date)
        dateTextView.visibility = View.VISIBLE

        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        numberPickerDay.visibility = View.VISIBLE

        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        numberPickerMonth.visibility = View.VISIBLE

        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
        numberPickerYear.visibility = View.VISIBLE

        val dosageTextView = findViewById<TextView>(R.id.dosageTextView)
        dosageTextView.visibility = View.VISIBLE

        val dosageSpinner = findViewById<Spinner>(R.id.quantitySpinner)
        dosageSpinner.visibility = View.VISIBLE

        val quantityOptions = resources.getStringArray(R.array.quantity_options)
        val quantityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityOptions)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dosageSpinner.adapter = quantityAdapter

        val customCheckBox = findViewById<CheckBox>(R.id.customCheckBox)
        customCheckBox.visibility = View.VISIBLE

        val capacityTextView = findViewById<TextView>(R.id.capacityTextView)
        capacityTextView.visibility = View.VISIBLE

        val customEditText = findViewById<EditText>(R.id.customEditText)
        customEditText.visibility = View.VISIBLE

        val iloscTabletekEditText = findViewById<EditText>(R.id.iloscTabletek)
        iloscTabletekEditText.visibility = View.VISIBLE

        timePickerLayout = findViewById(R.id.timePickerLayout)

        dosageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                quantity = position + 1
                updateTimeWindows()

                if (customCheckBox.isChecked) {
                    customEditText.visibility = View.GONE
                    timePickerLayout.removeAllViews()
                    for (i in 1..quantity) {
                        val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                        )
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    }
                } else {
                    val customQuantity = customEditText.text.toString().toIntOrNull()
                    if (customQuantity != null && customQuantity > 1) {
                        customEditText.visibility = View.GONE
                        timePickerLayout.removeAllViews()
                        val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                        )
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    } else {
                        customEditText.visibility = View.VISIBLE
                        timePickerLayout.removeAllViews()
                        val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                        )
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie wykonujemy żadnej akcji
            }
        }
        customCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customEditText.visibility = View.GONE
                updateTimeWindows()
            } else {
                customEditText.visibility = View.VISIBLE
                timePickerLayout.removeAllViews()
                val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                )
                timePicker.setIs24HourView(true)
                timePickerLayout.addView(timePicker)
            }
        }
    }

    // ustawienie pól na widzialne i uzupełnienie danych
    private fun allVisibleFromDB(capacity: Any?, expirationDate: Any?, dosage: Any?, frequency:Any?) {
        val iloscTabletekTextView = findViewById<TextView>(R.id.capacityTextView)
        iloscTabletekTextView.visibility = View.VISIBLE

        val dateTextView = findViewById<TextView>(R.id.date)
        dateTextView.visibility = View.VISIBLE

        // data ważności
        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        numberPickerDay.visibility = View.VISIBLE

        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        numberPickerMonth.visibility = View.VISIBLE

        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
        numberPickerYear.visibility = View.VISIBLE

        // rozbicie daty na wartości
        val dateParts = expirationDate.toString().split("-")
        if (dateParts.size == 3) {
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val year = dateParts[2].toInt()
            numberPickerDay.value = day
            numberPickerMonth.value = month
            numberPickerYear.value = year
        } else {
            println("Nieprawidłowy format daty")
        }

        val dosageTextView = findViewById<TextView>(R.id.dosageTextView)
        dosageTextView.visibility = View.VISIBLE

        val dosageSpinner = findViewById<Spinner>(R.id.quantitySpinner)
        dosageSpinner.visibility = View.VISIBLE
        val quantityOptions = resources.getStringArray(R.array.quantity_options)
        val quantityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityOptions)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dosageSpinner.adapter = quantityAdapter

        if (dosage != null) {
            val position = quantityAdapter.getPosition( dosage.toString())
            if (position >= 0) {
                dosageSpinner.setSelection(position)
            }
        }

        val customCheckBox = findViewById<CheckBox>(R.id.customCheckBox)
        customCheckBox.visibility = View.VISIBLE

        val capacityTextView = findViewById<TextView>(R.id.capacityTextView)
        capacityTextView.visibility = View.VISIBLE

        // częstotliwość brania
        val customEditText = findViewById<EditText>(R.id.customEditText)
        customEditText.visibility = View.VISIBLE

        if (frequency!=null){
            customEditText.setText(frequency.toString())
        }

        // Pobierz referencję do pola EditText
        val capacityEditText = findViewById<EditText>(R.id.iloscTabletek)
        capacityEditText.visibility = View.VISIBLE
        if (capacity != null)
        {
            capacityEditText.setText(capacity.toString())
        }

        // Tworzenie TextWatcher do monitorowania wprowadzanych wartości
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Przed zmianą tekstu
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Podczas zmiany tekstu
            }

            override fun afterTextChanged(s: Editable?) {
                // Po zmianie tekstu
                val capacity = s.toString().toIntOrNull()
                if (capacity != null && capacity < 0) {
                    // Jeśli wprowadzona liczba jest mniejsza od zera, ustaw na zero
                    capacityEditText.setText("0")
                }
            }
        }
        // Dodaj TextWatcher do pola EditText
        capacityEditText.addTextChangedListener(textWatcher)

        timePickerLayout = findViewById(R.id.timePickerLayout)

        dosageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                quantity = position + 1
                updateTimeWindows()

                if (customCheckBox.isChecked) {
                    customEditText.visibility = View.GONE
                    timePickerLayout.removeAllViews()
                    for (i in 1..quantity) {
                        val timePicker = TimePicker(ContextThemeWrapper(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                            , R.style.TimePickerTheme))
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    }
                } else {
                    val customQuantity = customEditText.text.toString().toIntOrNull()
                    if (customQuantity != null && customQuantity > 1) {
                        customEditText.visibility = View.GONE
                        timePickerLayout.removeAllViews()
                        val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                        )
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    } else {
                        customEditText.visibility = View.VISIBLE
                        timePickerLayout.removeAllViews()
                        val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                        )
                        timePicker.setIs24HourView(true)
                        timePickerLayout.addView(timePicker)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie wykonujemy żadnej akcji
            }
        }
        customCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customEditText.visibility = View.GONE
                updateTimeWindows()
            } else {
                customEditText.visibility = View.VISIBLE
                timePickerLayout.removeAllViews()
                val timePicker = TimePicker(ContextThemeWrapper(this@AddDosageActivity, R.style.TimePickerTheme)
                )
                timePicker.setIs24HourView(true)
                timePickerLayout.addView(timePicker)
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
            val timePicker = TimePicker(ContextThemeWrapper(this, R.style.TimePickerTheme))
            timePicker.setIs24HourView(true)
            timePicker.setBackgroundColor(Color.WHITE)
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
                    drug

                    if (name == "produktLeczniczy") {
                        drug = Drugs()
                        drug.nazwaProduktu = parser.getAttributeValue(null, "nazwaProduktu")
                    } else if (name == "opakowanie" && drug != null) {
                        val rodzajOpakowania = parser.getAttributeValue(null, "rodzajOpakowania")
                        val pojemnosc = parser.getAttributeValue(null, "pojemnosc")

                        if (rodzajOpakowania != null) {
                            drug.rodzajOpakowania = rodzajOpakowania
                        } else {
                            // Jeśli rodzajOpakowania jest null, możesz przypisać domyślną wartość
                            drug.rodzajOpakowania = "tabletki"
                        }

                        if (pojemnosc != null) {
                            drug.pojemnosc = pojemnosc
                        } else {
                            drug.pojemnosc = "0"
                        }
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
    fun saveDataToFirebase(firestoreDB: FirebaseFirestore, email: String) {
            val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
            val nazwaProduktu = autoComplete.text.toString()

            val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
            val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
            val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
            val iloscTabletek = findViewById<EditText>(R.id.iloscTabletek)

            val day = numberPickerDay.value
            val month = numberPickerMonth.value
            val year = numberPickerYear.value
            val dataWaznosci = String.format("%02d-%02d-%d", day, month, year)

            val timePickerLayout = findViewById<TextInputLayout>(R.id.timePickerLayout)
            val dawkowanie = ArrayList<String>()

            val customCheckbox = findViewById<CheckBox>(R.id.customCheckBox)

            if (customCheckbox.isChecked) {
                for (i in 0 until timePickerLayout.childCount) {
                    val timePicker = timePickerLayout.getChildAt(i) as TimePicker
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    val timeString = String.format("%02d:%02d", hour, minute)
                    dawkowanie.add(timeString)
                }
            } else {
                val customQuantity = customEditText.text.toString().toIntOrNull()
                if (customQuantity != null && customQuantity in 1..24) {
                    val firstTimePicker = timePickerLayout.getChildAt(0) as TimePicker
                    val hour = firstTimePicker.hour
                    val minute = firstTimePicker.minute
                    val initialTimeString = String.format("%02d:%02d", hour, minute)
                    dawkowanie.add(initialTimeString)

                    var multiplier = 1
                    for (i in 1 until quantity) {
                        var nextHour = hour + (customQuantity * multiplier)
                        while (nextHour >= 24) {
                            nextHour -= 24
                        }
                        val nextTimeString = String.format("%02d:%02d", nextHour, minute)
                        dawkowanie.add(nextTimeString)
                        multiplier *= 2
                    }
                } else {
                    val timePicker = timePickerLayout.getChildAt(0) as TimePicker
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    val timeString = String.format("%02d:%02d", hour, minute)
                    dawkowanie.add(timeString)
                }
            }

            // Sprawdź, czy nazwa leku już istnieje w bazie dla danego użytkownika
            firestoreDB.collection("leki")
                .whereEqualTo("email", email)
                .whereEqualTo("nazwaProduktu", nazwaProduktu)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // Tworzenie mapy z danymi
                        val dataMap = hashMapOf<String, Any>()
                        dataMap["email"] = email
                        dataMap["nazwaProduktu"] = nazwaProduktu
                        dataMap["dataWaznosci"] = dataWaznosci
                        dataMap["dawkowanie"] = dawkowanie
                        dataMap["pojemnosc"] = iloscTabletek.text.toString()

                        // Dodawanie danych do Firestore
                        firestoreDB.collection("leki")
                            .add(dataMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Lek został dodany.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Błąd podczas dodawania danych do Firestore", e)
                                Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // sprawdzenie czy podane wartości zgadzają się z tymi, które są w bazie,
                        // jeśli nie, to uaktualniamy zmiany i pytamy się o akceprację tych zmian
                        Toast.makeText(
                            this,
                            "Lek o nazwie $nazwaProduktu już istnieje w bazie.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Błąd podczas sprawdzania danych w Firestore", e)
                    Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
                }
        }
}