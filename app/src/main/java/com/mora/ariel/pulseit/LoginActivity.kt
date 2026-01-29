package com.mora.ariel.pulseit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMain()
            return
        }
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGuess = findViewById<Button>(R.id.btnGuess)

        btnLogin.setOnClickListener { signInWithGoogle() }
        btnGuess.setOnClickListener { signInAsGuest() }

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
                    // Llamamos a guardar y dejamos que esa función maneje el cambio de pantalla
                    saveUserToFirestore(user)
                } else {
                    Log.e("AUTH", "Error en login anónimo", task.exception)
                    Toast.makeText(this, "Error de red al entrar como invitado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser?) {
        if (user == null) return

        val userRef = db.collection("users").document(user.uid)

        val userData = hashMapOf(
            "uid" to user.uid,
            "nombre" to (user.displayName ?: "Invitado"),
            "email" to (user.email ?: "N/A"),
            "fotoUrl" to (user.photoUrl?.toString() ?: ""),
            "tipoCuenta" to if (user.isAnonymous) "invitado" else "google",
            "fechaRegistro" to com.google.firebase.Timestamp.now()
        )
        userRef.set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Datos guardados correctamente")
                goToMain()
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al guardar datos: ${e.message}")
                // Si falla por internet, igual permitimos entrar (opcional)
                goToMain()
            }
    }

    private fun goToMain() {
        val intent = Intent(this, EleccionTemaActivity::class.java)
        startActivity(intent)
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
                    Log.e("GOOGLE_AUTH", "Error de Google", e)
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
                    saveUserToFirestore(auth.currentUser)
                } else {
                    Log.e("AUTH", "Error con credenciales de Google")
                }
            }
    }
}