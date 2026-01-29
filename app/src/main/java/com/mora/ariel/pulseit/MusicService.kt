package com.mora.ariel.pulseit

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
/**
 * Servicio encargado de gestionar la música de fondo de forma global.
 */
class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.bg_music)
        mediaPlayer?.isLooping = true // Reproducción infinita
        mediaPlayer?.setVolume(0.5f, 0.5f) // Volumen al 50%
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}