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
        fun poner(c: Context, idioma: String? = null, segundo: String? = null, modelo: String? = null) {
            c.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().apply {
                idioma?.let { putString("idioma", it) }; segundo?.let { putString("segundo", it) }; modelo?.let { putString("modelo", it) }
            }.apply()
        }
    }
}
