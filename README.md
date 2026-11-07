# LetraSync Pro

**La letra de una canción, sincronizada con su audio, en el teléfono y sin conexión.**

LetraSync Pro toma una canción y su letra y le pone a cada línea (o a cada palabra) el momento exacto en que se canta.
El resultado es un archivo **LRC** (el formato de las letras sincronizadas de los reproductores) o **SRT**
(subtítulos), listo para karaoke, para estudiar una canción o para devolvérselo a [PinDesk](https://github.com/1xmanMAX/PIXPIN_PRO_ANDROID).
Además separa la **voz** de la **música** (karaoke de verdad) y es un **reproductor de música** con los controles del
sistema.

| | |
|---|---|
| **Paquete** | `com.letrasyncpro` |
| **Versión** | 0.9.0 (versionCode 9) |
| **Android** | 10 o más nuevo (minSdk 29, targetSdk 36), solo `arm64-v8a` |
| **Conexión** | No hace falta. Solo para bajar una vez los modelos y para buscar letras en LRCLib |
| **Interfaz** | Jetpack Compose, al estilo de iOS / Apple Music |

---

## Índice

1. [Instalar](#instalar)
2. [Cómo se usa (paso a paso)](#cómo-se-usa-paso-a-paso)
3. [Todas las funciones](#todas-las-funciones)
4. [Separar voz y música](#separar-voz-y-música)
5. [Reproductor de música](#reproductor-de-música)
6. [Integración con PinDesk](#integración-con-pindesk)
7. [Formatos](#formatos)
8. [Privacidad](#privacidad)
9. [Cómo está hecha](#cómo-está-hecha)
10. [Compilar](#compilar)
11. [Historial de versiones](#historial-de-versiones)
12. [Créditos y licencias](#créditos-y-licencias)

---

## Instalar

1. Entra en **[Releases](https://github.com/1xmanMAX/LetraSync-Pro/releases)** y baja el último `LetraSyncPro-<versión>.apk`.
2. Ábrelo en el teléfono. La primera vez Android pide permitir **«Instalar apps de esta fuente»** al navegador o al
   gestor de archivos: actívalo y vuelve.
3. Las versiones siguientes se instalan encima de la anterior (van firmadas con la misma clave): no se pierde nada.

> Si Android dice «App no instalada» al actualizar, es que la que tienes se firmó con otra clave: desinstálala
> primero (se pierde la biblioteca de canciones).

---

## Cómo se usa (paso a paso)

La pantalla principal tiene **tres pasos** en un control segmentado arriba: **Letra · Sincronizar · Revisar**.

### 1. Elegir la canción

Toca **«Elegir canción»** y escoge un audio del teléfono (MP3, M4A, AAC, OGG, FLAC, WAV…). También llega sola desde
PinDesk ([ver abajo](#integración-con-pindesk)) o desde tu **biblioteca** (las canciones recientes, con la letra
como la dejaste).

### 2. Letra

Tres maneras de tener la letra:

- **Pegar**: copias la letra de cualquier sitio y la pegas. Una línea por frase.
- **Buscar**: la busca en **[LRCLib](https://lrclib.net)** por título y artista. Si allí ya está sincronizada, la
  trae con sus tiempos.
- **Archivo**: abre un `.lrc`, un `.srt` o un `.txt`.

### 3. Sincronizar

Elige cómo:

| Modo | Para qué | Cómo |
|---|---|---|
| **En la onda** (recomendado) | La mayor precisión | Arriba la frase, grande; abajo la **onda** de la canción. La app propone dónde empieza la voz y lo hace sonar: **«Aceptar»** o tocas en la onda donde empieza de verdad. Deslizar mueve la canción; **pellizcar acerca** (hasta 1 s a la vista, con un punto cada 5 ms). Velocidad **½× · ¾× · 1×** sin cambiar el tono. |
| **Al ritmo** | Rápido, si conoces la canción | Suena la canción y das **un toque** al empezar cada línea. |
| **Palabra a palabra** | Karaoke con la palabra que suena | Un toque por palabra. Sale un **LRC mejorado** (`<mm:ss.xx>` por palabra). |
| **Automática** | Sin tocar nada | Whisper **oye la canción** y alinea la letra con lo que oyó (ver [Cómo está hecha](#cómo-está-hecha)). |

Cada tiempo se guarda con un **anticipo** (0,3 s por defecto, en Ajustes): la línea aparece un poquito antes de
cantarse, como en los buenos reproductores. En la onda, la franja antes de cada marca es ese anticipo.
El **retardo al tocar** (Ajustes) descuenta lo que tarda tu dedo.

### 4. Revisar

La onda con todas las líneas encima. Tocas una y aparece su tiempo en grande con:

- **−0,1 / +0,1 s** (mantener repite), **«Aquí»** (el momento actual), **«Oír»** (desde 2 s antes) y **«Bucle»**.
- **Mover una línea nunca mueve otra** ni la deja cruzarse con sus vecinas.
- En **ámbar** las líneas que se pusieron a ojo; en **rojo** las que van antes que la anterior.
- **Deshacer / rehacer** de todo.

### 5. Guardar o compartir

**Sacar** guarda o comparte el **LRC** (con centésimas) o el **SRT**. Si la canción llegó de PinDesk, **«Listo»** se
la devuelve sincronizada.

---

## Todas las funciones

Lo poco frecuente está en **«Más»**:

- **Sincronizar sola** con el reconocedor de voz, con tres tamaños en Ajustes: **rápido, equilibrado y preciso**.
- **Afinar**: −0,5 / −0,1 / +0,1 / +0,5 s, «Ahora», «Oír», quitar el tiempo, ir a la anterior o la siguiente.
  Mantener una línea le pone el tiempo actual.
- **Toda la letra**: adelantarla o atrasarla entera; **rellenar** las que quedaron sin tiempo repartiéndolas entre sus
  vecinas; quitar todos los tiempos.
- **Mover también las de detrás** (edición por lotes).
- **Bucle de una línea** para repetir un trozo difícil.
- **La onda con imán**: arrastrar una marca la mueve y, con el imán, se pega al arranque del sonido más cercano.
- **Datos de la canción** (`[ti:]` título, `[ar:]` artista, `[al:]` álbum, `[by:]` autor del LRC).
- **Vista previa** a pantalla completa, como la verá quien la use.
- **Guardar LRC solo por líneas** aunque se haya hecho palabra a palabra.
- **Barra de tiempo como TikTok**: se arrastra con una mano desde donde iba, sin saltos.

---

## Separar voz y música

**Más → «Separar voz y música»**: la canción se separa en **solo voz** y **karaoke** (la música sin la voz),
**en el teléfono**.

- Modelo: **UVR-MDX-NET-Voc_FT** (el de voz más usado de *Ultimate Vocal Remover*), en la versión ONNX de sherpa-onnx.
  Se baja una vez (67 MB).
- Al separar, el reproductor deja elegir **Canción · Solo voz · Karaoke**, y la onda de «En la onda» pasa a ser la de
  la voz: así la app propone mucho mejor dónde empieza cada frase.
- **«Separación fina»** (Ajustes): más lenta y más limpia (procesa también la canción invertida y promedia).
- Comprobado contra la implementación de referencia de sherpa-onnx: la voz sale **igual (76 dB de SNR)**.

Detalles técnicos: [docs/separar-voz.md](docs/separar-voz.md).

---

## Reproductor de música

- **Biblioteca** con tus canciones y las que llegan desde PinDesk.
- **Suena de fondo** (servicio de reproducción) con **notificación, pantalla de bloqueo y ajustes rápidos**
  (MediaSession), como cualquier reproductor.
- **Karaoke** con la línea (o la palabra) que suena resaltada.

---

## Integración con PinDesk

PinDesk usa LetraSync Pro como herramienta externa, igual que su escáner: en el karaoke de una canción, mantener la
canción en el chat → **«Sincronizar letra»**.

1. PinDesk le manda la canción (como `content://`, con permiso de lectura) y su letra.
2. LetraSync se abre con todo cargado; sincronizas como siempre.
3. **«Listo»** devuelve la letra con sus tiempos (formato `[m:ss] línea` de PinDesk y LRC estándar). **El texto es el
   que se mandó**: solo cambian los tiempos.

Contrato completo (acción, extras, respuesta), para usarlo desde cualquier app:
[docs/integracion.md](docs/integracion.md).

```kotlin
val pedir = Intent("com.letrasyncpro.action.SINCRONIZAR")
    .setPackage("com.letrasyncpro")
    .setDataAndType(uriDeLaCancion, "audio/*")
    .putExtra("com.letrasyncpro.extra.LETRA", letra)
    .putExtra("com.letrasyncpro.extra.TITULO", "Bésame mucho")
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
```

---

## Formatos

| Formato | Entra | Sale | Notas |
|---|---|---|---|
| **LRC** | ✅ | ✅ | `[mm:ss.xx]línea`, con etiquetas `[ti:] [ar:] [al:] [by:]` |
| **LRC mejorado** | ✅ | ✅ | Tiempos por palabra `<mm:ss.xx>` |
| **SRT** | ✅ | ✅ | Subtítulos con inicio y fin |
| **Texto** | ✅ | — | Una línea por frase |
| **Audio** | MP3, M4A/AAC, OGG/Opus, FLAC, WAV | WAV (voz / karaoke) | Lo que decodifique Android |

---

## Privacidad

- La canción **no sale del teléfono**: reconocer, alinear y separar se hace en el propio aparato.
- Solo hay red para **bajar los modelos** (una vez, desde los releases de sherpa-onnx y Hugging Face) y para
  **buscar letras en LRCLib** cuando lo pides (se manda título, artista y duración).
- Sin cuentas, sin anuncios, sin analíticas.

---

## Cómo está hecha

```
app/src/main/java/com/letrasyncpro/
├── ui/
│   ├── LetraSyncActivity.kt   La pantalla: los tres pasos, «Más», ajustes y biblioteca
│   ├── MarcarEnLaOnda.kt      «En la onda»: la onda estirable, la propuesta y el anticipo
│   ├── PiezasPro.kt, Ios.kt   Controles al estilo de iOS (segmentado, listas, botones)
│   └── Tema.kt                Colores claro/oscuro
├── motor/
│   ├── LetraDeCancion.kt      El modelo de la letra (líneas, palabras, tiempos) y LRC/SRT
│   ├── EdicionDeLetra.kt      Mover sin cruzar, rellenar, deshacer/rehacer
│   ├── Onda.kt                La onda de la canción (picos por tramo)
│   ├── OirCancion.kt          Reproducir y arrastrar el audio (AudioTrack), velocidad sin cambiar el tono
│   ├── RelojSuave.kt          El tiempo de la canción sin saltos para pintar
│   ├── Transcriptor.kt        Whisper con sherpa-onnx: lo que se oye, con sus tiempos
│   ├── AlinearLetra.kt        Alinear la letra con lo oído (el mismo cálculo que PinDesk y su versión de PC)
│   ├── MotorWhisper.kt        Bajar y elegir el modelo (rápido / equilibrado / preciso)
│   ├── SepararVoz.kt          Decodificar en estéreo 44,1 kHz y guardar las pistas
│   ├── SeparacionUvr.kt       UVR MDX-Net: tramos solapados, STFT, modelo, STFT inversa
│   ├── Stft.kt                STFT / iSTFT (Hann, n_fft 6144, hop 1024)
│   ├── LrcLib.kt              Buscar letras en lrclib.net
│   ├── Biblioteca.kt          Las canciones recientes con su letra
│   └── Preferencias.kt        Ajustes (anticipo, retardo, tamaño del reconocedor…)
├── Automatica.kt / ServicioAutomatico   La sincronización automática, en un proceso aparte (`:reconocedor`)
├── ServicioDeMusica.kt        Reproducción de fondo y MediaSession
└── Integracion.kt             La petición de PinDesk y la respuesta
app/src/main/cpp/separador.c   Puente en C a la API de onnxruntime para el separador
app/src/main/jniLibs/arm64-v8a  libonnxruntime.so, libsherpa-onnx-jni.so, libseparador.so
```

Decisiones importantes:

- **La automática corre en otro proceso** (`:reconocedor`): si el modelo se queda sin memoria, cae ese proceso, no la app.
- **Un solo onnxruntime**: sherpa-onnx exige exactamente el suyo (1.27.1), así que el separador lo usa desde C
  (`separador.c`, compilado con `herramientas/compilar-separador.sh`) en vez de meter otro.
- **Mover una línea nunca mueve otra**: los límites se calculan con las vecinas antes de aplicar el cambio.

---

## Compilar

Requisitos: **JDK 21**, **Android SDK 36** (build-tools 36.0.0) y, solo para recompilar el separador en C, el
**NDK 27.2.12479018**.

```sh
./gradlew assembleRelease                         # el APK
sh herramientas/apk-para-repartir.sh              # el APK firmado para repartir (v1+v2)
sh herramientas/compilar-separador.sh             # solo si cambias app/src/main/cpp/separador.c
```

- Sin `local.properties` se usa `ANDROID_HOME`.
- `herramientas/prueba.keystore` es una **clave de prueba** pública: sirve para que las versiones de prueba se
  instalen unas encima de otras. Para publicar en serio, usa tu propia clave (nunca la subas al repositorio).

### En GitHub

La acción **«APK de prueba»** (`.github/workflows/prueba.yml`, se lanza a mano en *Actions*) compila y publica el APK
como **prerelease** `v<versión>-prueba`, o como release normal `v<versión>` si se marca «estable».

---

## Historial de versiones

| Versión | Qué trajo |
|---|---|
| **0.9.0** | Reproductor de música: biblioteca, de fondo, mandos del sistema; las canciones de PinDesk en la biblioteca |
| **0.8.0** | La barra del tiempo se arrastra como en TikTok (desde donde iba, con una mano) |
| **0.7.0** | La automática ya no cierra la app; la onda no tiembla; las marcas quedan donde se ponen; se oye al arrastrar |
| **0.6.0** | Separar voz y música (UVR MDX-Net); marcar más despacio con la onda estirable |
| **0.5.0** | «En la onda» (eliges dónde empieza cada frase, con anticipo) y diseño al estilo de Apple |
| **0.4.0** | Rediseño táctil en tres pasos; mover una línea ya no mueve ni cruza a las vecinas |
| **0.3.0** | Onda con marcas arrastrables e imán, palabra a palabra (LRC mejorado), bucle, LRCLib, SRT, vista previa |
| **0.2.0** | Afinar por décimas, oír desde 2 s antes, velocidad, mover toda la letra, rellenar huecos, deshacer/rehacer, biblioteca |
| **0.1.0** | Primera versión: la letra sincronizada con su audio |

---

## Créditos y licencias

LetraSync Pro se apoya en estos proyectos (sus licencias se respetan; ver cada enlace):

- **[sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)** (Apache-2.0): reconocimiento de voz (Whisper) y los
  modelos ONNX, incluido el separador.
- **[ONNX Runtime](https://github.com/microsoft/onnxruntime)** (MIT).
- **[Whisper](https://github.com/openai/whisper)** (MIT), de OpenAI, en las versiones ONNX de sherpa-onnx.
- **[Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui)** (MIT): el modelo UVR-MDX-NET-Voc_FT.
- **[LRCLib](https://lrclib.net)**: la base abierta de letras sincronizadas.
- Ideas de interfaz tomadas de (sin copiar su código): [LRC Editor](https://github.com/Spikatrix/LRC-Editor),
  [lrc-maker](https://github.com/magic-akari/lrc-maker), [Chronograph](https://github.com/Dzheremi2/Chronograph),
  [Composer](https://github.com/better-lyrics/composer) y [Voxen](https://github.com/Raqhael-ux/Voxen-LRC-Editor).

El código de LetraSync Pro no lleva todavía una licencia abierta: **todos los derechos reservados** por su autor.
