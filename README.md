# LetraSync Pro

La letra de una canción, sincronizada con su audio, en el teléfono y sin conexión.

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
- **Integrada con PinDesk**, como Escáner Pro Max: PinDesk le manda la canción y su letra y recibe la letra
  sincronizada. Ver [docs/integracion.md](docs/integracion.md).

Compilar: `./gradlew assembleRelease` (el APK para repartir: `sh herramientas/apk-para-repartir.sh`). En GitHub,
la acción «APK de prueba» lo publica como prerelease.
