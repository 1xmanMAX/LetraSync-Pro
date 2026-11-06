#!/bin/sh
# **El puente con ONNX Runtime para separar la voz** (app/src/main/cpp/separador.c), compilado para arm64.
# Se compila a mano y se guarda en jniLibs (como las bibliotecas de sherpa-onnx), así la compilación normal no
# necesita el NDK. Las cabeceras son las de onnxruntime v1.27.1, la misma versión que trae sherpa-onnx.
#
# Uso: ANDROID_HOME=… herramientas/compilar-separador.sh
set -e
cd "$(dirname "$0")/.."
NDK=$(ls -d "${ANDROID_HOME:-/root/android-sdk}"/ndk/* | sort -V | tail -1)
CC="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android29-clang"
LIBS=app/src/main/jniLibs/arm64-v8a
"$CC" -O2 -shared -fPIC -Wall -Iapp/src/main/cpp/onnxruntime app/src/main/cpp/separador.c \
  -L"$LIBS" -lonnxruntime -llog -Wl,-soname,libletrasync-separador.so -Wl,-z,max-page-size=16384 \
  -o "$LIBS/libletrasync-separador.so"
"$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip" "$LIBS/libletrasync-separador.so"
ls -la "$LIBS/libletrasync-separador.so"
