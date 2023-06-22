package com.example.mobilepharmacy

import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicationsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medications)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val medicationsRecyclerView: RecyclerView = findViewById(R.id.medicationsRecyclerView)
        medicationsRecyclerView.layoutManager = LinearLayoutManager(this)
        medicationsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        medicationsRecyclerView.adapter = MedicationsAdapter()

        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            val userMedicationsRef = firestore.collection("leki").whereEqualTo("userId", userId)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("nazwaProduktu")
                        val medicationDose = document.get("dawkowanie") as? List<String>

                        if (medicationName != null && medicationDose != null) {
                            val medicationInfo = "$medicationName\nDawkowanie: ${medicationDose.joinToString(", ")}"
                            medicationsList.add(medicationInfo)
                        }
                    }

                    val medicationsAdapter = medicationsRecyclerView.adapter as MedicationsAdapter
                    medicationsAdapter.setMedicationsList(medicationsList)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    inner class MedicationsAdapter : RecyclerView.Adapter<MedicationsAdapter.MedicationViewHolder>() {

        private val medicationsList = mutableListOf<String>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
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
            private val medicationNameTextView: TextView = itemView.findViewById(R.id.medicationNameTextView)
            private val medicationDoseLayout: LinearLayout = itemView.findViewById(R.id.medicationDoseLayout)
            private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

            init {
                deleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val deletedMedication = medicationsList[position]
                        deleteMedicationFromDatabase(deletedMedication)
                        medicationsList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }

            fun bind(medicationInfo: String) {
                val parts = medicationInfo.split("\n")
                val medicationName = parts[0]
                val medicationDoseList = parts[1].split(", ")

                medicationNameTextView.text = medicationName

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
                        } else {
                            checkBox.paintFlags = checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }
                    medicationDoseLayout.addView(checkBox)
                }
            }
        }



        private fun deleteMedicationFromDatabase(medication: String) {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                firestore.collection("leki")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("nazwaProduktu", medication.split("\n")[0])
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                        }
                    }
                    .addOnFailureListener { exception ->
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

