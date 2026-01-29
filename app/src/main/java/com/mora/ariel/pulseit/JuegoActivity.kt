package com.mora.ariel.pulseit

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JuegoActivity : AppCompatActivity() {

    private lateinit var cells: List<FrameLayout>
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvPlayerName: TextView

    // Sistema de Audio
    private lateinit var soundPool: SoundPool
    private var sPop: Int = 0
    private var sSuccess: Int = 0
    private var sError: Int = 0

    // Variables de estado del juego
    private var tiempoInicio: Long = 0L
    private var score = 0
    private var level = 0
    private val gameSequence = mutableListOf<Int>()
    private var userStep = 0
    private var theme: String? = null
    private var difficulty: String? = null
    private var sequenceDelay = 1100L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)
        theme = intent.getStringExtra(EXTRA_THEME)
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY)
        sequenceDelay = when (difficulty) {
            getString(R.string.difficulty_easy) -> 1400L
            getString(R.string.difficulty_hard) -> 800L
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

        initSoundEffects()
        setupBoard()
        setupClickListeners()
        setInputsEnabled(false)
        lifecycleScope.launch {
            delay(1000)
            startLevel()
        }
        tiempoInicio = System.currentTimeMillis()
    }

    private fun initSoundEffects() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        sPop = soundPool.load(this, R.raw.sound_pop, 1)
        sSuccess = soundPool.load(this, R.raw.sound_success, 1)
        sError = soundPool.load(this, R.raw.sound_error, 1)
    }

    private fun setupBoard() {
        tvScore.text = score.toString()
        tvLevel.text = getString(R.string.level_prefix, level)

        cells.forEachIndexed { index, cell ->
            cell.removeAllViews()
            cell.alpha = 0f
            cell.background = null

            when (theme) {
                getString(R.string.theme_animals) -> setupAnimalsInCell(index, cell)
                getString(R.string.theme_numbers) -> setupNumbersInCell(index, cell)
                getString(R.string.theme_colors) -> setupColorsInCell(index, cell)
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
            setTextColor(ContextCompat.getColor(context, R.color.black))
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        cell.addView(txt)
    }

    private fun setupColorsInCell(index: Int, cell: FrameLayout) {
        // Usa los colores definidos en colors.xml
        val colors = listOf(
            R.color.game_tile_1, R.color.game_tile_2, R.color.game_tile_3,
            R.color.game_tile_4, R.color.game_tile_5, R.color.game_tile_6,
            R.color.game_tile_7, R.color.game_tile_8, R.color.game_tile_9
        )
        val gd = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setColor(ContextCompat.getColor(this@JuegoActivity, colors[index % colors.size]))
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
        tvPlayerName.text = getString(R.string.observe_text)
        userStep = 0
        level++
        tvLevel.text = getString(R.string.level_prefix, level)
        gameSequence.add((0..8).random())
        showSequence()
    }

    private fun showSequence() {
        lifecycleScope.launch {
            delay(800)
            for (index in gameSequence) {
                soundPool.play(sPop, 1f, 1f, 0, 0, 1f)
                playGameAnimation(index, sequenceDelay - 200, ContextCompat.getColor(this@JuegoActivity, R.color.glow_white))
                delay(sequenceDelay)
            }
            setInputsEnabled(true)
            tvPlayerName.text = getString(R.string.your_turn_text)
        }
    }

    private fun handleCellClick(index: Int) {
        if (index == gameSequence[userStep]) {
            soundPool.play(sSuccess, 1f, 1f, 0, 0, 1.1f)
            playGameAnimation(index, sequenceDelay - 200, ContextCompat.getColor(this, R.color.glow_success))
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
            soundPool.play(sError, 1f, 1f, 0, 0, 1f)
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

        val glowDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(14, strokeColor)
            if (theme == getString(R.string.theme_colors) && originalBg is GradientDrawable) {
                color = (originalBg as GradientDrawable).color
            } else {
                setColor(ContextCompat.getColor(this@JuegoActivity, R.color.overlay_dark))
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
            cell.background = if (theme == getString(R.string.theme_colors)) originalBg else null
        }, duration)
    }

    private fun playErrorAnimation(index: Int) {
        val cell = cells[index]
        val originalBg = cell.background

        val errorDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 45f
            setStroke(16, ContextCompat.getColor(this@JuegoActivity, R.color.glow_error))
            if (theme == getString(R.string.theme_colors) && originalBg is GradientDrawable) {
                color = (originalBg as GradientDrawable).color
            } else {
                setColor(ContextCompat.getColor(this@JuegoActivity, R.color.overlay_error))
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
        val totalSegs = (System.currentTimeMillis() - tiempoInicio) / 1000
        val tiempoTotalStr = String.format("%02d:%02d", totalSegs / 60, totalSegs % 60)

        val intent = Intent(this, ResultadosActivity::class.java).apply {
            putExtra(EXTRA_SCORE, score)
            putExtra(EXTRA_THEME, theme)
            putExtra(EXTRA_DIFFICULTY, difficulty)
            // Extras dinámicos para la pantalla final
            putExtra("extra_level", level)
            putExtra("extra_time", tiempoTotalStr)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}