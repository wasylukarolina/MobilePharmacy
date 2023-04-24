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

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val leftIcon: ImageView = findViewById(R.id.backButton)

        leftIcon.setOnClickListener{
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }



}