package com.mora.ariel.pulseit // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.io.path.exists

class ResultadosActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_resultados)

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
    private fun actualizarPuntaje(puntaje: Int) {
        val user = auth.currentUser ?: return
        val userRef = db.collection("users").document(user.uid)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            if (!snapshot.exists()) {
                val data = hashMapOf(
                    "nombre" to (user.displayName ?: "Invitado"),
                    "puntajeMaximo" to puntaje,
                    "partidasJugadas" to 1
                )
                transaction.set(userRef, data)
            } else {
                val puntajeActual = snapshot.getLong("puntajeMaximo") ?: 0
                if (puntaje > puntajeActual) {
                    transaction.update(userRef, "puntajeMaximo", puntaje)
                }
                transaction.update(userRef, "partidasJugadas",
                    FieldValue.increment(1))
            }
        }.addOnSuccessListener {
            Log.d("Firestore", "Estadísticas actualizadas con éxito")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al actualizar ranking", e)
        }
    }
}
