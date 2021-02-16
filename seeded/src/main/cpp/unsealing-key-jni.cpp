#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_unseal(
  JNIEnv *env, jobject thiz,
  jbyteArray ciphertext,
  jstring unsealing_instructions) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<UnsealingKey>(env, thiz)->unseal(
          jbyteArrayToVector(env, ciphertext),
          jstringToString(env, unsealing_instructions)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<UnsealingKey>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_recipeGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<UnsealingKey>(env, thiz)->recipe
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_sealingKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return byteVectorToJbyteArray(env,
       getNativeObjectPtr<UnsealingKey>(env, thiz)->sealingKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_unsealingKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<UnsealingKey>(env, thiz)->unsealingKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz) {
  try {
    delete getNativeObjectPtr<UnsealingKey>(env, thiz);
  } catch (...) {
  throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_getSealingKeyPtrJNI(
    JNIEnv *env,
    jobject thiz) {
  try {
    const UnsealingKey *UnsealingKeyPtr =
      getNativeObjectPtr<UnsealingKey>(env, thiz);
    return (jlong) new SealingKey(
      UnsealingKeyPtr->sealingKeyBytes,
      UnsealingKeyPtr->recipe
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring json
) {
  try {
    return (jlong) new UnsealingKey(UnsealingKey::fromJson(
      jstringToString(env, json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_deriveFromSeedJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json) {
  try {
    return (jlong) new UnsealingKey(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_constructJNI___3B_3BLjava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jbyteArray secret_key_bytes,
  jbyteArray public_key_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new UnsealingKey(
      jbyteArrayToSodiumBuffer(env, secret_key_bytes),
      jbyteArrayToVector(env, public_key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<UnsealingKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_UnsealingKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new UnsealingKey(UnsealingKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern C

