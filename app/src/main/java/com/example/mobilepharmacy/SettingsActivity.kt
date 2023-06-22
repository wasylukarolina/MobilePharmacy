package com.example.mobilepharmacy

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
                firebaseAuth.currentUser?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
//                            Toast.makeText()
                        } else {
//                            Toast.makeText()
                        }
                    }
            } else {
//                Toast.makeText()
            }
        }
    }
}
