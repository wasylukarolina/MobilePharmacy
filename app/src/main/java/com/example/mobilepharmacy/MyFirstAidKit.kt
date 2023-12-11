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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// lista z lekami, które posiadamy
class MyFirstAidKit : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var medicationRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_first_aid_kit)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        medicationRecyclerView = findViewById(R.id.medicationRecyclerViewMyFirsAidKit)

        medicationRecyclerView.layoutManager = LinearLayoutManager(this)
        medicationRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        medicationRecyclerView.adapter = MedicationsAdapter()

        // Pobierz dane z Firestore i ustaw RecyclerView
        val email = firebaseAuth.currentUser?.email
        if (email != null) {
            val userMedicationsRef = firestore.collection("leki")
                .whereEqualTo("email", email)

            userMedicationsRef.get()
                .addOnSuccessListener { querySnapshot ->

                    val medicationsList = mutableListOf<String>()

                    for (document in querySnapshot.documents) {
                        val medicationName = document.getString("nazwaProduktu")
                        val expirationDate = document.get("dataWaznosci")
                        val amount = document.get("pojemnosc")
                        val amountNumber = amount.toString().toDoubleOrNull()?:0.0

                        if (medicationName != null && expirationDate != null && amountNumber != 0.0) {
                            val medicationInfo =
                                "$medicationName \nData ważności: $expirationDate \nPojemność: $amount"
                            medicationsList.add(medicationInfo)
                        }
                    }

                    val medicationsAdapter = medicationRecyclerView.adapter as? MyFirstAidKit.MedicationsAdapter
                    medicationsAdapter?.setMedicationsList(medicationsList)

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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_medicamation_myfirstaidkit, parent, false)
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
                itemView.findViewById(R.id.medicationNameTextViewMyFirstAidKit)
            private val expirationDateTextView: TextView =
                itemView.findViewById(R.id.expiryDateTextViewMyFirstAidKit)
            private val medicationCapacityTextView: TextView =
                itemView.findViewById(R.id.capacityTextViewMyFirstAidKit)
            private val deleteButton: Button = itemView.findViewById(R.id.deleteButtonMyFirsAidKit)

            init {
                deleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val deletedMedication = medicationsList[position]
                        deleteMedicationFromDB(deletedMedication)
                        medicationsList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }

            fun bind(medicationInfo: String) {
                val parts = medicationInfo.split("\n")
                var medicationName = parts[0]
                var medicationDate = parts[1]
                val medicationCapacity = parts[2]

                medicationNameTextView.text = medicationName
                expirationDateTextView.text = medicationDate
                medicationCapacityTextView.text = medicationCapacity
            }
        }

        private fun deleteMedicationFromDB(medication: String) {
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

                            // Usuń cały dokument
                            lekDocument.reference.delete()
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