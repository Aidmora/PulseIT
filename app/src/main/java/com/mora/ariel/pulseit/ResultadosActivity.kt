package com.mora.ariel.pulseit

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ResultadosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    // Views
    private lateinit var tvPlayerName: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvCongrats: TextView
    private lateinit var imgPlayer: ImageView

    private var finalPlayer: MediaPlayer? = null

    // Variables que vienen de JuegoActivity
    private var level: Int = 0
    private var tiempoTotal: String = "00:00"
    private var difficulty: String = "normal"
    private var theme: String = "colors"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados)


        stopService(Intent(this, MusicService::class.java))

        auth = FirebaseAuth.getInstance()

        tvPlayerName = findViewById(R.id.tvPlayerName)
        tvScore = findViewById(R.id.tvScore)
        tvLevel = findViewById(R.id.tvLevel)
        tvTime = findViewById(R.id.tvTime)
        tvCongrats = findViewById(R.id.tvCongrats)
        imgPlayer = findViewById(R.id.imgPlayer)

        val puntajeObtenido = intent.getIntExtra("extra_score", 0)
        level = intent.getIntExtra("extra_level", 0)
        tiempoTotal = intent.getStringExtra("extra_time") ?: "00:00"
        difficulty = intent.getStringExtra("extra_difficulty") ?: "normal"
        theme = intent.getStringExtra("extra_theme") ?: "colors"

        tvScore.text = puntajeObtenido.toString()
        tvLevel.text = level.toString()
        tvTime.text = tiempoTotal
        tvCongrats.text = "¡Nivel $level Superado!"
        //Reproducir sonido
        playFinalSound()

        cargarDatosUsuario()
        actualizarPuntaje(puntajeObtenido)

        /*findViewById<MaterialButton>(R.id.btnPlayAgain).setOnClickListener {
            startService(Intent(this, MusicService::class.java))

            val intent = Intent(this, JuegoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }*/

        findViewById<MaterialButton>(R.id.btnBackHome).setOnClickListener {
            val intent = Intent(this, EleccionTemaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        /*findViewById<MaterialButton>(R.id.btnChangeMode).setOnClickListener {
            val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }*/
    }

    /**
     * Reproduce el sonido de finalización
     */
    private fun playFinalSound() {
        try {
            finalPlayer = MediaPlayer.create(this, R.raw.sound_final)
            finalPlayer?.start()
            // Liberar memoria automáticamente al terminar
            finalPlayer?.setOnCompletionListener { mp ->
                mp.release()
                finalPlayer = null
            }
        } catch (e: Exception) {
            Log.e("Audio", "Error al reproducir sound_final", e)
        }
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: return
        tvPlayerName.text = user.displayName ?: "Invitado"

        user.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop() // Foto circular para mejor estética
                .into(imgPlayer)
        }

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                doc.getString("nombre")?.let {
                    tvPlayerName.text = it
                }
            }
    }

    private fun actualizarPuntaje(puntaje: Int) {
        val user = auth.currentUser ?: return
        val userRef = db.collection("users").document(user.uid)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val nombre = user.displayName ?: "Invitado"
            val email = user.email ?: "N/A"
            val fotoUrl = user.photoUrl?.toString() ?: ""
            val esInvitado = user.isAnonymous

            if (!snapshot.exists()) {
                val data = hashMapOf(
                    "uid" to user.uid,
                    "nombre" to nombre,
                    "email" to email,
                    "fotoUrl" to fotoUrl,
                    "tipoCuenta" to if (esInvitado) "invitado" else "google",
                    "puntajeMaximo" to puntaje,
                    "partidasJugadas" to 1,
                    "fechaRegistro" to com.google.firebase.Timestamp.now()
                )
                transaction.set(userRef, data)
            } else {
                val puntajeActual = snapshot.getLong("puntajeMaximo") ?: 0
                if (puntaje > puntajeActual) {
                    transaction.update(userRef, "puntajeMaximo", puntaje)
                }
                transaction.update(userRef, "partidasJugadas", FieldValue.increment(1))
                transaction.update(userRef, "nombre", nombre)
                transaction.update(userRef, "fotoUrl", fotoUrl)
                transaction.update(userRef, "tipoCuenta", if (esInvitado) "invitado" else "google")
            }
        }.addOnSuccessListener {
            Log.d("Firestore", "Perfil y puntuación actualizados")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al actualizar datos", e)
        }

        val gameData = hashMapOf(
            "score" to puntaje,
            "level" to level,
            "time" to tiempoTotal,
            "difficulty" to difficulty,
            "theme" to theme,
            "fecha" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(user.uid).collection("games").add(gameData)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos si la actividad se cierra antes de que el sonido termine
        finalPlayer?.release()
        finalPlayer = null
    }
}