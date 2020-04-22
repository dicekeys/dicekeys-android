#include <jni.h>
#include <string>
#include <exception>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jlong JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_fromJsonJNI(
    JNIEnv* env,
    jclass cls,
    jstring _publicKeyAsJson
) {
  try {
    return (jlong) new PublicKey(PublicKey::fromJson(
      jstringToString(env, _publicKeyAsJson)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL Java_org_dicekeys_crypto_seeded_PublicKey_constructFromJsonJNI_00024seeded_1debug(
  JNIEnv* env,
  jclass cls,
  jstring _publicKeyAsJson
) {
  return Java_org_dicekeys_crypto_seeded_PublicKey_fromJsonJNI(env, cls, _publicKeyAsJson);
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

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_sealJNI(
  JNIEnv *env,
  jobject obj,
  jbyteArray message,
  jstring post_decryption_instructions_json
) {
  try {
    return (jlong) new PackagedSealedMessage(
      getNativeObjectPtr<PublicKey>(env, obj)->seal(
        jbyteArrayToSodiumBuffer(env, message),
        jstringToString(env, post_decryption_instructions_json)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
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


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<PublicKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PublicKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new PublicKey(PublicKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
