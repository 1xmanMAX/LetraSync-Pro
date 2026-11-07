package com.letrasyncpro

/**
 * **Cómo se usa LetraSync Pro desde otra aplicación** (PinDesk), como «Escáner Pro Max»: una acción con la
 * canción y su letra, y de vuelta la letra con sus tiempos. Ver `docs/integracion.md`.
 *
 * - Acción [ACCION] (`com.letrasyncpro.action.SINCRONIZAR`), con el audio en `data` (un `content://` con permiso de
 *   lectura, también en el `ClipData`) y [EXTRA_LETRA] con la letra, con o sin tiempos.
 * - Vuelve con `RESULT_OK` y, en [EXTRA_LETRA], la letra con tiempos en el formato de PinDesk (`[m:ss] línea`,
 *   el mismo que entiende el PC), y en [EXTRA_LRC] la misma en LRC estándar. Cancelado: no se cambia nada.
 */
object Integracion {
    const val PAQUETE = "com.letrasyncpro"
    const val ACCION = "com.letrasyncpro.action.SINCRONIZAR"
    const val EXTRA_LETRA = "com.letrasyncpro.extra.LETRA"
    const val EXTRA_LRC = "com.letrasyncpro.extra.LRC"
    const val EXTRA_TITULO = "com.letrasyncpro.extra.TITULO"
    const val EXTRA_QUIEN_LLAMA = "com.letrasyncpro.extra.CALLER_LABEL"
    /** El id de la canción en la app que llama: se guarda en la biblioteca para mandarle luego la letra retocada. */
    const val EXTRA_ID = "com.letrasyncpro.extra.ID"
    /** PinDesk: a quién y con qué se le manda la letra retocada desde la biblioteca. */
    const val PAQUETE_PINDESK = "com.forge.pixpin"
    const val ACCION_DEVUELTA = "com.forge.pixpin.action.LETRA_SINCRONIZADA"
}
