#include <jni.h>
#include <string>
#include <exception>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jlong JNICALL Java_org_dicekeys_crypto_seeded_SealingKey_fromJsonJNI(
    JNIEnv* env,
    jclass cls,
    jstring _sealingKeyAsJson
) {
  try {
    return (jlong) new SealingKey(SealingKey::fromJson(
      jstringToString(env, _sealingKeyAsJson)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL Java_org_dicekeys_crypto_seeded_SealingKey_constructFromJsonJNI_00024seeded_1debug(
  JNIEnv* env,
  jclass cls,
  jstring _sealingKeyAsJson
) {
  return Java_org_dicekeys_crypto_seeded_SealingKey_fromJsonJNI(env, cls, _sealingKeyAsJson);
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SealingKey_constructJNI(
    JNIEnv *env, jclass clazz,
    jbyteArray key_bytes,
    jstring key_derivation_options_json
) {
  try {
    return (jlong) new SealingKey(
      jbyteArrayToVector(env, key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


JNIEXPORT void  JNICALL Java_org_dicekeys_crypto_seeded_SealingKey_deleteNativeObjectPtrJNI(JNIEnv *env, jobject obj) {
  try {
    deleteNativeObjectPtr<SealingKey>(env, obj);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}


// keyBytesGetterJNI
JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_crypto_seeded_SealingKey_keyBytesGetterJNI(
    JNIEnv* env,
    jobject obj
) {
  try {
    return byteVectorToJbyteArray(env,
      getNativeObjectPtr<SealingKey>(env, obj)->getSealingKeyBytes()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL Java_org_dicekeys_crypto_seeded_SealingKey_derivationOptionsJsonGetterJNI(
    JNIEnv* env,
    jobject obj
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<SealingKey>(env, obj)->getDerivationOptionsJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SealingKey_sealJNI(
  JNIEnv *env,
  jobject obj,
  jbyteArray message,
  jstring unsealing_instructions
) {
  try {
    return (jlong) new PackagedSealedMessage(
      getNativeObjectPtr<SealingKey>(env, obj)->seal(
        jbyteArrayToSodiumBuffer(env, message),
        jstringToString(env, unsealing_instructions)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SealingKey_toJson(JNIEnv *env, jobject obj) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<SealingKey>(env, obj)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SealingKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SealingKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SealingKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new SealingKey(SealingKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern "C"
