#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {


JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Secret_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Secret>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Secret_recipeGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Secret>(env, thiz)->recipe
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_Secret_secretBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<Secret>(env, thiz)->secretBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_Secret_deleteNativeObjectPtrJNI(
  JNIEnv *env,
jobject thiz
) {
  try {
    delete getNativeObjectPtr<Secret>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Secret_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring seed_json
) {
  try {
    return (jlong) new Secret(Secret::fromJson(
      jstringToString(env, seed_json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Secret_deriveFromSeedJNI(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new Secret(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Secret_constructJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray seed_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new Secret(
      jbyteArrayToVector(env, seed_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_Secret_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<Secret>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_Secret_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new Secret(Secret::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
