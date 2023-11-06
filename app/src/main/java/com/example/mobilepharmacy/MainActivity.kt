package com.example.mobilepharmacy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilepharmacy.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var firstName: String
    private lateinit var lastName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Inicjalizacja Firestore
        firestoreDB = FirebaseFirestore.getInstance()

        // Ustawienie zdjęcia
        binding.imgStartViewImage.setImageResource(R.drawable.logo)

        binding.loginButton.setOnClickListener {
            // Logowanie zwykłe
            startActivity(Intent(this, Login::class.java))
        }

        binding.registerButton.setOnClickListener {
            // Rejestracja
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Inicjalizacja opcji logowania Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Inicjalizacja klienta logowania Google
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginButtonGoogle.setOnClickListener {
            // Logowanie z Google
            signInWithGoogle()
        }

        binding.loginButtonFacebook.setOnClickListener {
            // Logowanie z Facebookiem
        }
    }

    // Dodaj funkcję do obsługi logowania z Google
    private fun signInWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Pomyślnie zalogowano
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Logowanie nie powiodło się
                Toast.makeText(this, "Logowanie przez Google nie powiodło się", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Logowanie zakończone sukcesem
                    val user = firebaseAuth.currentUser
                    val email = user?.email.toString()
                    Toast.makeText(this, "Zalogowano przez Google jako ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    // Sprawdź, czy użytkownik jest zalogowany za pomocą Firebase Authentication
                    if (user != null) {
                        // Użytkownik jest zalogowany, więc możesz sprawdzić bazę danych
                        checkIfUserExistsAndSignIn(user)
                    } else {
                        Toast.makeText(this, "Błąd logowania.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Logowanie nie powiodło się
                    Toast.makeText(this, "Logowanie przez Google nie powiodło się", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Sprawdzenie czy użytkownik googla już jest w bazie
    private fun checkIfUserExistsAndSignIn(user: FirebaseUser) {
        val usersCollection = firestoreDB.collection("users")

        usersCollection.whereEqualTo("email", user.email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Użytkownik o danym emailu istnieje w bazie
                    val userDocument = querySnapshot.documents[0] // Zakładam, że jest tylko jeden użytkownik z danym emailem
                    Toast.makeText(this, "$userDocument", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AfterLoginActivity::class.java))

                } else {
                    // Użytkownik o danym emailu nie istnieje w bazie, tworzymy nowy rekord
                    createNewUser(user)
                }
            }
            .addOnFailureListener { e ->
                // Błąd podczas sprawdzania, co zrobić zależy od Twoich potrzeb
                Toast.makeText(this, "Błąd podczas sprawdzania użytkownika: $e", Toast.LENGTH_SHORT).show()
            }
    }

    // stworzenie nowego rekordu do bazy z użytkownikiem googla
    private fun createNewUser(user: FirebaseUser) {
        // podzielenie nazwy użytkownika na imię i nazwisko
        val displayName = user.displayName
        if (!displayName.isNullOrBlank()) {
            val nameParts = displayName.split(" ") // Rozdziel na podstawie spacji (możesz użyć innego znaku)
            if (nameParts.size >= 2) {
                firstName = nameParts[0]
                lastName = nameParts.subList(1, nameParts.size).joinToString(" ")
            }else if (nameParts.size ==1)            {
                firstName = nameParts[0]
            }
        }
        // Tworzenie nowego rekordu użytkownika
        val userData = hashMapOf(
            "email" to user.email,
            "userID" to user.uid,
            "firstName" to firstName,
            "lastName" to lastName
        )

        val userDocument = firestoreDB.collection("users").document() // Tworzy nowy dokument z unikalnym identyfikatorem
        userDocument.set(userData)
            .addOnSuccessListener {
                // Rekord użytkownika został utworzony pomyślnie
                // Przechodzimy dalej
                startActivity(Intent(this, AfterLoginActivity::class.java))
            }
            .addOnFailureListener { e ->
                // Błąd podczas tworzenia rekordu użytkownika
                Toast.makeText(this, "Błąd podczas tworzenia rekordu użytkownika: $e", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
