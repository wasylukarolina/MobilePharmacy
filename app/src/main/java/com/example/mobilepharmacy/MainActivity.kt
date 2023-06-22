package com.example.mobilepharmacy

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.mobilepharmacy.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        if (email != null && password != null) {
            // Przekierowanie do AfterLoginActivity, jeśli użytkownik jest zalogowany
            val intent = Intent(this, AfterLoginActivity::class.java)
            startActivity(intent)
            finish()
                } else {
            setContentView(R.layout.activity_main)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ustawienie zdjęcia
        binding.imgStartViewImage.setImageResource(R.drawable.logo)

        //Rejestracja
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Logowanie
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

}
