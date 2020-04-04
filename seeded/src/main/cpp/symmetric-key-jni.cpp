#include <jni.h>
#include <string>
#include <stdexcept>
#include "native-object-jni.h"
#include "java-throw-exception.h"
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

extern "C" {


JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_unsealJNI___3BLjava_lang_String_2(
  JNIEnv *env,
  jobject thiz,
  jbyteArray ciphertext,
  jstring post_decryption_instructions_json
) {
  try {
    return sodiumBufferToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->unseal(
        jbyteArrayToVector(env, ciphertext),
        jstringToString(env, post_decryption_instructions_json)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
  }
}

JNIEXPORT jbyteArray JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_sealJNI___3BLjava_lang_String_2(
  JNIEnv *env,
  jobject thiz,
  jbyteArray plaintext,
  jstring post_decryption_instructions_json
) {
  try {
    return byteVectorToJbyteArray(
      env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->seal(
        jbyteArrayToSodiumBuffer(env, plaintext),
        jstringToString(env, post_decryption_instructions_json)
      )
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return NULL;
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
Java_org_dicekeys_crypto_seeded_SymmetricKey_keyDerivationOptionsJsonGetterJNI(
    JNIEnv *env,
    jobject thiz
) {
  try {
    return stringToJString(env,
      getNativeObjectPtr<SymmetricKey>(env, thiz)->keyDerivationOptionsJson
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
Java_org_dicekeys_crypto_seeded_SymmetricKey_constructFromJsonJNI(
  JNIEnv *env,
  jclass clazz,
  jstring symmetric_key_json
) {
  try {
    return (jlong) new SymmetricKey(
      jstringToString(env, symmetric_key_json)
    );
  } catch (...) {
    throwCppExceptionAsJavaException(env, std::current_exception());
    return 0L;
  }
}
JNIEXPORT jlong JNICALL
Java_org_dicekeys_crypto_seeded_SymmetricKey_constructJNI__Ljava_lang_String_2Ljava_lang_String_2(
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

//JNIEXPORT jlong JNICALL Java_org_dicekeys_keys_SymmetricKey_constructJNI(
//  JNIEnv* env,
//  jobject obj,
//  jstring keySqrInHumanReadableFormWithOrientationsObj,
//  jstring keyDerivationOptionsJsonObj,
//  jstring clientsApplicationIdObj,
//  jboolean validateClientId // FIXME
//) {
//  try {
//    const std::string keySqrInHumanReadableFormWithOrientations(
//      env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
//    );
//    const std::string keyDerivationOptionsJson(
//      env->GetStringUTFChars( keyDerivationOptionsJsonObj, NULL )
//    );
//    const std::string clientsApplicationId(
//      env->GetStringUTFChars( clientsApplicationIdObj, NULL )
//    );
//    SymmetricKey* symmetricKeyPtr =
//      new SymmetricKey(env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientations, NULL ), keyDerivationOptionsJson, clientsApplicationId);
//    jlong symmetricKeyPtrAsJavaLong = (long)symmetricKeyPtr;
//    return symmetricKeyPtrAsJavaLong;
//  } catch (...) {
//    throwCppExceptionAsJavaException(env, std::current_exception());
//    return 0L;
//  }
//}
//
//JNIEXPORT void JNICALL Java_org_dicekeys_keys_SymmetricKey_destroyJNI(
//  JNIEnv* env,
//jobject obj,
//  jlong symmetricKeyPtrAsJavaLong
//) {
//try {
//delete ((SymmetricKey*)symmetricKeyPtrAsJavaLong);
//} catch (...) {
//throwCppExceptionAsJavaException(env, std::current_exception());
//}
//}
//
//
//JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_SymmetricKey_sealJNI(
//  JNIEnv* env,
//  jobject obj,
//jlong symmetricKeyPtrAsJavaLong,
//  jbyteArray jplaintext,
//jstring jpostDecryptionInstructionJson
//) {
//try {
//const SymmetricKey* symmetricKeyPtr = (SymmetricKey*)symmetricKeyPtrAsJavaLong;
//size_t plaintextLength = (size_t) env->GetArrayLength(jplaintext);
//const unsigned char* plaintext =
//  (unsigned char*) env->GetByteArrayElements(jplaintext, 0);
//const std::string postDecryptionInstructionJson(
//  env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
//);
//
//const SodiumBuffer ciphertext = symmetricKeyPtr->seal(
//  plaintext,
//  plaintextLength,
//  postDecryptionInstructionJson
//);
//jbyteArray ret = env->NewByteArray(ciphertext.length);
//env->SetByteArrayRegion(ret, 0, ciphertext.length, (jbyte*) ciphertext.data);
//return ret;
//} catch (...) {
//throwCppExceptionAsJavaException(env, std::current_exception());
//return NULL;
//}
//}
//
//JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_SymmetricKey_unsealJNI(
//  JNIEnv* env,
//  jobject obj,
//jlong symmetricKeyPtrAsJavaLong,
//  jbyteArray jciphertext,
//jstring jpostDecryptionInstructionJson
//) {
//try {
//const SymmetricKey* symmetricKeyPtr = (SymmetricKey*)symmetricKeyPtrAsJavaLong;
//size_t ciphertextLength = (size_t) env->GetArrayLength(jciphertext);
//const jbyte *ciphertext = env->GetByteArrayElements(jciphertext, 0);
//
//const std::string postDecryptionInstructionJson(
//  env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
//);
//
//const SodiumBuffer message = symmetricKeyPtr->unseal(
//  (const unsigned char*) ciphertext,
//  ciphertextLength,
//  postDecryptionInstructionJson
//);
//jbyteArray ret = env->NewByteArray(message.length);
//env->SetByteArrayRegion(ret, 0, message.length, (jbyte*) message.data);
//return ret;
//} catch (...) {
//throwCppExceptionAsJavaException(env, std::current_exception());
//return NULL;
//}
//}

} // 
