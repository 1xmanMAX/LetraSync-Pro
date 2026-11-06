# sherpa-onnx: su biblioteca nativa busca estas clases por su nombre.
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** { *; }

# El separador de voz: su biblioteca nativa busca estos métodos por su nombre.
-keep class com.letrasyncpro.motor.Separador { native <methods>; *; }
