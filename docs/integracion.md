# Integrar LetraSync Pro desde otra app (PinDesk)

LetraSync Pro se usa como «Escáner Pro Max»: la otra app le manda la canción y su letra, y recibe la letra
con sus tiempos. Sin servidores: todo pasa en el teléfono.

## La petición

```kotlin
val pedir = Intent("com.letrasyncpro.action.SINCRONIZAR")
    .setPackage("com.letrasyncpro")
    .setDataAndType(uriDeLaCancion, "audio/*")          // content://, con permiso de lectura
    .putExtra("com.letrasyncpro.extra.LETRA", letra)       // con o sin tiempos
    .putExtra("com.letrasyncpro.extra.TITULO", "Bésame mucho")
    .putExtra("com.letrasyncpro.extra.CALLER_LABEL", "PinDesk")
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
pedir.clipData = ClipData.newRawUri("cancion", uriDeLaCancion)
```

## La respuesta

- `RESULT_OK`:
  - `com.letrasyncpro.extra.LETRA`: la letra con tiempos en el formato de PinDesk y del PC: `[m:ss] línea`
    y un renglón en blanco entre líneas. **El texto es el que se mandó**; solo cambian los tiempos.
  - `com.letrasyncpro.extra.LRC`: la misma en LRC estándar (`[mm:ss.xx]línea`).
- `RESULT_CANCELED`: no se cambia nada.

## Cómo sincroniza

- **Sola**: Whisper (sherpa-onnx, en el teléfono; el modelo se baja una vez, unos 160 MB) oye la canción y la
  letra se alinea con lo oído (`AlinearLetra`, el mismo cálculo que PinDesk y el PC: con lo mismo oído, los
  mismos minutos). Las líneas que no se oyeron se reparten «a ojo» y se ven en ámbar.
- **A toques**: con la canción sonando, un toque al empezar cada línea.
- **Corregir**: en el karaoke, mantener una línea le pone el tiempo de ahora.
