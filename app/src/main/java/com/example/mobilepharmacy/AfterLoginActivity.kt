package com.example.mobilepharmacy


import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.applandeo.materialcalendarview.EventDay
import com.google.firebase.Timestamp
import java.text.ParseException
import java.util.Calendar
import java.util.Locale
import com.applandeo.materialcalendarview.listeners.OnDayClickListener

// menu po zalogowaniu
class AfterLoginActivity : AppCompatActivity() {

    private lateinit var calendarView: com.applandeo.materialcalendarview.CalendarView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlogin)

//        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val drawerLayout: DrawerLayout = findViewById(R.id.calendar)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // boczne menu
        toggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                supportActionBar?.title = "Menu"
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                supportActionBar?.title = "After Login"
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, AfterLoginActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_account -> {
                    // Obsługa kliknięcia przycisku "Account"
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_settings -> {
                    // Obsługa kliknięcia przycisku "Settings"
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_logout -> {
                    // Obsługa kliknięcia przycisku "Logout"
                    logout()
                    true
                }

                else -> false
            }
        }

// MODYFIKACJE KALENDARZA
        calendarView = findViewById(R.id.calendarView)

// lista dni, w których wzięto lek
        val medicationsListDate = mutableListOf<Calendar>()

// połaczenie z bazą danych i dodanie do listy dni, w których wzięto lek
        val email = firebaseAuth.currentUser?.email
        if (email != null) {
            val userMedicationsRef = firestore.collection("checkedMedications")
                .whereEqualTo("email", email)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->

                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("medicationName")
                        val dateTimeString = document.getString("checkedDate")
                        if (dateTimeString != null) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            try {
                                val date = dateFormat.parse(dateTimeString)
                                date?.let {
                                    val calendar = Calendar.getInstance()
                                    calendar.time = it

                                    // Tutaj możesz użyć 'calendar' do dalszych działań
                                    val medicationInfo =
                                        "$medicationName \nData wzięcia: ${dateFormat.format(it)}"
                                    medicationsList.add(medicationInfo)

                                    // Dodaj datę do listy dni, w których wzięto lek
                                    medicationsListDate.add(calendar)
                                }
                            } catch (e: ParseException) {
                                e.printStackTrace()
                                // Obsłuż błąd parsowania daty
                            }
                        }
                    }

                    // Ustaw listę dni, w których wzięto lek, jako zaznaczone w kalendarzu
                    val dayDecorators = medicationsListDate.map { EventDay(it, R.drawable.dot) }
                    calendarView.setEvents(dayDecorators)
                }
                .addOnFailureListener { _ ->
                    // Handle failure
                }
        }

        // Ustaw obsługę kliknięcia na dzień
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedDay = eventDay.calendar

                // Pobierz obecną datę
                val today = Calendar.getInstance()

                // Pobierz datę w formacie dd/MM/yyyy
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = dateFormat.format(clickedDay.time)

                if (clickedDay.before(today)) {
                    // Sprawdź, czy istnieją dane o lekach wziętych danego dnia w bazie
                    val email = firebaseAuth.currentUser?.email
                    if (email != null) {
                        val userMedicationsRef = firestore.collection("checkedMedications")
                            .whereEqualTo("email", email)
                            .whereEqualTo("checkedDate", dateString)

                        userMedicationsRef.get()
                            .addOnSuccessListener { querySnapshot ->
                                val medicationsList = mutableListOf<String>()

                                for (document in querySnapshot.documents) {
                                    val medicationName = document.getString("medicationName")
                                    val checkedTime = document.getString("checkedTime")

                                    if (medicationName != null && checkedTime != null) {
                                        val medicationInfo =
                                            "$medicationName, Godzina wzięcia: $checkedTime"
                                        medicationsList.add(medicationInfo)
                                    }
                                }

                                if (medicationsList.isNotEmpty()) {
                                    // Wyświetl informacje o wziętych lekach
                                    showInfoDialog("Leki wzięte dnia $dateString", medicationsList.joinToString("\n"))
                                } else {
                                    // Jeśli nie wzięto leków tego dnia, wyświetl odpowiedni komunikat
                                    showInfoDialog("Brak wziętych leków", "Nie wzięto żadnych leków dnia $dateString")
                                }
                            }
                            .addOnFailureListener { _ ->
                                // Handle failure
                            }
                    }
                } else {
                    // Jeśli kliknięty dzień jest dniem przyszłym, pobierz informacje o dawkowaniu z bazy danych
                    val email = firebaseAuth.currentUser?.email
                    if (email != null) {
                        val dosageRef = firestore.collection("leki")
                            .whereEqualTo("email", email)

                        dosageRef.get()
                            .addOnSuccessListener { querySnapshot ->
                                val dosageList = mutableListOf<String>()

                                for (document in querySnapshot.documents) {
                                    val medicationName = document.getString("nazwaProduktu")
                                    val dosageInfo = document.get("dawkowanie") as List<String>?

                                    if (medicationName != null && dosageInfo != null) {
                                        val dosage =
                                            "$medicationName, Dawkowanie: ${dosageInfo.joinToString(", ")}"
                                        dosageList.add(dosage)
                                    }
                                }

                                if (dosageList.isNotEmpty()) {
                                    // Wyświetl informacje o dawkowaniu
                                    showInfoDialog("Dawkowanie na dzień $dateString", dosageList.joinToString("\n"))
                                } else {
                                    // Jeśli brak informacji o dawkowaniu tego dnia, wyświetl odpowiedni komunikat
                                    showInfoDialog("Brak informacji o dawkowaniu", "Brak danych o dawkowaniu na dzień $dateString")
                                }
                            }
                            .addOnFailureListener { _ ->
                                // Handle failure
                            }
                    }
                }
            }
        })



        // Przyciski przenoszące do kolejnych layoutów
        val myDosage = findViewById<AppCompatButton>(R.id.myDosage)
        myDosage.setOnClickListener {
            val intent = Intent(this, MyDosageActivity::class.java)
            startActivity(intent)
        }

        val addDosage = findViewById<AppCompatButton>(R.id.dosage)
        addDosage.setOnClickListener {
            val intent = Intent(this, AddDosageActivity::class.java)
            startActivity(intent)
        }

        val addToFirstKit = findViewById<AppCompatButton>(R.id.addToFirstAidKit)
        addToFirstKit.setOnClickListener {
            val intent = Intent(this, AddToFirstAidKitActivity::class.java)
            startActivity(intent)
        }

        val myFirstAidKit = findViewById<AppCompatButton>(R.id.myFirsAidKit)
        myFirstAidKit.setOnClickListener{
            val intent = Intent(this, MyFirstAidKit::class.java)
            startActivity(intent)
        }

        val myHealth = findViewById<AppCompatButton>(R.id.myHealth)
        myHealth.setOnClickListener{
            val intent = Intent(this, AddHealthActivity::class.java)
            startActivity(intent)
        }

        val serachDrug = findViewById<AppCompatButton>(R.id.searchDrugs)
        serachDrug.setOnClickListener {
            val intent = Intent(this, DiseasesActivity::class.java)
            startActivity(intent)
        }

        val backButtonR = findViewById<ImageView>(R.id.backButtonR)
        backButtonR.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        // sprawdzenie w bazie, czy kończy się jakiś lek, jeśli tak to będzie
        // umieszczony text pod kalendarzem, że opakowanie danego leku się kończy
        checkCapacity()
    }

    // Funkcja do konwersji daty i czasu na obiekt Calendar
    private fun convertDateTimeToCalendar(dateTime: Any): Calendar {
        val calendar = Calendar.getInstance()

        if (dateTime is Timestamp) {
            // Jeżeli dateTime jest obiektem Timestamp z Firebase
            calendar.time = dateTime.toDate()
        } else if (dateTime is String) {
            // Jeżeli dateTime jest ciągiem znaków (String)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            try {
                val date = dateFormat.parse(dateTime)
                date?.let { calendar.time = it }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        return calendar
    }

    @SuppressLint("SetTextI18n")
    private fun checkCapacity() {
        val email = firebaseAuth.currentUser?.email
        if (email != null) {
            val userMedicationsRef = firestore.collection("leki").whereEqualTo("email", email)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("nazwaProduktu")
                        val capacity = document.get("pojemnosc")
                        val capacityNumber = capacity.toString().toDoubleOrNull() ?: 0.0

                        if (medicationName != null && (capacityNumber < 10.0) ) {
                            val medicationInfo =
                                "$medicationName "
                            medicationsList.add(medicationInfo)
                        }
                    }
                    if (medicationsList.isNotEmpty()) {
                        val capacityTextView = findViewById<TextView>(R.id.alertCapacityTextViewAfterLogin)
                        capacityTextView.text = "Kończące się leki: ${medicationsList.joinToString(" ")}"
                    }
                }
                .addOnFailureListener { _ ->
                    // Handle failure
                }
        }
    }

    private fun logout() {
        // Usunięcie informacji o zalogowanym użytkowniku
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.remove("password")
        editor.apply()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        // zakaz cofania
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = if (FirebaseAuth.getInstance().currentUser == null) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, Login::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    // Funkcja do wyświetlania okna informacyjnego
    private fun showInfoDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        builder.show()
    }
}