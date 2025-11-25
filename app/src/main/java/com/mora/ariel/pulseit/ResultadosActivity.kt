package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultadosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_resultados)

        // --- Lógica de navegación para los botones de resultados ---


        val btnPlayAgain = findViewById<MaterialButton>(R.id.btnPlayAgain)
        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, JuegoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

            finish()
        }

        val btnBackHome = findViewById<MaterialButton>(R.id.btnBackHome)
        btnBackHome.setOnClickListener {
            val intent = Intent(this, EleccionTemaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)

            finish()
        }

        val btnChangeMode = findViewById<MaterialButton>(R.id.btnChangeMode)
        btnChangeMode.setOnClickListener {
            // Creamos la intención de ir a la pantalla de Configuración de Partida
            val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
            // Limpiamos la pila hasta la pantalla de configuración
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            // Cerramos la pantalla actual de resultados
            finish()
        }
    }
}
