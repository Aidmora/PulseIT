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

        // Recuperamos el tema seleccionado de la pantalla anterior
        val theme = intent.getStringExtra(EXTRA_THEME) ?: return

        // Referencias a los componentes del Layout (Dificultad)
        val btnEasy = findViewById<ImageView>(R.id.btnEasy)
        val btnMedium = findViewById<ImageView>(R.id.btnMedium)
        val btnHard = findViewById<ImageView>(R.id.btnHard)
        val btnEmpezarJuego = findViewById<ImageView>(R.id.btnEmpezarJuego)

        val difficultyButtons = listOf(btnEasy, btnMedium, btnHard)

        // Listeners para la selección de dificultad
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

        // Lógica del botón para iniciar la partida
        btnEmpezarJuego.setOnClickListener {
            if (selectedDifficulty != null) {
                val intent = Intent(this, JuegoActivity::class.java).apply {
                    putExtra(EXTRA_THEME, theme)
                    putExtra(EXTRA_DIFFICULTY, selectedDifficulty)
                }
                startActivity(intent)
            } else {
                // Mensaje si no se ha elegido dificultad (Actualizado sin mención al idioma)
                Toast.makeText(this, "¡Selecciona una dificultad para jugar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Gestiona el estado visual de selección (isSelected) de los botones
     */
    private fun selectOption(selected: ImageView, group: List<ImageView>) {
        group.forEach { it.isSelected = false }
        selected.isSelected = true
    }
}