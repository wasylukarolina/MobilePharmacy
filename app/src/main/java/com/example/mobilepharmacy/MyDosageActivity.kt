package com.example.mobilepharmacy

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyDosageActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_dosage)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val medicationsRecyclerView: RecyclerView = findViewById(R.id.medicationsRecyclerView)
        medicationsRecyclerView.layoutManager = LinearLayoutManager(this)
        medicationsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        medicationsRecyclerView.adapter = MedicationsAdapter()

        val email = firebaseAuth.currentUser?.email

        if (email != null) {
            val userMedicationsRef = firestore.collection("leki").whereEqualTo("email", email)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("nazwaProduktu")
                        val medicationDose = document.get("dawkowanie") as? List<*>
                        val expirationDate = document.get("dataWaznosci")

                        if (medicationName != null && medicationDose != null) {
                            val medicationInfo =
                                "$medicationName \nData ważności: $expirationDate \nDawkowanie: ${medicationDose.joinToString(", ")} "
                            medicationsList.add(medicationInfo)
                        }
                    }

                    val medicationsAdapter = medicationsRecyclerView.adapter as MedicationsAdapter
                    medicationsAdapter.setMedicationsList(medicationsList)
                }
                .addOnFailureListener { _ ->
                    // Handle failure
                }
        }
    }

    inner class MedicationsAdapter :
        RecyclerView.Adapter<MedicationsAdapter.MedicationViewHolder>() {

        private val medicationsList = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
            return MedicationViewHolder(view)
        }

        override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
            val medicationInfo = medicationsList[position]
            holder.bind(medicationInfo)
        }

        override fun getItemCount(): Int {
            return medicationsList.size
        }

        inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val medicationNameTextView: TextView =
                itemView.findViewById(R.id.medicationNameTextView)
            private val expirationDateTextView: TextView =
                itemView.findViewById(R.id.dateTextView)
            private val medicationDoseLayout: LinearLayout =
                itemView.findViewById(R.id.medicationDoseLayout)
            private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

            init {
                deleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val deletedMedication = medicationsList[position]
                        updateMedicationFromDatabase(deletedMedication)
                        medicationsList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }

            fun bind(medicationInfo: String) {
                val parts = medicationInfo.split("\n")
                val medicationName = parts[0]
                val medicationDate = parts[1]
                val medicationDoseList = parts[2].split(", ")


                medicationNameTextView.text = medicationName
                expirationDateTextView.text = medicationDate

                // Clear existing views from medicationDoseLayout
                medicationDoseLayout.removeAllViews()

                // Add CheckBox for each medication dose
                for (dose in medicationDoseList) {
                    val checkBox = CheckBox(itemView.context)
                    checkBox.text = dose
                    checkBox.setTextColor(itemView.context.resources.getColor(android.R.color.black))

                    // Set black color for the checkbox border
                    val checkBoxButtonDrawable = CompoundButtonCompat.getButtonDrawable(checkBox)
                    if (checkBoxButtonDrawable != null) {
                        DrawableCompat.setTint(checkBoxButtonDrawable, Color.BLACK)
                    }

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            checkBox.paintFlags = checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            decreaseMedicationCount(medicationName) // Zmniejsz liczbę leków w bazie
                            checkBox.isEnabled = false // Wyłącz checkbox, aby uniemożliwić odznaczenie

                            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                            val currentDateAndTime: String = dateFormat.format(Date())

                            // Podziel datę i czas
                            val parts = currentDateAndTime.split(" ")
                            val currentDate = parts[0]
                            val currentTime = parts[1]

                            // Dodaj datę i czas zaznaczenia checkboxa do bazy danych
                            addCheckboxCheckedDateToDatabase(medicationName, currentDate, currentTime)
                        }
                    }
                    medicationDoseLayout.addView(checkBox)

                }
            }

            private fun addCheckboxCheckedDateToDatabase(medicationName: String, currentDate: String, currentTime:String) {
                val email = firebaseAuth.currentUser?.email
                if (email != null) {
                    val medicationCheckedDateData = hashMapOf(
                        "medicationName" to medicationName,
                        "checkedTime" to currentTime,
                        "checkedDate" to currentDate,
                        "email" to email
                    )

                    firestore.collection("checkedMedications")
                        .add(medicationCheckedDateData)
                        .addOnSuccessListener {
                            // Dodanie daty zaznaczenia checkboxa do bazy danych powiodło się
                        }
                        .addOnFailureListener { _ ->
                            // Handle failure
                        }
                }
            }

            private fun decreaseMedicationCount(medicationName: String) {
                val email = firebaseAuth.currentUser?.email
                if (email != null) {
                    firestore.collection("leki")
                        .whereEqualTo("email", email)
                        .whereEqualTo("nazwaProduktu", medicationName)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                val medicationCountString = document.getString("ilosc tabletek")
                                var medicationCount = medicationCountString?.toLongOrNull() ?: 0

                                if (medicationCount > 0) {
                                    medicationCount -= 1

                                    document.reference.update(
                                        "ilosc tabletek",
                                        medicationCount.toString()
                                    )
                                        .addOnSuccessListener {
                                            // Aktualizacja liczby leków w bazie powiodła się
                                        }
                                        .addOnFailureListener { _ ->
                                            // Handle failure
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { _ ->
                            // Handle failure
                        }
                }
            }
        }

        private fun updateMedicationFromDatabase(medication: String) {
            val email = firebaseAuth.currentUser?.email
            val name = medication.split("\n")[0].toString().trim()
            val date = medication.split("\n")[1].toString().replace("Data ważności: ", "").trim()
            if (email != null) {
                firestore.collection("leki")
                    .whereEqualTo("email", email)
                    .whereEqualTo("nazwaProduktu", name)
                    .whereEqualTo("dataWaznosci", date)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // Znaleziono lek, zakładamy że jest tylko jeden pasujący
                            val lekDocument = querySnapshot.documents[0]

                            // Przygotuj dane do aktualizacji
                            val dataToUpdate = hashMapOf<String, Any>()
                            dataToUpdate["dawkowanie"] = emptyList<String>()
                            lekDocument.reference.update(dataToUpdate)
                            lekDocument.reference.update("dawkowanie", FieldValue.delete())
                        }
                    }
                    .addOnFailureListener { _ ->
                        // Handle failure
                    }
            }
        }



        fun setMedicationsList(medications: List<String>) {
            medicationsList.clear()
            medicationsList.addAll(medications)
            notifyDataSetChanged()
        }
    }
}
