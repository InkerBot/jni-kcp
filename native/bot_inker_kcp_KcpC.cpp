#include <cstring>
#include "bot_inker_kcp_KcpC.h"
#include "ikcp.h"

thread_local JNIEnv *threadLocalJniEnv;

int output_handle(const char *buf, int len, ikcpcb *kcp, void *user) {
  JNIEnv *jniEnv = threadLocalJniEnv;
  jclass ikcpcb_class = jniEnv->FindClass("bot/inker/kcp/IKcpcb");
  jmethodID jmethod_id = jniEnv->GetMethodID(ikcpcb_class, "output", "(Ljava/nio/ByteBuffer;I)I");
  jobject buffer = jniEnv->NewDirectByteBuffer((void *) buf, len);
  return jniEnv->CallIntMethod((jobject) user, jmethod_id, buffer, len);
}

void log_handle(const char *log, struct IKCPCB *kcp, void *user) {
  JNIEnv *jniEnv = threadLocalJniEnv;
  jclass ikcpcb_class = jniEnv->FindClass("bot/inker/kcp/IKcpcb");
  jmethodID jmethod_id = jniEnv->GetMethodID(ikcpcb_class, "writelog", "(Ljava/nio/ByteBuffer;)V");
  jobject buffer = jniEnv->NewDirectByteBuffer((void*) log, (jlong) strlen(log));
  jniEnv->CallVoidMethod((jobject) user, jmethod_id, buffer);
}

inline long get_buffer_address(jobject buf) {
  JNIEnv *jniEnv = threadLocalJniEnv;
  jclass bufferClass = jniEnv->FindClass("java/nio/Buffer");
  jfieldID bufferAddressField = jniEnv->GetFieldID(bufferClass, "address", "J");
  return jniEnv->GetLongField(buf, bufferAddressField);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    get_jni_output_method
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_bot_inker_kcp_KcpC_get_1jni_1output_1method
  (JNIEnv *jniEnv, jclass) {
  threadLocalJniEnv=jniEnv;
  return (jlong) (int (*)(const char *, int, ikcpcb *, void *)) &output_handle;
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    get_jni_log_method
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_bot_inker_kcp_KcpC_get_1jni_1log_1method
    (JNIEnv * jniEnv, jclass) {
  threadLocalJniEnv = jniEnv;
  return (jlong) (void (*)(const char *, struct IKCPCB *, void *)) &log_handle;
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    new_global_reference
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_bot_inker_kcp_KcpC_new_1global_1reference
  (JNIEnv *jniEnv, jclass, jobject lobj) {
  threadLocalJniEnv=jniEnv;
  return (jlong) jniEnv->NewGlobalRef(lobj);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    delete_global_reference
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_delete_1global_1reference
  (JNIEnv *jniEnv, jclass, jlong gref) {
  threadLocalJniEnv=jniEnv;
  jniEnv->DeleteGlobalRef((jobject) gref);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    get_buffer_address
 * Signature: (Ljava/nio/Buffer;)J
 */
JNIEXPORT jlong JNICALL Java_bot_inker_kcp_KcpC_get_1buffer_1address
  (JNIEnv *jniEnv, jclass, jobject buf) {
  threadLocalJniEnv=jniEnv;
  return get_buffer_address(buf);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_create
 * Signature: (IJ)J
 */
JNIEXPORT jlong JNICALL Java_bot_inker_kcp_KcpC_ikcp_1create
  (JNIEnv *jniEnv, jclass, jint conv, jlong user) {
  threadLocalJniEnv=jniEnv;
  return (jlong) ikcp_create(conv, (void*) user);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1release
  (JNIEnv *jniEnv, jclass, jlong kcp) {
  threadLocalJniEnv=jniEnv;
  ikcp_release((ikcpcb*) kcp);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_setoutput
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1setoutput
  (JNIEnv *jniEnv, jclass, jlong kcp, jlong output) {
  threadLocalJniEnv=jniEnv;
  ikcp_setoutput((ikcpcb*) kcp, (int (*)(const char *, int, ikcpcb *, void *)) output);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_setlog
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1setlog
    (JNIEnv *jniEnv, jclass, jlong kcp, jlong log) {
  threadLocalJniEnv=jniEnv;
  ((ikcpcb*) kcp)->writelog = (void (*)(const char *, struct IKCPCB *, void *)) log;
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_recv
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1recv
  (JNIEnv *jniEnv, jclass, jlong kcp, jlong buffer, jint len) {
  threadLocalJniEnv=jniEnv;
  return ikcp_recv((ikcpcb*) kcp, (char *) buffer, len);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_send
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1send
  (JNIEnv *jniEnv, jclass, jlong kcp, jlong buffer, jint len) {
  threadLocalJniEnv=jniEnv;
  return ikcp_send((ikcpcb*) kcp, (const char *) buffer, len);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_update
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1update
  (JNIEnv *jniEnv, jclass, jlong kcp, jint current) {
  threadLocalJniEnv=jniEnv;
  ikcp_update((ikcpcb*) kcp, current);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_check
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1check
  (JNIEnv *jniEnv, jclass, jlong kcp, jint current) {
  threadLocalJniEnv=jniEnv;
  return (jint) ikcp_check((ikcpcb*) kcp, current);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_input
 * Signature: (JJJ)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1input
  (JNIEnv *jniEnv, jclass, jlong kcp, jlong data, jlong size) {
  threadLocalJniEnv=jniEnv;
  return ikcp_input((ikcpcb*) kcp, (const char *) data, size);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_flush
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1flush
  (JNIEnv *jniEnv, jclass, jlong kcp) {
  threadLocalJniEnv=jniEnv;
  ikcp_flush((ikcpcb*) kcp);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_peeksize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1peeksize
  (JNIEnv *jniEnv, jclass, jlong kcp) {
  threadLocalJniEnv=jniEnv;
  return ikcp_peeksize((ikcpcb*) kcp);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_setmtu
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1setmtu
  (JNIEnv *jniEnv, jclass, jlong kcp, jint mtu) {
  threadLocalJniEnv=jniEnv;
  return ikcp_setmtu((ikcpcb*) kcp, mtu);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_wndsize
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1wndsize
  (JNIEnv *jniEnv, jclass, jlong kcp, jint sndwnd, jint rcvwnd) {
  threadLocalJniEnv=jniEnv;
  return ikcp_wndsize((ikcpcb*) kcp, sndwnd, rcvwnd);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_waitsnd
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1waitsnd
  (JNIEnv *jniEnv, jclass, jlong kcp) {
  threadLocalJniEnv=jniEnv;
  return ikcp_waitsnd((ikcpcb*) kcp);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_nodelay
 * Signature: (JIIII)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1nodelay
  (JNIEnv *jniEnv, jclass, jlong kcp, jint nodelay, jint interval, jint resend, jint nc) {
  threadLocalJniEnv=jniEnv;
  return ikcp_nodelay((ikcpcb*) kcp, nodelay, interval, resend, nc);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_allocator
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_bot_inker_kcp_KcpC_ikcp_1allocator
  (JNIEnv *jniEnv, jclass, jlong new_malloc, jlong new_free) {
  threadLocalJniEnv=jniEnv;
  ikcp_allocator((void* (*)(size_t)) new_malloc, (void (*)(void*)) new_free);
}

/*
 * Class:     bot_inker_kcp_KcpC
 * Method:    ikcp_getconv
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_bot_inker_kcp_KcpC_ikcp_1getconv
  (JNIEnv *jniEnv, jclass, jlong kcp) {
  threadLocalJniEnv=jniEnv;
  return (jint) ikcp_getconv((ikcpcb*) kcp);
}