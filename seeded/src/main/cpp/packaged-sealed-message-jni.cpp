#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_unsealingInstructionsGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PackagedSealedMessage>(env, thiz)->unsealingInstructions
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_derivationOptionsJsonGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PackagedSealedMessage>(env, thiz)->derivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_ciphertextGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return byteVectorToJbyteArray(
      env,
      getNativeObjectPtr<PackagedSealedMessage>(env, thiz)->ciphertext
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    delete getNativeObjectPtr<PackagedSealedMessage>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_toJson(JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PackagedSealedMessage>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<PackagedSealedMessage>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }

}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray packaged_sealed_message_in_serialized_binary_form
) {
  try {
    return (jlong) new PackagedSealedMessage(PackagedSealedMessage::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, packaged_sealed_message_in_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring packaged_sealed_message_in_json_format
) {
  try{
    return (jlong) new PackagedSealedMessage(PackagedSealedMessage::fromJson(
      jstringToString(env, packaged_sealed_message_in_json_format)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
  Java_org_dicekeys_crypto_seeded_PackagedSealedMessage_constructJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray ciphertext,
  jstring key_derivation_options_json,
  jstring unsealing_instructions
) {
  try {
    return (jlong) new PackagedSealedMessage(
      jbyteArrayToVector(env, ciphertext),
      jstringToString(env, key_derivation_options_json),
      jstringToString(env, unsealing_instructions)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}


} // extern "C"
