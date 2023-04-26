package com.example.mobilepharmacy

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.example.mobilepharmacy.databinding.ActivityRegisterBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()


        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val imie = binding.editTextFirstName.text.toString()
            val nazwisko = binding.editTextLastName.text.toString()
            val haslo = binding.editTextPassword.text.toString()
            val hasloRepeat = binding.editTextConfirmPassword.text.toString()

            if (email.isNotEmpty() && imie.isNotEmpty() && nazwisko.isNotEmpty() && haslo.isNotEmpty() && hasloRepeat.isNotEmpty()) {
                if (haslo == hasloRepeat) {
                    firebaseAuth.createUserWithEmailAndPassword(email, haslo).addOnCompleteListener {
                            if (it.isSuccessful) {
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Hasła różnią się", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }


        val leftIcon: ImageView = findViewById(R.id.backButtonR)

        leftIcon.setOnClickListener{
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}