package com.mora.ariel.pulseit

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JuegoActivity : AppCompatActivity() {

    private lateinit var cells: List<FrameLayout>
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvPlayerName: TextView

    private var score = 0
    private var level = 0
    private val gameSequence = mutableListOf<Int>()
    private var userStep = 0

    private var theme: String? = null
    private var difficulty: String? = null
    private var sequenceDelay = 1000L

    companion object {
        const val EXTRA_SCORE = "extra_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        theme = intent.getStringExtra(EXTRA_THEME)
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY)

        // Tiempos más pausados para una experiencia más "suave"
        sequenceDelay = when (difficulty) {
            "easy" -> 1400L
            "hard" -> 800L
            else -> 1100L
        }

        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvPlayerName = findViewById(R.id.tvPlayerName)

        cells = listOf(
            findViewById(R.id.cell_0), findViewById(R.id.cell_1), findViewById(R.id.cell_2),
            findViewById(R.id.cell_3), findViewById(R.id.cell_4), findViewById(R.id.cell_5),
            findViewById(R.id.cell_6), findViewById(R.id.cell_7), findViewById(R.id.cell_8)
        )

        setupBoard()
        setupClickListeners()
        setInputsEnabled(false)

        lifecycleScope.launch {
            delay(1000)
            startLevel()
        }
    }

    private fun setupBoard() {
        tvScore.text = score.toString()
        tvLevel.text = "Nivel $level"

        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.alpha = 0f
            cell.background = null

            when (theme) {
                "animals" -> setupAnimalsInCell(index, cell)
                "numbers" -> setupNumbersInCell(index, cell)
                "colors" -> setupColorsInCell(index, cell)
            }
        }
    }

    private fun setupAnimalsInCell(index: Int, cell: FrameLayout) {
        val animals = listOf(
            R.drawable.animal_tortuga, R.drawable.animal_leon, R.drawable.animal_elefante,
            R.drawable.animal_pato, R.drawable.animal_perro, R.drawable.animal_perezoso,
            R.drawable.animal_pinguino, R.drawable.animal_zorro, R.drawable.animal_abeja
        )
        val img = ImageView(this).apply {
            setImageResource(animals[index % animals.size])
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(25, 25, 25, 25)
        }
        cell.addView(img)
    }

    private fun setupNumbersInCell(index: Int, cell: FrameLayout) {
        val txt = TextView(this).apply {
            text = (index + 1).toString()
            textSize = 42f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        cell.addView(txt)
    }

    private fun setupColorsInCell(index: Int, cell: FrameLayout) {
        // En colores el contorno verde se aplica igual sobre el FrameLayout
    }

    private fun setupClickListeners() {
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener { handleCellClick(index) }
        }
    }

    private fun startLevel() {
        setInputsEnabled(false)
        tvPlayerName.text = "¡Observa!"
        userStep = 0
        level++
        tvLevel.text = "Nivel $level"
        gameSequence.add((0..8).random())
        showSequence()
    }

    private fun showSequence() {
        lifecycleScope.launch {
            delay(800)
            for (index in gameSequence) {
                // Color blanco suave para el patrón de la máquina
                playGameAnimation(index, sequenceDelay - 200, Color.parseColor("#95FFFFFF"))
                delay(sequenceDelay)
            }
            setInputsEnabled(true)
            tvPlayerName.text = "¡Tu Turno!"
        }
    }

    private fun handleCellClick(index: Int) {
        if (index == gameSequence[userStep]) {
            // ACIERTO: Ahora tiene el mismo ritmo que el patrón (lento y claro)
            playGameAnimation(index, sequenceDelay - 200, Color.parseColor("#4CAF50"))
            userStep++

            if (userStep == gameSequence.size) {
                score += 100
                tvScore.text = score.toString()
                setInputsEnabled(false)
                lifecycleScope.launch {
                    delay(sequenceDelay) // Espera proporcional antes del nuevo nivel
                    startLevel()
                }
            }
        } else {
            // ERROR: Contorno Rojo + Sacudida
            setInputsEnabled(false)
            playErrorAnimation(index)
            lifecycleScope.launch {
                delay(1500)
                endGame()
            }
        }
    }

    /**
     * ANIMACIÓN MAESTRA: Ahora sostiene el brillo más tiempo
     */
    private fun playGameAnimation(index: Int, duration: Long, strokeColor: Int) {
        val cell = cells[index]

        val glowDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(14, strokeColor) // Borde grueso y visible
            setColor(Color.parseColor("#12000000"))
        }

        cell.background = glowDrawable

        // Mantenemos la imagen visible (1f, 1f) el 50% del tiempo de la animación
        val alpha = ObjectAnimator.ofFloat(cell, "alpha", 0f, 1f, 1f, 0f)

        // Mantenemos la escala en su punto máximo también
        val scaleX = ObjectAnimator.ofFloat(cell, "scaleX", 0.7f, 1.05f, 1.05f, 0.8f)
        val scaleY = ObjectAnimator.ofFloat(cell, "scaleY", 0.7f, 1.05f, 1.05f, 0.8f)

        AnimatorSet().apply {
            playTogether(alpha, scaleX, scaleY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        cell.postDelayed({ cell.background = null }, duration)
    }

    private fun playErrorAnimation(index: Int) {
        val cell = cells[index]

        val errorDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(16, Color.RED)
            setColor(Color.parseColor("#30FF0000"))
        }
        cell.background = errorDrawable

        val alpha = ObjectAnimator.ofFloat(cell, "alpha", 0f, 1f, 1f)
        val shake = ObjectAnimator.ofFloat(cell, "translationX", 0f, 25f, -25f, 25f, -25f, 0f)

        AnimatorSet().apply {
            playTogether(alpha, shake)
            duration = 1000
            start()
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        cells.forEach { it.isEnabled = enabled }
    }

    private fun endGame() {
        val intent = Intent(this, ResultadosActivity::class.java).apply {
            putExtra("extra_score", score)
        }
        startActivity(intent)
        finish()
    }
}