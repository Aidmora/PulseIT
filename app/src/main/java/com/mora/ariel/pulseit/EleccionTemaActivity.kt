package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class EleccionTemaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eleccion_tema)


        val cardAnimales = findViewById<LinearLayout>(R.id.cardAnimales)
        cardAnimales.setOnClickListener {
            val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
            startActivity(intent)
        }

        val btnVolver = findViewById<LinearLayout>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
