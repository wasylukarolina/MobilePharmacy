package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.mobilepharmacy.databinding.ActivityAccountBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// strona, która pokazuje imię i nazwisko oraz ikonkę
class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var userInfo: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfo = binding.accountActivityFirstAndLastName
        firestore = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance()

        val userId = user.currentUser?.uid
        val userEmail = user.currentUser?.email

        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val firstName = documentSnapshot.getString("firstName")
                        val lastName = documentSnapshot.getString("lastName")

                        val fullName = "$firstName $lastName"
                        userInfo.text = fullName
                    }
                }
                .addOnFailureListener { exception ->
                    // Obsłuż błąd odczytu danych
                }
        }

        if (userEmail != null) {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                val displayName = account.displayName
                userInfo.text = displayName
                          }
        }
    }
}
