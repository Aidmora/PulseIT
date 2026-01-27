package com.mora.ariel.pulseit

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionPartidaActivity : AppCompatActivity() {

    private var selectedDifficulty: String? = null
    private var selectedLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion_partida)

        val theme = intent.getStringExtra(EXTRA_THEME) ?: return

        val btnEasy = findViewById<ImageView>(R.id.btnEasy)
        val btnMedium = findViewById<ImageView>(R.id.btnMedium)
        val btnHard = findViewById<ImageView>(R.id.btnHard)

        val btnEN = findViewById<ImageView>(R.id.btnEN)
        val btnES = findViewById<ImageView>(R.id.btnES)

        val btnEmpezarJuego = findViewById<ImageView>(R.id.btnEmpezarJuego)

        val difficultyButtons = listOf(btnEasy, btnMedium, btnHard)
        val languageButtons = listOf(btnES, btnEN)

        btnEasy.setOnClickListener {
            selectOption(btnEasy, difficultyButtons)
            selectedDifficulty = "easy"
        }

        btnMedium.setOnClickListener {
            selectOption(btnMedium, difficultyButtons)
            selectedDifficulty = "medium"
        }

        btnHard.setOnClickListener {
            selectOption(btnHard, difficultyButtons)
            selectedDifficulty = "hard"
        }

        btnEmpezarJuego.setOnClickListener {
            if (selectedDifficulty != null && selectedLanguage != null) {
                val intent = Intent(this, JuegoActivity::class.java).apply {
                    putExtra(EXTRA_THEME, theme)
                    putExtra(EXTRA_DIFFICULTY, selectedDifficulty)
                    putExtra(EXTRA_LANGUAGE, selectedLanguage)
                }
                startActivity(intent)
            }else{
                Toast.makeText(this, "Selecciona dificultad e idioma", Toast.LENGTH_SHORT).show()
            }
        }

        btnES.setOnClickListener {
            selectOption(btnES, languageButtons)
            selectedLanguage = "es"
        }

        btnEN.setOnClickListener {
            selectOption(btnEN, languageButtons)
            selectedLanguage = "en"
        }
    }

    private fun selectOption(selected: ImageView, group: List<ImageView>) {
        group.forEach { it.isSelected = false }
        selected.isSelected = true
    }
}
