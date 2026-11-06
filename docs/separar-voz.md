# Separar voz y música

LetraSync Pro separa una canción en **solo voz** y **karaoke** (la música sin la voz), en el teléfono y sin conexión
(después de bajar una vez el separador). Sirve para cantar encima y, sobre todo, para **marcar mejor**: con la voz
separada, la onda de «En la onda» enseña solo la voz y la app propone el arranque de cada frase sobre ella.

## De dónde sale

Se miró lo que usan las apps abiertas que lo hacen: **Demucs** (Meta), **Spleeter** (Deezer) y **Ultimate Vocal
Remover** (los modelos **MDX-Net**). En calidad de voz, MDX-Net está por encima de Spleeter y es mucho más ligero que
Demucs para un teléfono. Se usa **UVR-MDX-NET-Voc_FT**, el de voz más usado de UVR, en la versión ONNX que reparte
sherpa-onnx (`source-separation-models`, 67 MB).

## Cómo va (`motor/SeparacionUvr.kt`)

Igual que `sherpa-onnx/csrc/offline-source-separation-uvr-impl.h`:

1. La canción a estéreo y 44,1 kHz (`SepararVoz.decodificarEstereo`).
2. Tramos de `hop · (dim_t − 1)` = 261 120 muestras, solapados `n_fft / 2` a cada lado; de cada uno vale el centro.
3. STFT compleja de cada canal (Hann periódica, `n_fft` 6144, `hop` 1024, `center` con reflejo; `motor/Stft.kt`,
   con una FFT de 3·2048); las 3072 primeras bandas, en un tensor `[1, 4, 3072, 256]`.
4. El modelo devuelve el espectro de la voz; la STFT inversa da la voz; **el karaoke es la canción menos la voz**.
5. «Separación fina» (Ajustes): también con la canción cambiada de signo, y la voz es la media (el «denoise» de UVR).

Se comprobó con la canción de ejemplo de sherpa-onnx: la voz de LetraSync (Kotlin, con el modelo de verdad) y la de
sherpa-onnx coinciden con **76 dB de SNR** (es decir, son la misma).

## En el teléfono

ONNX Runtime ya venía con sherpa-onnx (1.27.1). Para no meter un segundo onnxruntime (sherpa exige exactamente el
suyo), el modelo se llama desde un puente propio en C (`app/src/main/cpp/separador.c`) con la API de C de
onnxruntime, compilado aparte (`herramientas/compilar-separador.sh`) y guardado en `jniLibs` como las demás.
Las pistas quedan en `files/pistas/<canción>/voz.wav` y `karaoke.wav`.
