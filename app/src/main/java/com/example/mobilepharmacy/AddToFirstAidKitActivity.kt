package com.example.mobilepharmacy

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.Calendar

// dodanie nowego leku do apteczki, podajemy tylko informacje o dacie ważności oraz ilości tabletek w opakowaniu

class AddToFirstAidKitActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<String>
    private val medicinesList: ArrayList<Drugs> = ArrayList()
    private var newOld: Boolean =
        true // new_old - oznacza, czy jest nowe czy stare opawkowanie true - nowe false - stare
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_first_aid_kit)

        // informacje o użytkowniku
        val firestoreDB = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""

        // odczytanie bazy danych leków
        val assetManager = assets
        val xmlInputStream =
            assetManager.open("Rejestr_Produktow_Leczniczych_calosciowy_stan_na_dzien_20230511.xml")

        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(xmlInputStream, null)

        parseXml(parser)?.let { medicinesList.addAll(it) }

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.auto_complete_txt)
        // Tworzenie adaptera i przypisanie go do AutoCompleteTextView
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            medicinesList.map { it.nazwaProduktu })
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnClickListener {
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
            val daysInMonth = when (newVal) {
                2 -> if (isLeapYear(numberPickerYear.value)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            numberPickerDay.maxValue = daysInMonth
        }

        val buttonZapisz = findViewById<Button>(R.id.buttonDodaj)
        buttonZapisz.setOnClickListener {
            val nazwaProduktu = autoCompleteTextView.text.toString()

            if (nazwaProduktu.isEmpty()) {
                Toast.makeText(this, "Nazwa leku nie może być pusta.", Toast.LENGTH_SHORT).show()
            } else {
                val capacity = findViewById<EditText>(R.id.capacityEditText)

                val day = numberPickerDay.value
                val month = numberPickerMonth.value
                val year = numberPickerYear.value
                // wprowadzona data
                val inputDate = Calendar.getInstance()
                inputDate.set(Calendar.YEAR, year)
                inputDate.set(Calendar.MONTH, month - 1)  // Miesiące są indeksowane od 0
                inputDate.set(Calendar.DAY_OF_MONTH, day)

                if (capacity != null &&  !inputDate.before(currentDate)) {
                    saveDataToFirebase(firestoreDB, email)
                } else if (inputDate.before(currentDate)) {
                    Toast.makeText(this, "Lek jest przeterminowany", Toast.LENGTH_SHORT)
                        .show()
                }else if ((capacity.text.toString().toDoubleOrNull()?:0.0) > 99 )
                {
                    Toast.makeText(this, "Za duża pojemność opakowania", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    Toast.makeText(this, "Wszystkie pola muszą być uzupełnione", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Filtruj leki na podstawie wprowadzonego tekstu
    private fun filterMedicines(query: String) {
        val filteredMedicines =
            medicinesList.map { it.nazwaProduktu }.filter { it.contains(query, ignoreCase = true) }
        adapter.clear()
        adapter.addAll(filteredMedicines)
        adapter.notifyDataSetChanged()
    }

    // Sprawdzenie, czy dany lek znajduje się już w apteczce użytkownika
    private fun checkMedicineInDatabase(
        selectedMedicineName: String,
        firestoreDB: FirebaseFirestore,
        email: String
    ) {

        val lekiRef = firestoreDB.collection("leki")
        lekiRef
            .whereEqualTo("email", email)
            .whereEqualTo("nazwaProduktu", selectedMedicineName)
            .get()
            .addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {
                    // Lek został znaleziony w bazie dla danego użytkownika
                    val alertDialogBuilder =
                        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
                    alertDialogBuilder.setTitle("Lek jest już w bazie danych")
                    alertDialogBuilder.setMessage("Czy zakupiłeś nowe opakowanie czy chcesz skorzystać ze starego opakowania?")

                    // Dodaj opcję "Nowe opakowanie"
                    alertDialogBuilder.setPositiveButton("Nowe opakowanie") { _, _ ->
                        // Obsługa wyboru "Nowe opakowanie"
                        newOld = true
                        allVisibleNewDrug()
                    }

                    // Dodaj opcję "Stare opakowanie"
                    alertDialogBuilder.setNegativeButton("Stare opakowanie") { _, _ ->
                        // Obsługa wyboru "Stare opakowanie"
                        // Pobranie pojemności opakowania z bazy
                        newOld = false
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()

                } else {
                    // Lek nie został znaleziony w bazie dla danego użytkownika
                    allVisibleNewDrug()
                }
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this, "Błąd połączenia z bazą", Toast.LENGTH_SHORT).show()
            }
    }

    // wyświetla wszystko
    private fun allVisibleNewDrug() {
        // obecna data
        val currentDate = Calendar.getInstance()
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        val dateTextView = findViewById<TextView>(R.id.date)
        dateTextView.visibility = View.VISIBLE

        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        numberPickerDay.visibility = View.VISIBLE
        numberPickerDay.isEnabled = true
        numberPickerDay.value = currentDay

        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        numberPickerMonth.visibility = View.VISIBLE
        numberPickerMonth.isEnabled = true
        numberPickerMonth.value = currentDate.get(Calendar.MONTH) + 1

        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
        numberPickerYear.visibility = View.VISIBLE
        numberPickerYear.isEnabled = true
        numberPickerYear.value = currentDate.get(Calendar.YEAR)

        val capacityTextView = findViewById<TextView>(R.id.capacityTextView)
        capacityTextView.visibility = View.VISIBLE

        val iloscTabletekEditText = findViewById<EditText>(R.id.capacityEditText)
        iloscTabletekEditText.visibility = View.VISIBLE
        iloscTabletekEditText.setText("")
    }

    // Funkcja sprawdzająca, czy rok jest przestępny
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser): ArrayList<Drugs>? {
        var drugs: ArrayList<Drugs>? = null
        var eventType = parser.eventType
        var drug: Drugs? = null
        var halfwayReached = false
        val MAX_DRUGS = 500

        while (eventType != XmlPullParser.END_DOCUMENT && !halfwayReached) {
            val name: String
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> drugs = ArrayList()
                XmlPullParser.START_TAG -> {
                    name = parser.name

                    if (name == "produktLeczniczy") {
                        drug = Drugs()
                        drug.nazwaProduktu = parser.getAttributeValue(null, "nazwaProduktu")
                    } else if (name == "opakowanie" && drug != null) {
                        val rodzajOpakowania = parser.getAttributeValue(null, "rodzajOpakowania")
                        val pojemnosc = parser.getAttributeValue(null, "pojemnosc")

                        if (rodzajOpakowania.isNullOrBlank() || rodzajOpakowania.equals("null", ignoreCase = true)) {
                            // Jeśli rodzajOpakowania jest null lub pusty, przypisz domyślną wartość
                            drug.rodzajOpakowania = "tabletki"
                        } else {
                            halfwayReached = true
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
                        // Dodaj warunek sprawdzający, czy osiągnięto środek dokumentu
                        if (drugs.size >= MAX_DRUGS / 2) {
                            halfwayReached = true
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return drugs
    }
    private fun saveDataToFirebase(firestoreDB: FirebaseFirestore, email: String) {
        val autoComplete: AutoCompleteTextView = findViewById(R.id.auto_complete_txt)
        val nazwaProduktu = autoComplete.text.toString()

        val numberPickerDay = findViewById<NumberPicker>(R.id.numberPickerDay)
        val numberPickerMonth = findViewById<NumberPicker>(R.id.numberPickerMonth)
        val numberPickerYear = findViewById<NumberPicker>(R.id.numberPickerYear)
        val iloscTabletek = findViewById<EditText>(R.id.capacityEditText)

        val day = numberPickerDay.value
        val month = numberPickerMonth.value
        val year = numberPickerYear.value
        val dataWaznosci = String.format("%02d-%02d-%d", day, month, year)

        // Sprawdź, czy nazwa leku już istnieje w bazie dla danego użytkownika
        firestoreDB.collection("leki")
            .whereEqualTo("email", email)
            .whereEqualTo("nazwaProduktu", nazwaProduktu)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Tworzenie mapy z danymi
                    val dataMap = hashMapOf<String, Any>()
                    val iloscTabletekNumber = iloscTabletek.text.toString().toDoubleOrNull() ?: 0
                    dataMap["email"] = email
                    dataMap["nazwaProduktu"] = nazwaProduktu
                    dataMap["dataWaznosci"] = dataWaznosci
                    dataMap["pojemnosc"] = iloscTabletekNumber

                    // Dodawanie danych do Firestore
                    firestoreDB.collection("leki")
                        .add(dataMap)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Lek został dodany.",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, AfterLoginActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            Log.e(ContentValues.TAG, "Błąd podczas dodawania danych do Firestore", e)
                            Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    if (newOld) {
                        val dataMap = hashMapOf<String, Any>()
                        val iloscTabletekNumber = iloscTabletek.text.toString().toDoubleOrNull() ?: 0
                        dataMap["email"] = email
                        dataMap["nazwaProduktu"] = nazwaProduktu
                        dataMap["dataWaznosci"] = dataWaznosci
                        dataMap["pojemnosc"] = iloscTabletekNumber

                        // Dodawanie danych do Firestore
                        firestoreDB.collection("leki")
                            .add(dataMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Lek został dodany.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, AfterLoginActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                Log.e(ContentValues.TAG, "Błąd podczas dodawania danych do Firestore", e)
                                Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Błąd podczas sprawdzania danych w Firestore", e)
                Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
            }
    }

}