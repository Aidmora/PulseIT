package com.mora.ariel.pulseit

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class JuegoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        val btnCorrecto = findViewById<MaterialButton>(R.id.btnFeedback)

        btnCorrecto.setOnClickListener {
            val intent = Intent(this, ResultadosActivity::class.java)
            startActivity(intent)
        }
    }
}