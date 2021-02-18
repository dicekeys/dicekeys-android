#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {


JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Password_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Password>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Password_recipeGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<Password>(env, thiz)->recipe
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_Password_passwordGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(
      env,
      getNativeObjectPtr<Password>(env, thiz)->password
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_Password_deleteNativeObjectPtrJNI(
  JNIEnv *env,
jobject thiz
) {
  try {
    delete getNativeObjectPtr<Password>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Password_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring seed_json
) {
  try {
    return (jlong) new Password(Password::fromJson(
      jstringToString(env, seed_json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Password_deriveFromSeedJNI(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json,
  jstring word_list_as_string
) {
  try {
    return (jlong) new Password(Password::deriveFromSeedAndWordList(
        jstringToString(env, seed_string),
        jstringToString(env, key_derivation_options_json),
        jstringToString(env, word_list_as_string)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_Password_constructJNI(
  JNIEnv *env,
  jclass clazz,
  jstring password,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new Password(
      jstringToString(env, password),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_Password_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<Password>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return nullptr;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_Password_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new Password(Password::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
