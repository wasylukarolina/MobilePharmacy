package com.example.mobilepharmacy

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


class AfterLoginActivity : AppCompatActivity() {

    lateinit var calendarView: CalendarView
    lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlogin)

        // Inicjalizacja pola calendarView
        calendarView = findViewById(R.id.calendarView)

        // Ustawienie listenera dla zmiany wybranej daty
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Tutaj można wpisać kod, który wykona się po zmianie wybranej daty
        }

        val drawerLayout : DrawerLayout = findViewById(R.id.calendar)
        val navigationView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout,   R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){

                R.id.nav_home -> Toast.makeText(applicationContext, "Kliknąłęś dom", Toast.LENGTH_SHORT).show()
                R.id.nav_account -> Toast.makeText(applicationContext, "Kliknąłęś konto", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext, "Kliknąłęś ustawienia", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> Toast.makeText(applicationContext, "Kliknąłęś logout", Toast.LENGTH_SHORT).show()


            }

            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}