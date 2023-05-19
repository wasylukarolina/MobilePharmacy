package com.example.mobilepharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast

class AddHealthActivity : AppCompatActivity() {

    private lateinit var checkboxCukrzyca: CheckBox
    private lateinit var checkboxAstma: CheckBox
    private lateinit var checkboxCiaza: CheckBox
    private lateinit var checkboxChorobySerca: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_health)

        checkboxCukrzyca = findViewById(R.id.checkboxCukrzyca)
        checkboxAstma = findViewById(R.id.checkboxAstma)
        checkboxCiaza = findViewById(R.id.checkboxCiaza)
        checkboxChorobySerca = findViewById(R.id.checkboxChorobySerca)

        val buttonZapiszZmiany: View = findViewById(R.id.buttonZapiszZmiany)
        buttonZapiszZmiany.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val selectedHealthConditions = mutableListOf<String>()

        if (checkboxCukrzyca.isChecked) {
            selectedHealthConditions.add("Cukrzyca")
        }

        if (checkboxAstma.isChecked) {
            selectedHealthConditions.add("Astma")
        }

        if (checkboxCiaza.isChecked) {
            selectedHealthConditions.add("Ciąża")
        }

        if (checkboxChorobySerca.isChecked) {
            selectedHealthConditions.add("Choroby serca")
        }

        // Przykład wyświetlenia zaznaczonych stanów zdrowia
        val selectedHealthConditionsText = selectedHealthConditions.joinToString(", ")
        Toast.makeText(this, "Zaznaczone stany zdrowia: $selectedHealthConditionsText", Toast.LENGTH_SHORT).show()
    }
}
