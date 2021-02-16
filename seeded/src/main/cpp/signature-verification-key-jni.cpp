#include <string>
#include <jni.h>
#include <exception>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_fromJsonJNI(
  JNIEnv *env,
  jclass cls,
  jstring signatureVerificationKeyAsJson
) {
  try {
    return (jlong) new SignatureVerificationKey(SignatureVerificationKey::fromJson(
      jstringToString(env, signatureVerificationKeyAsJson)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_constructJNI(
  JNIEnv *env, jclass clazz,
  jbyteArray key_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new SignatureVerificationKey(
      jbyteArrayToVector(env, key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


JNIEXPORT void  JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject obj
) {
  try {
    deleteNativeObjectPtr<SignatureVerificationKey>(env, obj);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_keyBytesGetterJNI(
  JNIEnv *env,
  jobject obj
) {
  try {
    return byteVectorToJbyteArray(env,
                                  getNativeObjectPtr<SignatureVerificationKey>(env,
                                                                               obj)->getKeyBytes()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_recipeGetterJNI(
  JNIEnv *env,
  jobject obj
) {
  try {
    return stringToJString(env,
                           getNativeObjectPtr<SignatureVerificationKey>(env,
                                                                        obj)->getRecipeJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jboolean JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_verifySignature(
  JNIEnv *env,
  jobject obj,
  jbyteArray message,
  jbyteArray signature
) {
  try {
    return static_cast<jboolean>(
      getNativeObjectPtr<SignatureVerificationKey>(env, obj)->verify(
        jbyteArrayToSodiumBuffer(env, message),
        jbyteArrayToVector(env, signature)
      ));
  } catch (...) {
    // throwCppExceptionAsJavaException(env, std::current_exception());
    return false;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_toJson(JNIEnv *env, jobject obj) {
  try {
    return stringToJString(env,
                           getNativeObjectPtr<SignatureVerificationKey>(env, obj)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SignatureVerificationKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SignatureVerificationKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new SignatureVerificationKey(SignatureVerificationKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern c
