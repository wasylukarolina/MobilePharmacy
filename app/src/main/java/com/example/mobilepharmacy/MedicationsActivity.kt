package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicationsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medications)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val medicationsTextView: TextView = findViewById(R.id.medicationsTextView)

        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val userMedicationsRef = firestore.collection("leki").whereEqualTo("userId", userId)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("nazwaProduktu")

                        if (medicationName != null) {
                            medicationsList.add(medicationName)
                        }
                    }

                    if (medicationsList.isNotEmpty()) {
                        val medicationsText = medicationsList.joinToString("\n")
                        medicationsTextView.text = medicationsText
                    } else {
                        medicationsTextView.text = "Brak danych na temat leków."
                    }
                }
                .addOnFailureListener { exception ->
                    medicationsTextView.text = "Błąd pobierania danych."
                }
        } else {
            medicationsTextView.text = "Błąd: Brak zalogowanego użytkownika."
        }
    }
}
