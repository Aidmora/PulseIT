package com.mora.ariel.pulseit

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionPartidaActivity : AppCompatActivity() {

    private var selectedDifficulty: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion_partida)
        val theme = intent.getStringExtra(EXTRA_THEME) ?: return
        val btnEasy = findViewById<ImageView>(R.id.btnEasy)
        val btnMedium = findViewById<ImageView>(R.id.btnMedium)
        val btnHard = findViewById<ImageView>(R.id.btnHard)
        val btnEmpezarJuego = findViewById<ImageView>(R.id.btnEmpezarJuego)

        val difficultyButtons = listOf(btnEasy, btnMedium, btnHard)

        btnEasy.setOnClickListener {
            selectOption(btnEasy, difficultyButtons)
            selectedDifficulty = getString(R.string.difficulty_easy)
        }

        btnMedium.setOnClickListener {
            selectOption(btnMedium, difficultyButtons)
            selectedDifficulty = getString(R.string.difficulty_medium)
        }

        btnHard.setOnClickListener {
            selectOption(btnHard, difficultyButtons)
            selectedDifficulty = getString(R.string.difficulty_hard)
        }

        // Lógica para iniciar el juego
        btnEmpezarJuego.setOnClickListener {
            if (selectedDifficulty != null) {
                val intent = Intent(this, JuegoActivity::class.java).apply {
                    putExtra(EXTRA_THEME, theme)
                    putExtra(EXTRA_DIFFICULTY, selectedDifficulty)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "¡Por favor, elige una dificultad!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectOption(selected: ImageView, group: List<ImageView>) {
        group.forEach { it.isSelected = false }
        selected.isSelected = true
    }
}