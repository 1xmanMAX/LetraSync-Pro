# LetraSync Pro

La letra de una canción, sincronizada con su audio, en el teléfono y sin conexión.

**Al estilo de Apple** (0.5.0): colores y controles de iOS, la letra como en Apple Music. Tres pasos en un control
segmentado:

1. **Letra**: Pegar · Buscar (LRCLib) · Archivo (LRC, SRT, texto).
2. **Sincronizar**:
   - **En la onda** (la recomendada): la frase grande arriba y la onda de la canción debajo. La app propone dónde
     empieza la voz y lo hace sonar; «Aceptar» o tocar en la onda donde empieza de verdad (deslizar para moverse,
     pellizcar para acercar). Se guarda con un **anticipo** (0,3 s por defecto, en Ajustes): la letra aparece un
     poco antes de cantarse. En la onda, la franja antes de cada marca es ese anticipo.
   - **Al ritmo** y **Palabra a palabra**: a toques mientras suena (también con el anticipo y el retardo del dedo).
   - **Automática**.
3. **Revisar**: la onda con las líneas y, al tocar una, su tiempo grande con −0,1 / +0,1 (mantener repite), «Aquí»,
   «Oír» y «Bucle». **Mover una línea nunca mueve otra** ni la deja cruzarse con sus vecinas.

Lo poco frecuente va en «Más».

- **Sincronizar sola**: el reconocedor de voz oye la canción y la letra pegada se alinea con lo oído (tres
  tamaños de reconocedor en Ajustes: rápido, equilibrado, preciso).
- **A toques**: un toque al empezar cada línea, con la canción sonando a 0.5×, 0.75× o 1× (los tiempos son
  siempre los de la canción).
- **Afinar**: tocar una línea la elige: −0.5 / −0.1 / +0.1 / +0.5 s, «Ahora», «Oír» (desde 2 s antes), quitar
  el tiempo y pasar a la anterior o la siguiente. Mantener una línea le pone el tiempo de ahora.
- **Toda la letra**: moverla a la vez (adelantada o atrasada), rellenar las que quedaron sin tiempo
  repartiéndolas entre sus vecinas, quitar todos los tiempos.
- **Revisar**: en ámbar las que se pusieron a ojo; en rojo las que van antes que la anterior. Deshacer y rehacer.
- **Sacar**: LRC con centésimas y subtítulos SRT, para guardar o compartir.
- **Biblioteca**: las canciones recientes, con la letra como se dejó.
- **La onda** (como la línea de tiempo de Composer): la canción dibujada con las líneas encima; arrastrar el
  fondo corre la canción, arrastrar una marca mueve su línea y, con el imán, se pega al arranque del sonido.
- **Por palabras** (como Chronograph y Voxen): a toques palabra a palabra, karaoke con la palabra que suena y
  LRC mejorado (`<mm:ss.xx>`). También «Guardar LRC solo por líneas».
- **Bucle** de una línea, **mover también las de detrás** (como la edición por lotes de LRC Editor) y
  **retardo al tocar** (lo que tarda el dedo; Ajustes).
- **Buscar la letra en LRCLib** (lrclib.net, como Chronograph), **abrir SRT**, **datos de la canción**
  (`[ti:] [ar:] [al:] [by:]`) y **vista previa** a pantalla completa.

Ideas tomadas de: [LRC Editor](https://github.com/Spikatrix/LRC-Editor), [lrc-maker](https://github.com/magic-akari/lrc-maker),
[Chronograph](https://github.com/Dzheremi2/Chronograph), [Composer](https://github.com/better-lyrics/composer) y
[Voxen](https://github.com/Raqhael-ux/Voxen-LRC-Editor).
- **Integrada con PinDesk**, como Escáner Pro Max: PinDesk le manda la canción y su letra y recibe la letra
  sincronizada. Ver [docs/integracion.md](docs/integracion.md).

Compilar: `./gradlew assembleRelease` (el APK para repartir: `sh herramientas/apk-para-repartir.sh`). En GitHub,
la acción «APK de prueba» lo publica como prerelease.
