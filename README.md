# LetraSync Pro

La letra de una canción, sincronizada con su audio, en el teléfono y sin conexión.

- **Sincronizar sola**: el reconocedor de voz oye la canción y la letra pegada se alinea con lo oído.
- **A toques**: un toque al empezar cada línea, con la canción sonando.
- **Karaoke**: la línea que suena, resaltada; tocar salta a ella; mantener le pone el tiempo de ahora.
- **LRC**: compartir o guardar la letra en LRC estándar.
- **Integrada con PinDesk**, como Escáner Pro Max: PinDesk le manda la canción y su letra y recibe la letra
  sincronizada. Ver [docs/integracion.md](docs/integracion.md).

Compilar: `./gradlew assembleRelease` (el APK para repartir: `sh herramientas/apk-para-repartir.sh`). En GitHub,
la acción «APK de prueba» lo publica como prerelease.
