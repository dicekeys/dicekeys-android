#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_unseal(
  JNIEnv *env, jobject thiz,
  jbyteArray ciphertext,
  jstring post_decryption_instructions_json) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<PrivateKey>(env, thiz)->unseal(
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
Java_org_dicekeys_crypto_seeded_PrivateKey_toJson(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PrivateKey>(env, thiz)->toJson()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jstring JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_derivationOptionsJsonGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<PrivateKey>(env, thiz)->derivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_publicKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return byteVectorToJbyteArray(env,
       getNativeObjectPtr<PrivateKey>(env, thiz)->publicKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_privateKeyBytesGetterJNI(
  JNIEnv *env,
  jobject thiz
  ) {
  try {
    return sodiumBufferToJbyteArray(env,
      getNativeObjectPtr<PrivateKey>(env, thiz)->privateKeyBytes
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT void JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_deleteNativeObjectPtrJNI(
  JNIEnv *env,
  jobject thiz) {
  try {
    delete getNativeObjectPtr<PrivateKey>(env, thiz);
  } catch (...) {
  throwCppExceptionAsJavaException(env, std::current_exception());
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_getPublicKeyPtrJNI(
    JNIEnv *env,
    jobject thiz) {
  try {
    const PrivateKey *PrivateKeyPtr =
      getNativeObjectPtr<PrivateKey>(env, thiz);
    return (jlong) new PublicKey(
      PrivateKeyPtr->publicKeyBytes,
      PrivateKeyPtr->derivationOptionsJson
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_fromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring json
) {
  try {
    return (jlong) new PrivateKey(PrivateKey::fromJson(
      jstringToString(env, json)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_deriveFromSeedJNI__Ljava_lang_String_2Ljava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jstring seed_string,
  jstring key_derivation_options_json) {
  try {
    return (jlong) new PrivateKey(
      jstringToString(env, seed_string),
      jstringToString(env, key_derivation_options_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_constructJNI___3B_3BLjava_lang_String_2(
  JNIEnv *env,
  jclass clazz,
  jbyteArray secret_key_bytes,
  jbyteArray public_key_bytes,
  jstring key_derivation_options_json
) {
  try {
    return (jlong) new PrivateKey(
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
Java_org_dicekeys_crypto_seeded_PrivateKey_toSerializedBinaryForm(
  JNIEnv *env,
  jobject thiz
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<PrivateKey>(env, thiz)->toSerializedBinaryForm()
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_PrivateKey_fromSerializedBinaryFormJNI(
  JNIEnv *env,
  jclass clazz,
  jbyteArray as_serialized_binary_form) {
  try {
    return (jlong) new PrivateKey(PrivateKey::fromSerializedBinaryForm(
      jbyteArrayToSodiumBuffer(env, as_serialized_binary_form)
    ));
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}

} // extern C

