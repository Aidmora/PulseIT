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
    private var tiempoInicio: Long = 0L
    private var tiempoTotal: String = "00:00"
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

        // Calibración de ritmo por dificultad
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
        tiempoInicio = System.currentTimeMillis()
    }

    private fun setupBoard() {
        tvScore.text = score.toString()
        tvLevel.text = "Nivel $level"

        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.alpha = 0f // Invisible al inicio (Memoria)
            cell.background = null

            when (theme) {
                "animals" -> setupAnimalsInCell(index, cell)
                "numbers" -> setupNumbersInCell(index, cell)
                "colors" -> setupColorsInCell(index, cell)
                else -> setupColorsInCell(index, cell)
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
        val colorStrs = listOf(
            "#EF5350", "#42A5F5", "#66BB6A",
            "#FFEE58", "#FFA726", "#AB47BC",
            "#26A69A", "#EC407A", "#78909C"
        )
        val gd = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setColor(Color.parseColor(colorStrs[index % colorStrs.size]))
        }
        cell.background = gd
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
                playGameAnimation(index, sequenceDelay - 200, Color.parseColor("#95FFFFFF"))
                delay(sequenceDelay)
            }
            setInputsEnabled(true)
            tvPlayerName.text = "¡Tu Turno!"
        }
    }

    private fun handleCellClick(index: Int) {
        if (index == gameSequence[userStep]) {
            playGameAnimation(index, sequenceDelay - 200, Color.parseColor("#4CAF50"))
            userStep++

            if (userStep == gameSequence.size) {
                score += 100
                tvScore.text = score.toString()
                setInputsEnabled(false)
                lifecycleScope.launch {
                    delay(sequenceDelay)
                    startLevel()
                }
            }
        } else {
            setInputsEnabled(false)
            playErrorAnimation(index)
            lifecycleScope.launch {
                delay(1500)
                endGame()
            }
        }
    }

    private fun playGameAnimation(index: Int, duration: Long, strokeColor: Int) {
        val cell = cells[index]
        val originalBg = cell.background

        // Creamos el Glow (Borde) manteniendo el color si es el tema colores
        val glowDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(14, strokeColor)
            if (theme == "colors" && originalBg is GradientDrawable) {
                color = originalBg.color
            } else {
                setColor(Color.parseColor("#12000000"))
            }
        }

        cell.background = glowDrawable

        val alpha = ObjectAnimator.ofFloat(cell, "alpha", 0f, 1f, 1f, 0f)
        val scaleX = ObjectAnimator.ofFloat(cell, "scaleX", 0.7f, 1.05f, 1.05f, 0.8f)
        val scaleY = ObjectAnimator.ofFloat(cell, "scaleY", 0.7f, 1.05f, 1.05f, 0.8f)

        AnimatorSet().apply {
            playTogether(alpha, scaleX, scaleY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        cell.postDelayed({
            cell.background = if (theme == "colors") originalBg else null
        }, duration)
    }

    private fun playErrorAnimation(index: Int) {
        val cell = cells[index]
        val originalBg = cell.background

        val errorDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(16, Color.RED)
            if (theme == "colors" && originalBg is GradientDrawable) {
                color = originalBg.color
            } else {
                setColor(Color.parseColor("#30FF0000"))
            }
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
        val tiempoFin = System.currentTimeMillis()
        val totalMs = tiempoFin - tiempoInicio
        val totalSegs = totalMs / 1000
        val minutos = totalSegs / 60
        val segundos = totalSegs % 60
        tiempoTotal = String.format("%02d:%02d", minutos, segundos)

        val intent = Intent(this, ResultadosActivity::class.java).apply {
            putExtra("extra_score", score)
            putExtra("extra_level", level)
            putExtra("extra_time", tiempoTotal)
            putExtra("extra_difficulty", difficulty)
            putExtra("extra_theme", theme)
        }
        startActivity(intent)
        finish()
    }

}