package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddHealthActivity : AppCompatActivity() {

    private lateinit var checkboxCukrzyca: CheckBox
    private lateinit var checkboxAstma: CheckBox
    private lateinit var checkboxCiaza: CheckBox
    private lateinit var checkboxChorobySerca: CheckBox

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_health)

        checkboxCukrzyca = findViewById(R.id.checkboxCukrzyca)
        checkboxAstma = findViewById(R.id.checkboxAstma)
        checkboxCiaza = findViewById(R.id.checkboxCiaza)
        checkboxChorobySerca = findViewById(R.id.checkboxChorobySerca)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val buttonZapiszZmiany: View = findViewById(R.id.buttonZapiszZmiany)
        buttonZapiszZmiany.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val selectedHealthConditions = mutableListOf<String>()

        if (checkboxCukrzyca.isChecked) {
            selectedHealthConditions.add("Cukrzyca")
        }

        if (checkboxAstma.isChecked) {
            selectedHealthConditions.add("Astma")
        }

        if (checkboxCiaza.isChecked) {
            selectedHealthConditions.add("Ciąża")
        }

        if (checkboxChorobySerca.isChecked) {
            selectedHealthConditions.add("Choroby serca")
        }
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val userDiseasesRef = firestore.collection("diseases").document(userId)

            userDiseasesRef.set(mapOf("conditions" to selectedHealthConditions))
                .addOnSuccessListener {
                    Toast.makeText(this, "Zmiany zostały zapisane.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Błąd podczas zapisywania zmian.", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "Błąd: Brak zalogowanego użytkownika.", Toast.LENGTH_SHORT).show()
        }
    }
}
