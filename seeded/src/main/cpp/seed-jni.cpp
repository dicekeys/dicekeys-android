#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {


JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Seed_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Seed>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Seed_keyDerivationOptionsJsonGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Seed>(env, thiz)->keyDerivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_Seed_seedBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<Seed>(env, thiz)->seedBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_Seed_deleteNativeObjectPtrJNI(
  JNIEnv *env,
jobject thiz
) {
  try {
    delete getNativeObjectPtr<Seed>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Seed_constructFromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring seed_json
) {
  try {
    return (jlong) new Seed(
      jstringToString(env, seed_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Seed_constructJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new Seed(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Seed_constructJNI___3BLjava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jbyteArray seed_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new Seed(
      jbyteArrayToVector(env, seed_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
