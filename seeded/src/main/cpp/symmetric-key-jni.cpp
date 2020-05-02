#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_unseal(
  JNIEnv *env,
  jobject thiz,
  jbyteArray ciphertext,
  jstring unsealing_instructions
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->unseal(
        jbyteArrayToVector(env, ciphertext),
        jstringToString(env, unsealing_instructions)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_sealToCiphertextOnly(
  JNIEnv *env,
  jobject thiz,
  jbyteArray plaintext,
  jstring unsealing_instructions
) {
  try {
    return byteVectorToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->sealToCiphertextOnly(
        jbyteArrayToSodiumBuffer(env, plaintext),
        jstringToString(env, unsealing_instructions)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_sealJNI(
  JNIEnv *env,
  jobject thiz,
  jbyteArray plaintext,
  jstring unsealing_instructions
) {
  try {
    return (jlong) new PackagedSealedMessage(
      getNativeObjectPtr<SymmetricKey>(env, thiz)->seal(
        jbyteArrayToSodiumBuffer(env, plaintext),
        jstringToString(env, unsealing_instructions)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }}


JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
       getNativeObjectPtr<SymmetricKey>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_derivationOptionsJsonGetterJNI(
    JNIEnv *env,
    jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->derivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_keyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->keyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}
JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    delete getNativeObjectPtr<SymmetricKey>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }}
JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring symmetric_key_json
) {
  try {
    return (jlong) new SymmetricKey(SymmetricKey::fromJson(
      jstringToString(env, symmetric_key_json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}
JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_deriveFromSeedJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new SymmetricKey(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}
JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_constructJNI___3BLjava_lang_String_2(
    JNIEnv *env,
    jclass clazz,
    jbyteArray key_bytes,
    jstring key_derivation_options_json
) {
  try {
    return (jlong) new SymmetricKey(
      jbyteArrayToVector(env, key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new SymmetricKey(SymmetricKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
