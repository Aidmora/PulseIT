package com.mora.ariel.pulseit

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mora.ariel.pulseit.EXTRA_THEME
import com.mora.ariel.pulseit.EXTRA_DIFFICULTY
import com.mora.ariel.pulseit.EXTRA_LANGUAGE

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
    private var sequenceDelay = 600L

    companion object {
        const val EXTRA_SCORE = "extra_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        theme = intent.getStringExtra(EXTRA_THEME)
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY)

        sequenceDelay = when (difficulty) {
            "easy" -> 1000L
            "hard" -> 300L
            else -> 600L
        }

        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvPlayerName = findViewById(R.id.tvPlayerName)

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

        setupBoard()
        setupClickListeners()

        lifecycleScope.launch {
            delay(1000)
            startLevel()
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        cells.forEach { cell ->
            cell.isEnabled = enabled
            cell.isClickable = enabled
        }
    }

    private fun setupBoard() {
        tvScore.text = score.toString()
        tvLevel.text = "Nivel $level"

        when (theme) {
            "animals" -> setupAnimalsTheme()
            "colors" -> setupColorsTheme()
            "numbers" -> setupNumbersTheme()
            else -> setupColorsTheme()
        }
    }

    private fun createRoundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 28f
            setColor(color)
        }
    }

    private fun setupAnimalsTheme() {
        val animalResources = listOf(
            R.drawable.animal_tortuga,
            R.drawable.animal_leon,
            R.drawable.animal_elefante,
            R.drawable.animal_pato,
            R.drawable.animal_perro,
            R.drawable.animal_perezoso,
            R.drawable.animal_pinguino,
            R.drawable.animal_zorro,
            R.drawable.animal_abeja
        )

        if (animalResources.size < 9) return

        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.background = null

            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(animalResources[index])
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                val pad = 8
                setPadding(pad, pad, pad, pad)
            }

            cell.addView(imageView)
        }
    }

    private fun setupColorsTheme() {
        val colorList = listOf(
            Color.parseColor("#EF5350"), Color.parseColor("#42A5F5"), Color.parseColor("#66BB6A"),
            Color.parseColor("#FFEE58"), Color.parseColor("#FFA726"), Color.parseColor("#AB47BC"),
            Color.parseColor("#26A69A"), Color.parseColor("#EC407A"), Color.parseColor("#78909C")
        )
        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.background = createRoundedBackground(colorList[index])
        }
    }

    private fun setupNumbersTheme() {
        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.background = createRoundedBackground(Color.parseColor("#37474F"))

            val textView = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                text = (index + 1).toString()
                textSize = 36f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

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
        gameSequence.add((0..8).random())
        showSequence()
    }

    private fun showSequence() {
        lifecycleScope.launch {
            delay(500)
            for (index in gameSequence) {
                highlightCell(index, sequenceDelay / 2)
                delay(sequenceDelay)
            }
            setInputsEnabled(true)
            tvPlayerName.text = "¡Tu Turno!"
        }
    }

    private fun handleCellClick(index: Int) {
        highlightCell(index, 150)

        if (index == gameSequence[userStep]) {
            userStep++
            if (userStep == gameSequence.size) {
                score += 100
                tvScore.text = score.toString()
                setInputsEnabled(false)
                lifecycleScope.launch {
                    delay(1000)
                    startLevel()
                }
            }
        } else {
            setInputsEnabled(false)
            endGame()
        }
    }

    private fun highlightCell(index: Int, duration: Long) {
        val cell = cells[index]
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
        finish()
    }
}