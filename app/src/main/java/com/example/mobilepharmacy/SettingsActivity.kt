package com.example.mobilepharmacy

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SettingsActivity : AppCompatActivity() {

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var numberPickerMonth: NumberPicker
    private lateinit var numberPickerDay: NumberPicker
    private lateinit var numberPickerYear: NumberPicker
    private lateinit var buttonSaveChanges: AppCompatButton

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextUsername = findViewById(R.id.editTextUsername)
        numberPickerMonth = findViewById(R.id.numberPickerMonth)
        numberPickerDay = findViewById(R.id.numberPickerDay)
        numberPickerYear = findViewById(R.id.numberPickerYear)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)

        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Set up NumberPicker values
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 12
        numberPickerDay.minValue = 1
        numberPickerDay.maxValue = 31
        numberPickerYear.minValue = 1900
        numberPickerYear.maxValue = 2100

        // Get today's date
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Add 1 because months are zero-based
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Set default values for NumberPickers
        numberPickerMonth.value = currentMonth
        numberPickerDay.value = currentDay
        numberPickerYear.value = currentYear

        buttonSaveChanges.setOnClickListener {
            saveChanges()
        }
    }



    private fun saveChanges() {
        val firstName = editTextFirstName.text.toString().trim()
        val lastName = editTextLastName.text.toString().trim()
        val username = editTextUsername.text.toString().trim()
        val birthMonth = numberPickerMonth.value
        val birthDay = numberPickerDay.value
        val birthYear = numberPickerYear.value

        // Utwórz obiekt z danymi użytkownika
        val userData = HashMap<String, Any>()
        userData["firstName"] = firstName
        userData["lastName"] = lastName
        userData["username"] = username
        userData["birthMonth"] = birthMonth
        userData["birthDay"] = birthDay
        userData["birthYear"] = birthYear

        // Sprawdź, czy wprowadzona data jest większa niż dzisiejsza data
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Add 1 because months are zero-based
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (birthYear > currentYear ||
            (birthYear == currentYear && birthMonth > currentMonth) ||
            (birthYear == currentYear && birthMonth == currentMonth && birthDay > currentDay)
        ) {
            // Wyświetl komunikat lub wykonaj inne działania w przypadku niepoprawnej daty
            // Na przykład: Toast.makeText(this, "Niepoprawna data urodzenia", Toast.LENGTH_SHORT).show()
            return
        }

        // Zapisz dane użytkownika do bazy danych Firebase
        val userDocument = firestore.collection("users").document(userId)
        userDocument.set(userData)
            .addOnSuccessListener {
                // Zapisano dane pomyślnie
                // Możesz dodać odpowiednie działania po zapisaniu danych, na przykład wyświetlić powiadomienie
                // lub przejść do innej aktywności
            }
            .addOnFailureListener { e ->
                // Wystąpił błąd podczas zapisywania danych
                // Możesz dodać odpowiednie działania w przypadku niepowodzenia, na przykład wyświetlić komunikat o błędzie
            }
    }

}
