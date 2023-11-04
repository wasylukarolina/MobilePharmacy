package com.example.mobilepharmacy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class AfterLoginActivity : AppCompatActivity() {

    lateinit var calendarView: CalendarView
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlogin)

        val drawerLayout: DrawerLayout = findViewById(R.id.calendar)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // boczne menu
        toggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                supportActionBar?.title = "Menu"
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                supportActionBar?.title = "After Login"
            }
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, AfterLoginActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_account -> {
                    // Obsługa kliknięcia przycisku "Account"
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    // Obsługa kliknięcia przycisku "Settings"
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
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


//        val addDrugIcon = findViewById<ImageView>(R.id.addDrugIcon)
//        addDrugIcon.setOnClickListener {
//            val intent = Intent(this, AddDrugActivity::class.java)
//            startActivity(intent)
//        }
//
//        val addHealthButton = findViewById<ImageView>(R.id.addHealthIcon)
//        addHealthButton.setOnClickListener {
//            val intent = Intent(this, AddHealthActivity::class.java)
//            startActivity(intent)
//        }

//        val updateHealthButton = findViewById<AppCompatButton>(R.id.updateHealth)
//        updateHealthButton.setOnClickListener {
//            val intent = Intent(this, DiseasesActivity::class.java)
//            startActivity(intent)
//        }

        val myDrugs = findViewById<AppCompatButton>(R.id.myDosage)
        myDrugs.setOnClickListener {
            val intent = Intent(this, MedicationsActivity::class.java)
            startActivity(intent)
        }

        val backButtonR = findViewById<ImageView>(R.id.backButtonR)
        backButtonR.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }
    }

    private fun logout() {
        // Usunięcie informacji o zalogowanym użytkowniku
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.remove("password")
        editor.remove("userID") // Usunięcie ID użytkownika
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