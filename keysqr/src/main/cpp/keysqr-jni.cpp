#include <string>
#include <jni.h>
#include <exception>
#include "./read-keysqr/lib-keysqr/lib-keysqr.hpp"
// #include "./read-keysqr/includes/json.hpp"

extern "C" {

void javaThrow(
     JNIEnv* env,
     const char* exceptionClassName,
     const char* message
) {
    env->ExceptionClear();
    const jclass noClassDefFoundExceptionClass = env->FindClass("java/lang/NoClassDefFoundError");
    const jclass exClass = env->FindClass(exceptionClassName);
    if (exClass == NULL) {
        std::string noClassMessage =
                std::string("Exception class not found for exception") +
                std::string(exceptionClassName) +
                std::string(" for message ") +
                std::string(message);
        env->ThrowNew(
            noClassDefFoundExceptionClass,
            noClassMessage.c_str()
        );
        return;
    } else {
        env->ThrowNew( exClass, message );
        return;
    }
}
void throwCppExceptionAsJavaException(
     JNIEnv* env,
     const std::exception_ptr unknownException
) {
  if (unknownException == NULL) {
    javaThrow(env, "org/dicekeys/UnknownKeySqrApiException", "Unknown exception type");
    return;
  }
  try {
    std::rethrow_exception(unknownException);
  } catch (ClientNotAuthorizedException e) {
    javaThrow(env, "org/dicekeys/ClientNotAuthorizedException", e.what());
    return;
  } catch (InvalidKeyDerivationOptionsJsonException e) {
    javaThrow(env, "org/dicekeys/InvalidKeyDerivationOptionsJsonException", e.what());
    return;
  } catch (InvalidKeyDerivationOptionValueException e) {
    javaThrow(env, "org/dicekeys/InvalidKeyDerivationOptionValueException", e.what());
    return;
  } catch (CryptographicVerificationFailure e) {
    javaThrow(env, "org/dicekeys/CryptographicVerificationFailure", e.what());
    return;
  } catch (std::bad_alloc e) {
      javaThrow(env, "java/lang/OutOfMemoryException", e.what());
      return;
  } catch (std::invalid_argument e) {
      javaThrow(env, "org/dicekeys/InvalidArgumentException", e.what());
      return;
  } catch (nlohmann::json::exception e) {
    javaThrow(env, "org/dicekeys/UnknownKeySqrApiException", e.what());
    return;
  } catch (std::exception e) {
    javaThrow(env, "org/dicekeys/UnknownKeySqrApiException", e.what());
    return;
  } catch (...) {
    javaThrow(env, "org/dicekeys/UnknownKeySqrApiException", "Unknown exception type");
    return;
  }
  javaThrow(env, "org/dicekeys/UnknownKeySqrApiException", "Unknown exception type");
}


JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_KeySqr_getSeedJNI(
     JNIEnv* env,
     jobject obj,
     jstring keySqrInHumanReadableFormWithOrientationsObj,
     jstring keyDerivationOptionsJsonObj,
     jstring clientsApplicationIdObj
 ) {
    try {
        const std::string keySqrInHumanReadableFormWithOrientations(
                env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
        );
        const std::string keyDerivationOptionsJson(
                env->GetStringUTFChars( keyDerivationOptionsJsonObj, NULL )
        );
        const std::string clientsApplicationId(
                env->GetStringUTFChars( clientsApplicationIdObj, NULL )
        );
        const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
        Seed seed(keySqr, keyDerivationOptionsJson, clientsApplicationId);
        const auto seedBuffer = seed.reveal();
        jbyteArray ret = env->NewByteArray(seedBuffer.length);
        env->SetByteArrayRegion(ret, 0, seedBuffer.length, (jbyte*) seedBuffer.data);
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL; //env->NewByteArray(0);
    }
}

//
// Public key operations
//
JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_PublicKey_sealJNI(
        JNIEnv* env,
        jobject obj,
        jbyteArray jpublicKeyBytes,
        jstring jkeyDerivationOptionsJson,
        jbyteArray jplaintext,
        jstring jpostDecryptionInstructionJson
) {
    try {
        size_t publicKeyBytesLength = (size_t) env->GetArrayLength(jpublicKeyBytes);
        const unsigned char *publicKeyBytesArray =
            (const unsigned char*)  env->GetByteArrayElements(jpublicKeyBytes, 0);
        std::vector<unsigned char> publicKeyBytes(publicKeyBytesLength);
        memcpy(publicKeyBytes.data(), publicKeyBytesArray, publicKeyBytesLength);
        const std::string keyDerivationOptionsJson(
            env->GetStringUTFChars( jkeyDerivationOptionsJson, NULL )
        );
        size_t plaintextLength = (size_t) env->GetArrayLength(jplaintext);
        const unsigned char* plaintext =
            (unsigned char*) env->GetByteArrayElements(jplaintext, 0);

        const std::string postDecryptionInstructionJson(
                env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
        );

        const PublicKey publicKey(publicKeyBytes, keyDerivationOptionsJson);

        const SodiumBuffer ciphertext = publicKey.seal(
                plaintext,
                plaintextLength,
                postDecryptionInstructionJson
        );
        jbyteArray ret = env->NewByteArray(ciphertext.length);
        env->SetByteArrayRegion(ret, 0, ciphertext.length, (jbyte*) ciphertext.data);
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}

} 