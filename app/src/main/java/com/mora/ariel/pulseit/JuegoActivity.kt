package com.mora.ariel.pulseit

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Importamos las constantes de tu archivo Constants.kt
import com.mora.ariel.pulseit.EXTRA_THEME
import com.mora.ariel.pulseit.EXTRA_DIFFICULTY
import com.mora.ariel.pulseit.EXTRA_LANGUAGE

class JuegoActivity : AppCompatActivity() {

    // --- Views ---
    private lateinit var cells: List<FrameLayout>
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvPlayerName: TextView

    // --- Game State ---
    private var score = 0
    private var level = 0
    private val gameSequence = mutableListOf<Int>()
    private var userStep = 0

    // --- Game Parameters ---
    private var theme: String? = null
    private var difficulty: String? = null
    private var sequenceDelay = 600L

    companion object {
        const val EXTRA_SCORE = "extra_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        // --- Recibir Extras ---
        theme = intent.getStringExtra(EXTRA_THEME)
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY)
        // val language = intent.getStringExtra(EXTRA_LANGUAGE) // Reservado para futuro uso

        // Configurar velocidad según dificultad
        sequenceDelay = when (difficulty) {
            "easy" -> 1000L   // Lento
            "hard" -> 300L    // Rápido
            else -> 600L      // Medium (Normal)
        }

        // --- Inicializar Vistas ---
        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvPlayerName = findViewById(R.id.tvPlayerName)

        // Mapeamos las celdas del 0 al 8
        cells = listOf(
            findViewById(R.id.cell_0),
            findViewById(R.id.cell_1),
            findViewById(R.id.cell_2),
            findViewById(R.id.cell_3),
            findViewById(R.id.cell_4),
            findViewById(R.id.cell_5),
            findViewById(R.id.cell_6),
            findViewById(R.id.cell_7),
            findViewById(R.id.cell_8)
        )

        // --- Configurar Tablero y Listeners ---
        setupBoard()
        setupClickListeners()

        // --- Iniciar Juego ---
        lifecycleScope.launch {
            delay(1000) // Espera inicial para que el usuario se prepare
            startLevel()
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        cells.forEach { cell ->
            cell.isEnabled = enabled
        }
    }

    private fun setupBoard() {
        tvScore.text = score.toString()
        tvLevel.text = "Nivel $level"
        // Aquí podrías poner el nombre del usuario si lo trajeras de Login

        when (theme) {
            "animals" -> setupAnimalsTheme()
            "colors" -> setupColorsTheme()
            "numbers" -> setupNumbersTheme()
            else -> setupColorsTheme() // Por defecto
        }
    }

    private fun setupAnimalsTheme() {
        // ⚠️ ATENCIÓN: Asegúrate de que estos nombres coincidan con tus archivos en drawable
        // Rellena esta lista con tus 9 imágenes.
        val animalResources = listOf(
            R.drawable.animal_tortuga, // Reemplazar
            R.drawable.animal_leon,    // Reemplazar
            R.drawable.animal_elefante,// Reemplazar
            R.drawable.animal_pato,    // Reemplazar
            R.drawable.animal_perro,   // Reemplazar
            R.drawable.animal_perezoso,// Reemplazar
            R.drawable.animal_pinguino,// Reemplazar
            R.drawable.animal_zorro,   // Reemplazar
            R.drawable.animal_abeja    // Reemplazar
        )

        // Si tienes menos de 9 imagenes, el código fallará. Asegurate de tener 9.
        if (animalResources.size < 9) return

        cells.forEachIndexed { index, cell ->
            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(animalResources[index])
                scaleType = ImageView.ScaleType.FIT_CENTER // Para que se vea el animal entero
            }
            cell.removeAllViews() // Limpiamos por si acaso
            cell.addView(imageView)
        }
    }

    private fun setupColorsTheme() {
        val colorList = listOf(
            Color.parseColor("#EF5350"), // Rojo
            Color.parseColor("#42A5F5"), // Azul
            Color.parseColor("#66BB6A"), // Verde
            Color.parseColor("#FFEE58"), // Amarillo
            Color.parseColor("#FFA726"), // Naranja
            Color.parseColor("#AB47BC"), // Morado
            Color.parseColor("#26A69A"), // Turquesa
            Color.parseColor("#EC407A"), // Rosa
            Color.parseColor("#78909C")  // Gris Azulado
        )
        cells.forEachIndexed { index, cell ->
            cell.setBackgroundColor(colorList[index])
        }
    }

    private fun setupNumbersTheme() {
        cells.forEachIndexed { index, cell ->
            val textView = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                text = (index + 1).toString()
                textSize = 40f
                setTextColor(Color.WHITE) // O Negro, dependiendo de tu fondo
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cell.removeAllViews()
            cell.addView(textView)
        }
    }

    private fun setupClickListeners() {
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener {
                handleCellClick(index)
            }
        }
    }

    private fun startLevel() {
        setInputsEnabled(false)
        tvPlayerName.text = "Observa..."
        userStep = 0
        level++
        tvLevel.text = "Nivel $level"

        // Agregamos un paso nuevo a la secuencia
        gameSequence.add((0..8).random())

        showSequence()
    }

    private fun showSequence() {
        lifecycleScope.launch {
            delay(500)
            for (index in gameSequence) {
                highlightCell(index, sequenceDelay / 2) // Ilumina la mitad del tiempo de espera
                delay(sequenceDelay) // Espera antes de la siguiente
            }
            setInputsEnabled(true) // Turno del usuario
            tvPlayerName.text = "¡Tu Turno!"
        }
    }

    private fun handleCellClick(index: Int) {
        // Feedback visual al tocar (rápido, 150ms)
        highlightCell(index, 150)

        if (index == gameSequence[userStep]) {
            // Acierto
            userStep++
            if (userStep == gameSequence.size) {
                // Nivel Completado
                score += 100 // Sumamos 100 puntos por nivel
                tvScore.text = score.toString()
                setInputsEnabled(false)
                lifecycleScope.launch {
                    delay(1000) // Espera de celebración
                    startLevel()
                }
            }
        } else {
            // Fallo -> Game Over
            setInputsEnabled(false)
            endGame()
        }
    }

    private fun highlightCell(index: Int, duration: Long) {
        val cell = cells[index]
        // Animación de Alpha (parpadeo)
        ObjectAnimator.ofFloat(cell, "alpha", 1f, 0.3f, 1f).apply {
            this.duration = duration
            start()
        }
    }

    private fun endGame() {
        setInputsEnabled(false)
        val intent = Intent(this, ResultadosActivity::class.java).apply {
            putExtra(EXTRA_SCORE, score)
        }
        startActivity(intent)
        finish() // Cierra la actividad para que no se pueda volver atrás
    }
}
