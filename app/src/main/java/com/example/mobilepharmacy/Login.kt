package com.example.mobilepharmacy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.mobilepharmacy.databinding.ActivityLogin2Binding
import com.example.mobilepharmacy.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLogin2Binding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val haslo = binding.editTextPassword.text.toString()


            if (email.isNotEmpty() && haslo.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, haslo).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val leftIcon: ImageView = findViewById(R.id.backButton)

        leftIcon.setOnClickListener{
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }



}