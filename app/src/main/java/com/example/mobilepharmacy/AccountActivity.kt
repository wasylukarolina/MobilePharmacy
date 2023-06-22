package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.mobilepharmacy.databinding.ActivityAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var firstAndLastName: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firstAndLastName = binding.accountActivityFirstAndLastName

        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()

        // Pobierz ID aktualnie zalogowanego użytkownika
        val userId = user.currentUser?.uid

        // Pobierz imię i nazwisko użytkownika z bazy Firestore
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val firstName = documentSnapshot.getString("firstName")
                        val lastName = documentSnapshot.getString("lastName")

                        // Wyświetl imię i nazwisko w TextView
                        val fullName = "$firstName $lastName"
                        firstAndLastName.text = fullName
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd odczytu danych
                }
        }
    }
}
