package com.example.mobilepharmacy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var saveChangesButton: Button

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // Inicjalizacja elementów interfejsu
        newPasswordEditText = findViewById(R.id.passwordSettings)
        confirmPasswordEditText = findViewById(R.id.repeatSettings)
        saveChangesButton = findViewById(R.id.buttonSaveChanges)

        // Inicjalizacja instancji FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Obsługa kliknięcia przycisku "Zapisz zmiany"
        saveChangesButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                // Aktualizacja hasła użytkownika
                val user = firebaseAuth.currentUser
                if (user != null) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, AfterLoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                val errorMessage = task.exception?.message
                                Toast.makeText(this, "Nie udało się zmienić hasła: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Nie udało się pobrać informacji o użytkowniku", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hasła się nie zgadzają", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
