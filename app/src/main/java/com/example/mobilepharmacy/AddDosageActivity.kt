package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import kotlin.collections.ArrayList


class AddDosageActivity : AppCompatActivity() {
//    private var quantity: Int = 0
//    private lateinit var timePickerLayout: TextInputLayout
//    private val drugsList: ArrayList<String> = ArrayList()
//    private val filteredDrugsList: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private val medicinesList: ArrayList<Drugs> = ArrayList()
    //    private lateinit var customEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dosage)

        // pobranie danych o użytkowniku
        val firestoreDB = FirebaseFirestore.getInstance()
        // Pobieranie ID aktualnie zalogowanego użytkownika z SharedPreferences
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userID", "") ?: ""

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
                checkMedicineInDatabase(selectedMedicineName)
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
        private fun checkMedicineInDatabase(selectedMedicineName: String) {
            val firestoreDB = FirebaseFirestore.getInstance()
            val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
            val email = sharedPreferences.getString("email", "") ?: ""

            val lekiRef = firestoreDB.collection("leki")
            lekiRef
                .whereEqualTo("email", email)
                .whereEqualTo("nazwaProduktu", selectedMedicineName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Toast.makeText(this, "$email, $selectedMedicineName", Toast.LENGTH_SHORT).show()
                    if (!querySnapshot.isEmpty) {
                        // Pobranie pojemności opakowania z bazy
                         val pojemnosc = querySnapshot.documents[0].get("pojemnosc")
//                        Toast.makeText(this, "Pojemnosc $pojemnosc", Toast.LENGTH_SHORT).show()
                        // Lek został znaleziony w bazie dla danego użytkownika
//                        Toast.makeText(this, "Leki, działa", Toast.LENGTH_SHORT).show()
                        val alertDialogBuilder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
                        alertDialogBuilder.setTitle("Lek jest już w bazie danych")
                        alertDialogBuilder.setMessage("Czy zakupiłeś nowe opakowanie czy chcesz skorzystać ze starego opakowania?")

                        // Dodaj opcję "Nowe opakowanie"
                        alertDialogBuilder.setPositiveButton("Nowe opakowanie") { dialog, which ->
                            // Obsługa wyboru "Nowe opakowanie"
                            // Tutaj możesz dodać kod do obsługi nowego opakowania
                        }

                        // Dodaj opcję "Stare opakowanie"
                        alertDialogBuilder.setNegativeButton("Stare opakowanie") { dialog, which ->
                            // Obsługa wyboru "Stare opakowanie"
                            // Tutaj możesz dodać kod do obsługi starego opakowania
                            allVisibleFromDB(pojemnosc)
                        }
                        val alertDialog = alertDialogBuilder.create()

                        alertDialog.show()

                    } else {
                        // Lek nie został znaleziony w bazie dla danego użytkownika
                        Toast.makeText(this, "Leki - brak w bazie działa", Toast.LENGTH_SHORT).show()
                        val iloscTabletekEditText = findViewById<EditText>(R.id.iloscTabletek)
                        iloscTabletekEditText.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Leki, nie działa", Toast.LENGTH_SHORT).show()
                }


//        var databaseRef = FirebaseDatabase.getInstance().reference
//        customEditText = findViewById(R.id.customEditText)

//        fun saveDataToFirebase() {
//            val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
//            val nazwaProduktu = autoComplete.text.toString()
//
//            val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
//            val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
//            val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
//            val iloscTabletek = findViewById<EditText>(R.id.iloscTabletek)
//            val day = numberPickerDay.value
//            val month = numberPickerMonth.value
//            val year = numberPickerYear.value
//            val dataWaznosci = String.format("%02d-%02d-%d", day, month, year)
//
//            val timePickerLayout = findViewById<TextInputLayout>(R.id.timePickerLayout)
//            val dawkowanie = ArrayList<String>()
//
//            val customCheckbox = findViewById<CheckBox>(R.id.customCheckBox)
//
//            if (customCheckbox.isChecked) {
//                for (i in 0 until timePickerLayout.childCount) {
//                    val timePicker = timePickerLayout.getChildAt(i) as TimePicker
//                    val hour = timePicker.hour
//                    val minute = timePicker.minute
//                    val timeString = String.format("%02d:%02d", hour, minute)
//                    dawkowanie.add(timeString)
//                }
//            } else {
//                val customQuantity = customEditText.text.toString().toIntOrNull()
//                if (customQuantity != null && customQuantity in 1..24) {
//                    val firstTimePicker = timePickerLayout.getChildAt(0) as TimePicker
//                    val hour = firstTimePicker.hour
//                    val minute = firstTimePicker.minute
//                    val initialTimeString = String.format("%02d:%02d", hour, minute)
//                    dawkowanie.add(initialTimeString)
//
//                    var multiplier = 1
//                    for (i in 1 until quantity) {
//                        var nextHour = hour + (customQuantity * multiplier)
//                        while (nextHour >= 24) {
//                            nextHour -= 24
//                        }
//                        val nextTimeString = String.format("%02d:%02d", nextHour, minute)
//                        dawkowanie.add(nextTimeString)
//                        multiplier *= 2
//                    }
//                } else {
//                } else {
//                    val timePicker = timePickerLayout.getChildAt(0) as TimePicker
//                    val hour = timePicker.hour
//                    val minute = timePicker.minute
//                    val timeString = String.format("%02d:%02d", hour, minute)
//                    dawkowanie.add(timeString)
//                }
//            }
//
//            val firestoreDB = FirebaseFirestore.getInstance()
//
//            // Pobieranie ID aktualnie zalogowanego użytkownika z SharedPreferences
//            val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
//            val userId = sharedPreferences.getString("userID", "") ?: ""
//
//            // Sprawdź, czy nazwa leku już istnieje w bazie dla danego użytkownika
//            firestoreDB.collection("leki")
//                .whereEqualTo("userId", userId)
//                .whereEqualTo("nazwaProduktu", nazwaProduktu)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (querySnapshot.isEmpty) {
//                        // Tworzenie mapy z danymi
//                        val dataMap = hashMapOf<String, Any>()
//                        dataMap["userId"] = userId
//                        dataMap["nazwaProduktu"] = nazwaProduktu
//                        dataMap["dataWaznosci"] = dataWaznosci
//                        dataMap["dawkowanie"] = dawkowanie
//                        dataMap["ilosc tabletek"] = iloscTabletek.text.toString()
//
//                        // Dodawanie danych do Firestore
//                        firestoreDB.collection("leki")
//                            .add(dataMap)
//                            .addOnSuccessListener {
//                                Toast.makeText(
//                                    this,
//                                    "Lek został dodany.",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e(TAG, "Błąd podczas dodawania danych do Firestore", e)
//                                Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
//                            }
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Lek o nazwie $nazwaProduktu już istnieje w bazie.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "Błąd podczas sprawdzania danych w Firestore", e)
//                    Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
//                }
//        }
//
//
//        val buttonZapisz = findViewById<Button>(R.id.buttonDodaj)
//        buttonZapisz.setOnClickListener {
//            val nazwaProduktu = autoComplete.text.toString()
//            val customQuantity = customEditText.text.toString().toIntOrNull()
//            if (nazwaProduktu.isEmpty()) {
//                Toast.makeText(this, "Nazwa leku nie może być pusta.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            } else {
//                if (customQuantity in 1..23) {
//                    saveDataToFirebase()
//                } else {
//                    Toast.makeText(this, "Niepoprawne dawkowanie", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//
//        autoComplete = findViewById(R.id.auto_complete_txt)
//        adapter = ArrayAdapter(this, R.layout.list_drugs, filteredDrugsList)
//        autoComplete.setAdapter(adapter)
//
//        autoComplete.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//                // Niepotrzebne
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                filterDrugs(s.toString())
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                // Niepotrzebne
//            }
//        })
//
//        autoComplete.onItemClickListener =
//            AdapterView.OnItemClickListener { adapterView, view, i, l ->
//                val itemSelected = adapterView.getItemAtPosition(i)
//                Toast.makeText(this, "$itemSelected", Toast.LENGTH_SHORT).show()
//            }
//
//
//        // Uzyskanie referencji do slidów
//        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
//        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
//        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
//
//        // Konfiguracja slidów
//
//        // Slide dla dni
//        numberPickerDay.minValue = 1
//        numberPickerDay.maxValue = 31
//
//        // Slide dla miesięcy
//        numberPickerMonth.minValue = 1
//        numberPickerMonth.maxValue = 12
//
//        // Slide dla lat
//        numberPickerYear.minValue = 2023
//        numberPickerYear.maxValue = 2035
//
//        val currentDate = Calendar.getInstance()
//        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
//        numberPickerDay.minValue = currentDay
//
//        numberPickerMonth.minValue = currentDate.get(Calendar.MONTH) + 1
//        numberPickerYear.minValue = currentDate.get(Calendar.YEAR)
//
//
//        numberPickerMonth.setOnValueChangedListener { _, _, newVal ->
//            val selectedMonth = newVal
//            val daysInMonth = when (selectedMonth) {
//                2 -> if (isLeapYear(numberPickerYear.value)) 29 else 28
//                4, 6, 9, 11 -> 30
//                else -> 31
//            }
//            numberPickerDay.maxValue = daysInMonth
//        }

//        //        Lista leków
//        val drugsList: ArrayList<String> = ArrayList()
//
//        var pullParserFactory: XmlPullParserFactory
//        try {
//            pullParserFactory = XmlPullParserFactory.newInstance()
//            val parser = pullParserFactory.newPullParser()
//            val inputStream =
//                applicationContext.assets.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
//            parser.setInput(inputStream, null)
//
//            val drugs = parseXml(parser)
//
//            drugs?.let {
//                for (drug in it) {
//                    val text = drug.nazwaProduktu
//                    drugsList.add(text)
//                }
//            }
//
//
//            val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
//            val adapter2 = ArrayAdapter(this, R.layout.list_drugs, drugsList)
//            autoComplete.setAdapter(adapter2)
//
//            // rozwinięcie listy
//            autoComplete.setOnClickListener{
//                autoComplete.showDropDown()
//            }
//
//            autoComplete.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                    // Niepotrzebne
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    adapter2.filter.filter(s)
//                }
//
//                override fun afterTextChanged(s: Editable) {
//                    // Niepotrzebne
//                }
//            })
//
//            autoComplete.onItemClickListener =
//                AdapterView.OnItemClickListener { adapterView, view, i, l ->
//                    val itemSelected = adapterView.getItemAtPosition(i)
//                    Toast.makeText(this, "$itemSelected", Toast.LENGTH_SHORT).show()
//                    val firestoreDB = FirebaseFirestore.getInstance()
//
//                     // Pobieranie ID aktualnie zalogowanego użytkownika z SharedPreferences
//                    val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
//                    val userId = sharedPreferences.getString("userID", "") ?: ""
//
//                     // Sprawdź, czy nazwa leku już istnieje w bazie dla danego użytkownika
//                    firestoreDB.collection("leki")
//                        .whereEqualTo("userId", userId)
//                        .whereEqualTo("nazwaProduktu", itemSelected.toString())
//                        .get()
//                        .addOnSuccessListener { querySnapshot ->
//                            if (querySnapshot.isEmpty) {
//                                Toast.makeText(this, "Brak w bazie, idUżytkownika: $userId", Toast.LENGTH_SHORT).show()
//                            }
//                        // Tworzenie mapy z danymi
//                        val dataMap = hashMapOf<String, Any>()
//                        dataMap["userId"] = userId
//                        dataMap["nazwaProduktu"] = nazwaProduktu
//                        dataMap["dataWaznosci"] = dataWaznosci
//                        dataMap["dawkowanie"] = dawkowanie
//                        dataMap["ilosc tabletek"] = iloscTabletek.text.toString()
//
//                        // Dodawanie danych do Firestore
//                        firestoreDB.collection("leki")
//                            .add(dataMap)
//                            .addOnSuccessListener {
//                                Toast.makeText(
//                                    this,
//                                    "Lek został dodany.",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e(TAG, "Błąd podczas dodawania danych do Firestore", e)
//                                Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
//                            }
//                     else {
//                        Toast.makeText(
//                            this,
//                            "Lek o nazwie $itemSelected już istnieje w bazie.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "Błąd podczas sprawdzania danych w Firestore", e)
//                    Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
//                }
//                }
//        } catch (e: XmlPullParserException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }

//        //obsługa wyboru dawkowania
//
//        val quantityOptions = resources.getStringArray(R.array.quantity_options)
//        val quantityAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityOptions)
//        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        val quantitySpinner = findViewById<Spinner>(R.id.quantitySpinner)
//
//        quantitySpinner.adapter = quantityAdapter
//
//        timePickerLayout = findViewById(R.id.timePickerLayout)
//
//        val customCheckbox = findViewById<CheckBox>(R.id.customCheckBox)
//        val customEditText = findViewById<EditText>(R.id.customEditText)
//
//        customEditText.visibility = if (customCheckbox.isChecked) View.GONE else View.VISIBLE
//
//        quantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                quantity = position + 1
//                updateTimeWindows()
//
//                if (customCheckbox.isChecked) {
//                    customEditText.visibility = View.GONE
//                    timePickerLayout.removeAllViews()
//                    for (i in 1..quantity) {
//                        val timePicker = TimePicker(this@AddDosageActivity)
//                        timePicker.setIs24HourView(true)
//                        timePicker.setBackgroundColor(Color.WHITE)
//                        timePickerLayout.addView(timePicker)
//                    }
//                } else {
//                    val customQuantity = customEditText.text.toString().toIntOrNull()
//                    if (customQuantity != null && customQuantity in 1..24) {
//                        customEditText.visibility = View.GONE
//                        timePickerLayout.removeAllViews()
//                        val timePicker = TimePicker(this@AddDosageActivity)
//                        timePicker.setIs24HourView(true)
//                        timePicker.setBackgroundColor(Color.WHITE)
//                        timePickerLayout.addView(timePicker)
//                    } else {
//                        customEditText.visibility = View.VISIBLE
//                        timePickerLayout.removeAllViews()
//                        val timePicker = TimePicker(this@AddDosageActivity)
//                        timePicker.setIs24HourView(true)
//                        timePicker.setBackgroundColor(Color.WHITE)
//                        timePickerLayout.addView(timePicker)
//                    }
//                }
//            }

//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Nie wykonujemy żadnej akcji
//            }
//        }

//        customCheckbox.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                customEditText.visibility = View.GONE
//                updateTimeWindows()
//            } else {
//                customEditText.visibility = View.VISIBLE
//                timePickerLayout.removeAllViews()
//                val timePicker = TimePicker(this@AddDosageActivity)
//                timePicker.setIs24HourView(true)
//                timePicker.setBackgroundColor(Color.WHITE)
//                timePickerLayout.addView(timePicker)
//            }
//        }

    }

    // ustawienie pól na widzialne i uzupełnienie danych
    private fun allVisibleFromDB(pojemnosc: Any?) {
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

        val customCheckBox = findViewById<CheckBox>(R.id.customCheckBox)
        customCheckBox.visibility = View.VISIBLE

        val capacityTextView = findViewById<TextView>(R.id.capacityTextView)
        capacityTextView.visibility = View.VISIBLE

        val customEditText = findViewById<EditText>(R.id.customEditText)
        customEditText.visibility = View.VISIBLE

        // Pobierz referencję do pola EditText
        val iloscTabletekEditText = findViewById<EditText>(R.id.iloscTabletek)
        iloscTabletekEditText.visibility = View.VISIBLE
        iloscTabletekEditText.setText(pojemnosc.toString())

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
                val pojemnosc = s.toString().toIntOrNull()
                if (pojemnosc != null && pojemnosc < 0) {
                    // Jeśli wprowadzona liczba jest mniejsza od zera, ustaw na zero
                    iloscTabletekEditText.setText("0")
                }
            }
        }
        // Dodaj TextWatcher do pola EditText
        iloscTabletekEditText.addTextChangedListener(textWatcher)
    }

//    private fun filterDrugs(query: String) {
//        filteredDrugsList.clear()
//        for (drug in drugsList) {
//            if (drug.contains(query, ignoreCase = true)) {
//                filteredDrugsList.add(drug)
//            }
//        }
//    }

    // Funkcja sprawdzająca, czy rok jest przestępny
//    private fun isLeapYear(year: Int): Boolean {
//        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
//    }
//
//    private fun updateTimeWindows() {
//        timePickerLayout.removeAllViews()
//
//        for (i in 1..quantity) {
//            val timePicker = TimePicker(this)
//            timePicker.setIs24HourView(true)
//            timePicker.setBackgroundColor(Color.WHITE)
//            timePickerLayout.addView(timePicker)
//        }
//
//        timePickerLayout.visibility = View.VISIBLE
//    }

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
}