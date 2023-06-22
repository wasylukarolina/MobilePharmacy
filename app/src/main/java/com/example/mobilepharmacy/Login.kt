package com.example.mobilepharmacy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.example.mobilepharmacy.databinding.ActivityLogin2Binding
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLogin2Binding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val haslo = binding.editTextPassword.text.toString()


            if (email.isNotEmpty() && haslo.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, haslo).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", haslo)

                        // Pobieranie ID aktualnie zalogowanego użytkownika
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser
                        val userId = currentUser?.uid
                        editor.putString("userID", userId ?: "x") // Jeśli userId jest null, użyj pustego ciągu znaków

                        editor.apply()


                        val intent = Intent(this, AfterLoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val leftIcon: ImageView = findViewById(R.id.backButton)

        leftIcon.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Sprawdzenie, czy dane logowania zostały zapamiętane w SharedPreferences

    }
}