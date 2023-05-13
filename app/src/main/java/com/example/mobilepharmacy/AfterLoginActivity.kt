package com.example.mobilepharmacy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class AfterLoginActivity : AppCompatActivity() {

    lateinit var calendarView: CalendarView
    lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlogin)

        val drawerLayout : DrawerLayout = findViewById(R.id.calendar)
        val navigationView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,  drawerLayout,   R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Obsługa kliknięcia przycisku "Home"
                    true
                }
                R.id.nav_account -> {
                    // Obsługa kliknięcia przycisku "Account"
                    true
                }
                R.id.nav_settings -> {
                    // Obsługa kliknięcia przycisku "Settings"
                    true
                }
                R.id.nav_logout -> {
                    // Obsługa kliknięcia przycisku "Logout"
                    logout()
                    true
                }
                else -> false
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

//        val sharedPref = getSharedPreferences("myPrefs", MODE_PRIVATE)
//        val email = sharedPref.getString("email", null)
//        val haslo = sharedPref.getString("password", null)
//
//        if (email != null && haslo != null) {
//            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, haslo)
//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        // użytkownik zalogowany automatycznie
//                    }
//                }
//        }

        val addDrugIcon = findViewById<ImageView>(R.id.addDrugIcon)
        addDrugIcon.setOnClickListener {
            val intent = Intent(this, AddDrugActivity::class.java)
            startActivity(intent)
        }

    }

    private fun logout() {
        // Usunięcie informacji o zalogowanym użytkowniku
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.remove("password")
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // zakaz cofania
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = if (FirebaseAuth.getInstance().currentUser == null) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, Login::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }



}