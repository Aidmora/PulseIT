package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth

class EleccionTemaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eleccion_tema)
        val colors = findViewById<ImageView>(R.id.imgColors)
        val animals = findViewById<ImageView>(R.id.imgAnimals)
        val numbers = findViewById<ImageView>(R.id.imgNumbers)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            cerrarSesion()
        }

        colors.setOnClickListener {
            openNextScreen("animals")
        }

        animals.setOnClickListener {
            openNextScreen("colors")
        }

        numbers.setOnClickListener {
            openNextScreen("numbers")
        }


    }

    private fun openNextScreen(theme: String) {
        val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
        intent.putExtra(EXTRA_THEME, theme)
        startActivity(intent)
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}
