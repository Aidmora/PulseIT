package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGuess = findViewById<Button>(R.id.btnGuess)
        btnLogin.setOnClickListener { signInWithGoogle() }
        btnGuess.setOnClickListener { signInAsGuest() }
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInAsGuest() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserToFirestore(user)

                    // user.uid exists
                    // user.isAnonymous == true

                    goToMain()
                } else {
                    task.exception?.printStackTrace()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser?) {
        if (user == null) return

        val userRef = db.collection("users").document(user.uid)
        //Campos a capturar
        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to (user.displayName ?: "Invitado"),
            "email" to (user.email ?: "N/A"),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "tipoCuenta" to if (user.isAnonymous) "invitado" else "google",
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )

             SetOptions.merge()  //Si el usuario ya existe
        // actualiza los datos de perfil pero no borra sus puntajes previos
        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener {
                goToMain()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                // Aunque falle el guardado en DB, podrías dejarlo pasar al juego
                goToMain()
            }
    }
    private fun goToMain() {
        startActivity(Intent(this, EleccionTemaActivity::class.java))
        finish()
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    fun signInWithGoogle() {
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserToFirestore(user)
                    // SUCCESS: user.email, user.uid, user.displayName
                } else {
                    // FAILURE
                }
            }
    }


}
