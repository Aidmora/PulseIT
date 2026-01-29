package com.mora.ariel.pulseit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class EleccionTemaActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eleccion_tema)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val colors = findViewById<ImageView>(R.id.imgColors)
        val animals = findViewById<ImageView>(R.id.imgAnimals)
        val numbers = findViewById<ImageView>(R.id.imgNumbers)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d("AuthDebug", "Usuario: ${currentUser?.email}, ¿Es Anónimo?: ${currentUser?.isAnonymous}")

        if (currentUser != null && !currentUser.isAnonymous) {
            btnLogout.text = "Cerrar Sesión"
            btnLogout.visibility = View.VISIBLE
        } else {
            btnLogout.text = getString(R.string.login)
            btnLogout.visibility = View.VISIBLE
        }
        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        colors.setOnClickListener {
            openNextScreen(getString(R.string.theme_colors))
        }

        animals.setOnClickListener {
            openNextScreen(getString(R.string.theme_animals))
        }

        numbers.setOnClickListener {
            openNextScreen(getString(R.string.theme_numbers))
        }
    }

    private fun openNextScreen(theme: String) {
        val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
        intent.putExtra(EXTRA_THEME, theme)
        startActivity(intent)
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("force_login", true)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}