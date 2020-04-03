#include <string>
#include <jni.h>
#include <exception>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jlong JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_constructFromJsonJNI(
    JNIEnv* env,
    jclass cls,
    jstring _publicKeyAsJson
) {
  try {
    return (jlong) new PublicKey(
      jstringToString(env, _publicKeyAsJson)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_constructJNI(
    JNIEnv *env, jclass clazz,
    jbyteArray key_bytes,
    jstring key_derivation_options_json
) {
  try {
    return (jlong) new PublicKey(
      jbyteArrayToVector(env, key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


JNIEXPORT void  JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_deleteNativeObjectPtrJNI(JNIEnv *env, jobject obj) {
  try {
    deleteNativeObjectPtr<PublicKey>(env, obj);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}


// keyBytesGetterJNI
JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_keyBytesGetterJNI(
    JNIEnv* env,
    jobject obj
) {
  try {
    return byteVectorToJbyteArray(env,
      getNativeObjectPtr<PublicKey>(env, obj)->getPublicKeyBytes()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_keyDerivationOptionsJsonGetterJNI(
    JNIEnv* env,
    jobject obj
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PublicKey>(env, obj)->getKeyDerivationOptionsJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_seal(
  JNIEnv *env,
  jobject obj,
  jbyteArray message,
  jstring post_decryption_instructions_json
) {
  try {
    return byteVectorToJbyteArray(env,
      getNativeObjectPtr<PublicKey>(env, obj)->seal(
        jbyteArrayToSodiumBuffer(env, message),
        jstringToString(env, post_decryption_instructions_json)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_toJson(JNIEnv *env, jobject obj) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PublicKey>(env, obj)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
