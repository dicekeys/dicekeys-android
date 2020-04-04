#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_unseal(
  JNIEnv *env, jobject thiz,
  jbyteArray ciphertext,
  jstring post_decryption_instructions_json) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz)->unseal(
          jbyteArrayToVector(env, ciphertext),
          jstringToString(env, post_decryption_instructions_json)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_keyDerivationOptionsJsonGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz)->keyDerivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_publicKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return byteVectorToJbyteArray(env,
       getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz)->publicKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_secretKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz)->secretKey
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz) {
  try {
    delete getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz);
  } catch (...) {
  throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_getPublicKeyPtrJNI(
    JNIEnv *env,
    jobject thiz) {
  try {
    const PublicPrivateKeyPair *publicPrivateKeyPairPtr =
      getNativeObjectPtr<PublicPrivateKeyPair>(env, thiz);
    return (jlong) new PublicKey(
      publicPrivateKeyPairPtr->publicKeyBytes,
      publicPrivateKeyPairPtr->keyDerivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_constructFromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring json
) {
  try {
    return (jlong) new PublicPrivateKeyPair(
      jstringToString(env, json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_constructJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json) {
  try {
    return (jlong) new PublicPrivateKeyPair(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicPrivateKeyPair_constructJNI___3B_3BLjava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jbyteArray secret_key_bytes,
  jbyteArray public_key_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new PublicPrivateKeyPair(
      jbyteArrayToSodiumBuffer(env, secret_key_bytes),
      jbyteArrayToVector(env, public_key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern C

