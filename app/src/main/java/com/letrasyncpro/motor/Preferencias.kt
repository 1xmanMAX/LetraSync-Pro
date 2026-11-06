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
        /**
         * **El anticipo**: cuánto antes de que se cante aparece cada línea, en ms. Lo que se marca (en la onda, a
         * toques, «Aquí») es donde empieza la voz; se guarda ese momento menos el anticipo, para que dé tiempo a
         * leerla. 300 ms por defecto, lo que usan los karaokes.
         */
        fun anticipo(c: Context): Int = prefs(c).getInt("anticipo", ANTICIPO_POR_DEFECTO)
        fun ponerAnticipo(c: Context, ms: Int) = prefs(c).edit().putInt("anticipo", ms).apply()
        const val ANTICIPO_POR_DEFECTO = 300
        /** Separar voz y música en dos pasadas (más limpia, el doble de lenta). */
        fun separarFino(c: Context): Boolean = prefs(c).getBoolean("separarFino", false)
        fun ponerSepararFino(c: Context, si: Boolean) = prefs(c).edit().putBoolean("separarFino", si).apply()
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
