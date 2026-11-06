package com.letrasyncpro.motor

import android.content.Context

/** Lo que el usuario elige en la app: el idioma de las canciones y el tamaño del reconocedor. */
class Preferencias(val idioma: String, val segundoIdioma: String, val modelo: String) {
    companion object {
        private const val ARCHIVO = "letrasync"
        fun de(c: Context): Preferencias {
            val p = c.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            return Preferencias(p.getString("idioma", "").orEmpty(), p.getString("segundo", "").orEmpty(), p.getString("modelo", "").orEmpty())
        }
        /** Lo que tarda el dedo en reaccionar al tocar a tiempo, en ms (se resta a cada toque). */
        fun retardo(c: Context): Int = prefs(c).getInt("retardo", 0)
        fun ponerRetardo(c: Context, ms: Int) = prefs(c).edit().putInt("retardo", ms).apply()
        /** Si las marcas arrastradas en la onda se pegan al inicio del sonido más cercano. */
        fun iman(c: Context): Boolean = prefs(c).getBoolean("iman", true)
        fun ponerIman(c: Context, si: Boolean) = prefs(c).edit().putBoolean("iman", si).apply()
        private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        fun poner(c: Context, idioma: String? = null, segundo: String? = null, modelo: String? = null) {
            c.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().apply {
                idioma?.let { putString("idioma", it) }; segundo?.let { putString("segundo", it) }; modelo?.let { putString("modelo", it) }
            }.apply()
        }
    }
}
