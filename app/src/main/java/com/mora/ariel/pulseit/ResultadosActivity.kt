package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide

class ResultadosActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var tvPlayerName: TextView
    private lateinit var tvScore: TextView
    private lateinit var imgPlayer: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_resultados)
        auth = FirebaseAuth.getInstance()

        tvPlayerName = findViewById(R.id.tvPlayerName)
        tvScore = findViewById(R.id.tvScore)
        imgPlayer = findViewById(R.id.imgPlayer)

        cargarDatosUsuario()

        // --- Lógica de navegación para los botones de resultados ---
        val puntajeObtenido = intent.getIntExtra("extra_score", 0)
        actualizarPuntaje(puntajeObtenido)

        val btnPlayAgain = findViewById<MaterialButton>(R.id.btnPlayAgain)
        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, JuegoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

            finish()
        }

        val btnBackHome = findViewById<MaterialButton>(R.id.btnBackHome)
        btnBackHome.setOnClickListener {
            val intent = Intent(this, EleccionTemaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)

            finish()
        }

        val btnChangeMode = findViewById<MaterialButton>(R.id.btnChangeMode)
        btnChangeMode.setOnClickListener {
            // Creamos la intención de ir a la pantalla de Configuración de Partida
            val intent = Intent(this, ConfiguracionPartidaActivity::class.java)
            // Limpiamos la pila hasta la pantalla de configuración
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            // Cerramos la pantalla actual de resultados
            finish()
        }
    }
    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: return
        tvPlayerName.text = user.displayName ?: "Invitado"
        user.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .into(imgPlayer)
        }

        val score = intent.getIntExtra("extra_score", 0)
        tvScore.text = score.toString()

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
                // Si el usuario NO existe en Firestore, creamos el documento completo
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
            Log.d("Firestore", "Perfil y puntuación actualizados para: ${user.displayName}")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al actualizar datos", e)
        }

        val gameData = hashMapOf(
            "score" to puntaje,
            "fecha" to com.google.firebase.Timestamp.now(),
            "difficulty" to "normal",
            "theme" to "colors"
        )

        db.collection("users")
            .document(user.uid)
            .collection("games")
            .add(gameData)

    }
}
