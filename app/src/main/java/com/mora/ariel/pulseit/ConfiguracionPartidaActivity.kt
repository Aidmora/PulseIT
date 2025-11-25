package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionPartidaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion_partida) // Conecta con el archivo XML

        val btnEmpezarJuego = findViewById<LinearLayout>(R.id.btnEmpezarPartida)
        btnEmpezarJuego.setOnClickListener {
            val intent = Intent(this, JuegoActivity::class.java)
            startActivity(intent)
        }

        val btnVolverModos = findViewById<LinearLayout>(R.id.btnEmpezarPartida2)
        btnVolverModos.setOnClickListener {
            finish()
        }


    }
}
