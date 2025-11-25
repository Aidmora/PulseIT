package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // --- Lógica de Navegación ---

        val btnInvitado = findViewById<TextView>(R.id.btnInvitado)
        btnInvitado.setOnClickListener {
            val intent = Intent(this, EleccionTemaActivity::class.java)
            startActivity(intent)
        }

        val tvRegistrarse = findViewById<TextView>(R.id.tvRegistrarse)
        tvRegistrarse.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        btnLogin.setOnClickListener {

            val intent = Intent(this, EleccionTemaActivity::class.java)

            // Limpiamos el historial para que el usuario no pueda volver al login con el botón "atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
