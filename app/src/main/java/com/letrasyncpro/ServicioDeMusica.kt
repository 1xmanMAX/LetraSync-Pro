package com.letrasyncpro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * **Lo que suena, para el sistema** (7-nov-2026: «que el sistema reconozca que reproduzco música y salgan los
 * mandos en vivo, como con Spotify»). La pantalla dice aquí qué suena y qué hacer con cada mando; el servicio
 * lo enseña en la notificación, la pantalla de bloqueo y los ajustes rápidos (y lo oyen los auriculares).
 */
object Mandos {
    data class Estado(val titulo: String = "", val detalle: String = "", val sonando: Boolean = false, val posicionMs: Int = 0, val duracionMs: Int = 0, val velocidad: Float = 1f, val hayLista: Boolean = false)
    val estado = MutableStateFlow<Estado?>(null)
    var alSonar: () -> Unit = {}
    var alPausar: () -> Unit = {}
    var alIr: (Int) -> Unit = {}
    var alSiguiente: () -> Unit = {}
    var alAnterior: () -> Unit = {}

    fun poner(c: Context, e: Estado) {
        val antes = estado.value
        estado.value = e
        if (e.sonando && antes?.sonando != true) runCatching { c.startForegroundService(Intent(c, ServicioDeMusica::class.java)) }
    }
    fun quitar() { estado.value = null }
}

class ServicioDeMusica : Service() {
    companion object {
        private const val CANAL = "letrasync_musica"
        private const val AVISO = 21
        private const val ATRAS = "com.letrasyncpro.musica.ATRAS"
        private const val ALTERNAR = "com.letrasyncpro.musica.ALTERNAR"
        private const val ADELANTE = "com.letrasyncpro.musica.ADELANTE"
        private const val CERRAR = "com.letrasyncpro.musica.CERRAR"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var sesion: MediaSession
    private lateinit var audio: AudioManager

    private val alQuitarAuriculares = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) Mandos.alPausar() }
    }
    private val foco by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener { if (it == AudioManager.AUDIOFOCUS_LOSS || it == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) Mandos.alPausar() }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(AudioManager::class.java)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CANAL, "Música", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false); description = "Los mandos de la canción que suena" }
        )
        sesion = MediaSession(this, "LetraSyncMusica").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = Mandos.alSonar()
                override fun onPause() = Mandos.alPausar()
                override fun onSeekTo(pos: Long) = Mandos.alIr(pos.toInt())
                override fun onSkipToNext() = Mandos.alSiguiente()
                override fun onSkipToPrevious() = Mandos.alAnterior()
                override fun onStop() = Mandos.alPausar()
            })
            isActive = true
        }
        val e = Mandos.estado.value ?: Mandos.Estado()
        if (Build.VERSION.SDK_INT >= 29) startForeground(AVISO, aviso(e), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(AVISO, aviso(e))
        androidx.core.content.ContextCompat.registerReceiver(this, alQuitarAuriculares, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        scope.launch {
            var sonaba = false
            Mandos.estado.collect { e ->
                if (e == null) { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return@collect }
                if (e.sonando && !sonaba) audio.requestAudioFocus(foco)
                sonaba = e.sonando
                poner(e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ATRAS -> Mandos.alAnterior()
            ALTERNAR -> if (Mandos.estado.value?.sonando == true) Mandos.alPausar() else Mandos.alSonar()
            ADELANTE -> Mandos.alSiguiente()
            CERRAR -> { Mandos.alPausar(); Mandos.quitar() }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { unregisterReceiver(alQuitarAuriculares) }
        runCatching { audio.abandonAudioFocusRequest(foco) }
        sesion.isActive = false; sesion.release(); scope.cancel()
        super.onDestroy()
    }

    private fun poner(e: Mandos.Estado) {
        sesion.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SEEK_TO or
                    PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP)
                .setState(if (e.sonando) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED, e.posicionMs.toLong(), if (e.sonando) e.velocidad else 0f)
                .build()
        )
        sesion.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, e.titulo.ifBlank { "Canción" })
                .putString(MediaMetadata.METADATA_KEY_ARTIST, e.detalle.ifBlank { "LetraSync Pro" })
                .putLong(MediaMetadata.METADATA_KEY_DURATION, e.duracionMs.toLong())
                .build()
        )
        getSystemService(NotificationManager::class.java).notify(AVISO, aviso(e))
    }

    private fun aviso(e: Mandos.Estado): Notification {
        fun accion(que: String, icono: Int, rotulo: String) = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(this, icono), rotulo,
            PendingIntent.getService(this, que.hashCode(), Intent(this, ServicioDeMusica::class.java).setAction(que), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ).build()
        val abrir = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        return Notification.Builder(this, CANAL)
            .setSmallIcon(R.drawable.ic_letrasync)
            .setContentTitle(e.titulo.ifBlank { "Canción" })
            .setContentText(e.detalle.ifBlank { "LetraSync Pro" })
            .setContentIntent(abrir)
            .setDeleteIntent(PendingIntent.getService(this, 1, Intent(this, ServicioDeMusica::class.java).setAction(CERRAR), PendingIntent.FLAG_IMMUTABLE))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(e.sonando)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .addAction(accion(ATRAS, android.R.drawable.ic_media_previous, "Anterior"))
            .addAction(if (e.sonando) accion(ALTERNAR, android.R.drawable.ic_media_pause, "Pausa") else accion(ALTERNAR, android.R.drawable.ic_media_play, "Reproducir"))
            .addAction(accion(ADELANTE, android.R.drawable.ic_media_next, "Siguiente"))
            .setStyle(Notification.MediaStyle().setMediaSession(sesion.sessionToken).setShowActionsInCompactView(0, 1, 2))
            .build()
    }
}
