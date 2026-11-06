// El puente con ONNX Runtime para separar la voz (UVR MDX-Net). Lo justo: cargar el modelo, leer sus metadatos y
// pasarle un tensor [n, 4, dim_f, dim_t] de floats. Usa la libonnxruntime.so que ya trae sherpa-onnx (1.27.1),
// con su API de C: así no hay dos onnxruntime en la app. Se compila con herramientas/compilar-separador.sh.
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include "onnxruntime_c_api.h"

#define TAG "LetraSyncSeparador"

typedef struct {
    OrtEnv *env;
    OrtSession *sesion;
    OrtSessionOptions *opciones;
    OrtAllocator *asignador;
    char *entrada;
    char *salida;
} Separador;

static const OrtApi *api(void) { return OrtGetApiBase()->GetApi(ORT_API_VERSION); }

static int mal(JNIEnv *e, OrtStatus *s) {
    if (!s) return 0;
    const char *m = api()->GetErrorMessage(s);
    __android_log_print(ANDROID_LOG_ERROR, TAG, "%s", m);
    jclass c = (*e)->FindClass(e, "java/lang/IllegalStateException");
    (*e)->ThrowNew(e, c, m);
    api()->ReleaseStatus(s);
    return 1;
}

static void liberar(Separador *p) {
    if (!p) return;
    const OrtApi *a = api();
    if (p->entrada && p->asignador) a->AllocatorFree(p->asignador, p->entrada);
    if (p->salida && p->asignador) a->AllocatorFree(p->asignador, p->salida);
    if (p->sesion) a->ReleaseSession(p->sesion);
    if (p->opciones) a->ReleaseSessionOptions(p->opciones);
    if (p->env) a->ReleaseEnv(p->env);
    free(p);
}

JNIEXPORT jlong JNICALL Java_com_letrasyncpro_motor_Separador_crear(JNIEnv *e, jclass c, jstring ruta, jint hilos) {
    const OrtApi *a = api();
    Separador *p = calloc(1, sizeof(Separador));
    const char *r = (*e)->GetStringUTFChars(e, ruta, NULL);
    int fallo = mal(e, a->CreateEnv(ORT_LOGGING_LEVEL_WARNING, TAG, &p->env))
        || mal(e, a->CreateSessionOptions(&p->opciones))
        || mal(e, a->SetIntraOpNumThreads(p->opciones, hilos))
        || mal(e, a->SetSessionGraphOptimizationLevel(p->opciones, ORT_ENABLE_ALL))
        || mal(e, a->CreateSession(p->env, r, p->opciones, &p->sesion))
        || mal(e, a->GetAllocatorWithDefaultOptions(&p->asignador))
        || mal(e, a->SessionGetInputName(p->sesion, 0, p->asignador, &p->entrada))
        || mal(e, a->SessionGetOutputName(p->sesion, 0, p->asignador, &p->salida));
    (*e)->ReleaseStringUTFChars(e, ruta, r);
    if (fallo) { liberar(p); return 0; }
    return (jlong) (intptr_t) p;
}

JNIEXPORT jstring JNICALL Java_com_letrasyncpro_motor_Separador_metadato(JNIEnv *e, jclass c, jlong h, jstring clave) {
    Separador *p = (Separador *) (intptr_t) h;
    const OrtApi *a = api();
    OrtModelMetadata *m = NULL;
    if (mal(e, a->SessionGetModelMetadata(p->sesion, &m))) return NULL;
    const char *k = (*e)->GetStringUTFChars(e, clave, NULL);
    char *v = NULL;
    OrtStatus *s = a->ModelMetadataLookupCustomMetadataMap(m, p->asignador, k, &v);
    (*e)->ReleaseStringUTFChars(e, clave, k);
    jstring r = NULL;
    if (!mal(e, s) && v) { r = (*e)->NewStringUTF(e, v); a->AllocatorFree(p->asignador, v); }
    a->ReleaseModelMetadata(m);
    return r;
}

JNIEXPORT jfloatArray JNICALL Java_com_letrasyncpro_motor_Separador_ejecutar(JNIEnv *e, jclass c, jlong h, jfloatArray datos, jlongArray forma) {
    Separador *p = (Separador *) (intptr_t) h;
    const OrtApi *a = api();
    jsize n = (*e)->GetArrayLength(e, datos);
    jsize nd = (*e)->GetArrayLength(e, forma);
    jlong dims[8];
    (*e)->GetLongArrayRegion(e, forma, 0, nd, dims);
    int64_t d64[8];
    for (int i = 0; i < nd; i++) d64[i] = dims[i];
    jfloat *x = (*e)->GetFloatArrayElements(e, datos, NULL);
    OrtMemoryInfo *mem = NULL;
    OrtValue *ent = NULL, *sal = NULL;
    jfloatArray r = NULL;
    if (!mal(e, a->CreateCpuMemoryInfo(OrtArenaAllocator, OrtMemTypeDefault, &mem))
        && !mal(e, a->CreateTensorWithDataAsOrtValue(mem, x, (size_t) n * sizeof(float), d64, nd, ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, &ent))) {
        const char *ins[1] = { p->entrada };
        const char *outs[1] = { p->salida };
        if (!mal(e, a->Run(p->sesion, NULL, ins, (const OrtValue *const *) &ent, 1, outs, 1, &sal))) {
            float *y = NULL;
            OrtTensorTypeAndShapeInfo *info = NULL;
            size_t cuantos = 0;
            if (!mal(e, a->GetTensorMutableData(sal, (void **) &y)) && !mal(e, a->GetTensorTypeAndShape(sal, &info))
                && !mal(e, a->GetTensorShapeElementCount(info, &cuantos))) {
                r = (*e)->NewFloatArray(e, (jsize) cuantos);
                if (r) (*e)->SetFloatArrayRegion(e, r, 0, (jsize) cuantos, y);
            }
            if (info) a->ReleaseTensorTypeAndShapeInfo(info);
        }
    }
    if (sal) a->ReleaseValue(sal);
    if (ent) a->ReleaseValue(ent);
    if (mem) a->ReleaseMemoryInfo(mem);
    (*e)->ReleaseFloatArrayElements(e, datos, x, JNI_ABORT);
    return r;
}

JNIEXPORT void JNICALL Java_com_letrasyncpro_motor_Separador_liberar(JNIEnv *e, jclass c, jlong h) {
    liberar((Separador *) (intptr_t) h);
}
