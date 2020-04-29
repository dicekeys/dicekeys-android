#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_toJson(
  JNIEnv *env,
  jobject thiz,
  jboolean minimizeSizeByRemovingTheSignatureVerificationKeyBytesWhichCanBeRegeneratedLater
) {
   try {
     return stringToJString(env,
       getNativeObjectPtr<SigningKey>(env, thiz)->toJson(
         minimizeSizeByRemovingTheSignatureVerificationKeyBytesWhichCanBeRegeneratedLater
       )
    );
 } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_generateSignature(
  JNIEnv *env,
  jobject thiz,
  jbyteArray message
) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<SigningKey>(env, thiz
    )->generateSignature(
          jbyteArrayToVector(env, message)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_derivationOptionsJsonGetterJNI(
  JNIEnv *env,
    jobject thiz
  ) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<SigningKey>(env, thiz)->derivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_signingKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    return sodiumBufferToJbyteArray(env,
       getNativeObjectPtr<SigningKey>(env, thiz)->signingKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_signatureVerificationKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return byteVectorToJbyteArray(env,
      getNativeObjectPtr<SigningKey>(env, thiz)->getSignatureVerificationKeyBytes()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_getSignatureVerificationKeyJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    return (jlong) new SignatureVerificationKey(
      getNativeObjectPtr<SigningKey>(env, thiz)->getSignatureVerificationKey()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0;
  }}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    delete getNativeObjectPtr<SigningKey>(env, thiz);
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
   jstring json
 ) {
  try {
    return (jlong) new SigningKey(SigningKey::fromJson(
      jstringToString(env, json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_deriveFromSeedJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new SigningKey(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_constructJNI___3B_3BLjava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jbyteArray signing_key_bytes,
  jbyteArray signature_verification_key_bytes,
  jstring key_derivation_options_json) {
  try {
    return (jlong) new SigningKey(
      jbyteArrayToSodiumBuffer(env, signing_key_bytes),
      jbyteArrayToVector(env, signature_verification_key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_constructJNI___3BLjava_lang_String_2(
  JNIEnv *env,
    jclass clazz,
    jbyteArray signing_key_bytes,
    jstring key_derivation_options_json) {
  try {
    return (jlong) new SigningKey(
      jbyteArrayToSodiumBuffer(env, signing_key_bytes),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SigningKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SigningKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new SigningKey(SigningKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern C

