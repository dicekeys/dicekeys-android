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
  } catch (InvalidJsonKeyDerivationOptionsException e) {
    javaThrow(env, "org/dicekeys/InvalidJsonKeyDerivationOptionsException", e.what());
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
     jstring jsonKeyDerivationOptionsObj,
     jstring clientsApplicationIdObj
 ) {
    try {
        const std::string keySqrInHumanReadableFormWithOrientations(
                env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
        );
        const std::string jsonKeyDerivationOptions(
                env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
        );
        const std::string clientsApplicationId(
                env->GetStringUTFChars( clientsApplicationIdObj, NULL )
        );
        const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
        Seed seed(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
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
        jstring jjsonKeyDerivationOptions,
        jbyteArray jplaintext,
        jstring jpostDecryptionInstructionJson
) {
    try {
        size_t publicKeyBytesLength = (size_t) env->GetArrayLength(jpublicKeyBytes);
        const unsigned char *publicKeyBytesArray =
            (const unsigned char*)  env->GetByteArrayElements(jpublicKeyBytes, 0);
        std::vector<unsigned char> publicKeyBytes(publicKeyBytesLength);
        memcpy(publicKeyBytes.data(), publicKeyBytesArray, publicKeyBytesLength);
        const std::string jsonKeyDerivationOptions(
            env->GetStringUTFChars( jjsonKeyDerivationOptions, NULL )
        );
        size_t plaintextLength = (size_t) env->GetArrayLength(jplaintext);
        const unsigned char* plaintext =
            (unsigned char*) env->GetByteArrayElements(jplaintext, 0);

        const std::string postDecryptionInstructionJson(
                env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
        );

        const PublicKey publicKey(publicKeyBytes, jsonKeyDerivationOptions);

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


//
// PublicPrivateKeyPair operations
//
JNIEXPORT jlong JNICALL Java_org_dicekeys_keys_PublicPrivateKeyPair_constructJNI(
        JNIEnv* env,
        jobject obj,
        jstring keySqrInHumanReadableFormWithOrientationsObj,
        jstring jsonKeyDerivationOptionsObj,
        jstring clientsApplicationIdObj,
        jboolean validateClientId // FIXME
) {
    try {
        const std::string keySqrInHumanReadableFormWithOrientations(
                env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
        );
        const std::string jsonKeyDerivationOptions(
                env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
        );
        const std::string clientsApplicationId(
                env->GetStringUTFChars( clientsApplicationIdObj, NULL )
        );
        const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
        PublicPrivateKeyPair *publicPrivateKeyPairPtr =
            new PublicPrivateKeyPair(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
        jlong publicPrivateKeyPairPtrAsJavaLong = (long)publicPrivateKeyPairPtr;
        return publicPrivateKeyPairPtrAsJavaLong;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return 0L;
    }
}

JNIEXPORT void JNICALL Java_org_dicekeys_keys_PublicPrivateKeyPair_destroyJNI(
        JNIEnv* env,
        jobject obj,
        jlong publicPrivateKeyPair
) {
    try {
        delete ((PublicPrivateKeyPair*)publicPrivateKeyPair);
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
    }
}

JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_PublicPrivateKeyPair_getPublicKeyBytesJNI(
        JNIEnv* env,
        jobject obj,
        jlong publicPrivateKeyPair
) {
    try {
        const std::vector<unsigned char> publicKey =
                ((PublicPrivateKeyPair*)publicPrivateKeyPair)->getPublicKey().getPublicKeyBytes();
        jbyteArray ret = env->NewByteArray(publicKey.size());
        env->SetByteArrayRegion(ret, 0, publicKey.size(), (jbyte*) publicKey.data());
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}

JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_PublicPrivateKeyPair_unsealJNI(
        JNIEnv* env,
        jobject obj,
        jlong publicPrivateKeyPair,
        jbyteArray jciphertext,
        jstring jpostDecryptionInstructionJson
) {
    try {
        size_t ciphertextLength = (size_t) env->GetArrayLength(jciphertext);
        const jbyte *ciphertext = env->GetByteArrayElements(jciphertext, 0);

        const std::string postDecryptionInstructionJson(
                env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
        );

        const SodiumBuffer message = ((PublicPrivateKeyPair*)publicPrivateKeyPair)->unseal(
            (const unsigned char*) ciphertext,
            ciphertextLength,
            postDecryptionInstructionJson
        );
        jbyteArray ret = env->NewByteArray(message.length);
        env->SetByteArrayRegion(ret, 0, message.length, (jbyte*) message.data);
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}

//
// Signing key / verification key operations
//

JNIEXPORT jlong JNICALL Java_org_dicekeys_keys_SigningKey_constructJNI(
        JNIEnv* env,
        jobject obj,
        jstring keySqrInHumanReadableFormWithOrientationsObj,
        jstring jsonKeyDerivationOptionsObj,
        jstring clientsApplicationIdObj,
        jboolean validateClientId // FIXME
) {
    try {
        const std::string keySqrInHumanReadableFormWithOrientations(
                env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
        );
        const std::string jsonKeyDerivationOptions(
                env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
        );
        const std::string clientsApplicationId(
                env->GetStringUTFChars( clientsApplicationIdObj, NULL )
        );
        const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
        SigningKey *signingKey =
                new SigningKey(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
        jlong publicPrivateKeyPairPtrAsJavaLong = (long)signingKey;
        return publicPrivateKeyPairPtrAsJavaLong;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return 0L;
    }
}

JNIEXPORT void JNICALL Java_org_dicekeys_keys_SigningKey_destroyJNI(
        JNIEnv* env,
        jobject obj,
        jlong signingKey
) {
    try {
        delete ((SigningKey*)signingKey);
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
    }
}

JNIEXPORT jbyteArray Java_org_dicekeys_keys_SigningKey_generateSignatureJNI(
        JNIEnv* env,
        jobject obj,
        jlong signingKeyPtrAsLong,
        jbyteArray jmessage
) {
    try {
        SigningKey *signingKey = (SigningKey*) signingKeyPtrAsLong;

        const size_t messageLength = (size_t) env->GetArrayLength(jmessage);
        const unsigned char *message =
                (const unsigned char*)  env->GetByteArrayElements(jmessage, 0);

        const auto signature = signingKey->generateSignature(message, messageLength);

        jbyteArray signatureAsByteArray = env->NewByteArray(signature.size());
        env->SetByteArrayRegion(signatureAsByteArray, 0, signature.size(), (jbyte*) signature.data());

        return signatureAsByteArray;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return (jbyteArray) NULL;
    }
}

JNIEXPORT jbyteArray Java_org_dicekeys_keys_SigningKey_getSignatureVerificationKeyBytesJNI(
        JNIEnv* env,
        jobject obj,
        jlong signingKeyPtrAsLong
) {
    try {
        SigningKey *signingKey = (SigningKey*) signingKeyPtrAsLong;

        const auto keyBytes = signingKey->getSignatureVerificationKey().verificationKeyBytes;

        jbyteArray keyBytesArray = env->NewByteArray(keyBytes.size());
        env->SetByteArrayRegion(keyBytesArray, 0, keyBytes.size(), (jbyte*) keyBytes.data());

        return keyBytesArray;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return (jbyteArray) NULL;
    }
}

JNIEXPORT jboolean JNICALL Java_org_dicekeys_keys_SignatureVerificationKey_verifySignatureJNI(
        JNIEnv* env,
        jobject obj,
        jbyteArray jmessage,
        jbyteArray jsignature,
        jbyteArray jsignatureVerificationKeyBytes
) {
    try {
        const size_t messageLength = (size_t) env->GetArrayLength(jmessage);
        const unsigned char *message =
                (const unsigned char*)  env->GetByteArrayElements(jmessage, 0);
        const size_t signatureLength = (size_t) env->GetArrayLength(jsignature);
        const unsigned char *signature =
                (const unsigned char*)  env->GetByteArrayElements(jsignature, 0);
        const size_t signatureVerificationKeyBytesLength = (size_t) env->GetArrayLength(jsignatureVerificationKeyBytes);
        const unsigned char *signatureVerificationKeyBytes =
                (const unsigned char*)  env->GetByteArrayElements(jsignatureVerificationKeyBytes, 0);

        jboolean result = SignatureVerificationKey::verify(
            signatureVerificationKeyBytes, signatureVerificationKeyBytesLength,
            message, messageLength,
            signature, signatureLength
        );

        return result;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return (jboolean) false;
    }
}


//
// Symmetric key operations
//
JNIEXPORT jlong JNICALL Java_org_dicekeys_keys_SymmetricKey_constructJNI(
        JNIEnv* env,
        jobject obj,
        jstring keySqrInHumanReadableFormWithOrientationsObj,
        jstring jsonKeyDerivationOptionsObj,
        jstring clientsApplicationIdObj,
        jboolean validateClientId // FIXME
) {
    try {
        const std::string keySqrInHumanReadableFormWithOrientations(
                env->GetStringUTFChars( keySqrInHumanReadableFormWithOrientationsObj, NULL )
        );
        const std::string jsonKeyDerivationOptions(
                env->GetStringUTFChars( jsonKeyDerivationOptionsObj, NULL )
        );
        const std::string clientsApplicationId(
                env->GetStringUTFChars( clientsApplicationIdObj, NULL )
        );
        const KeySqrFromString keySqr(keySqrInHumanReadableFormWithOrientations);
        SymmetricKey* symmetricKeyPtr =
                new SymmetricKey(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
        jlong symmetricKeyPtrAsJavaLong = (long)symmetricKeyPtr;
        return symmetricKeyPtrAsJavaLong;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return 0L;
    }
}

JNIEXPORT void JNICALL Java_org_dicekeys_keys_SymmetricKey_destroyJNI(
        JNIEnv* env,
        jobject obj,
        jlong symmetricKeyPtrAsJavaLong
) {
    try {
        delete ((SymmetricKey*)symmetricKeyPtrAsJavaLong);
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
    }
}


JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_SymmetricKey_sealJNI(
        JNIEnv* env,
        jobject obj,
        jlong symmetricKeyPtrAsJavaLong,
        jbyteArray jplaintext,
        jstring jpostDecryptionInstructionJson
) {
    try {
        const SymmetricKey* symmetricKeyPtr = (SymmetricKey*)symmetricKeyPtrAsJavaLong;
        size_t plaintextLength = (size_t) env->GetArrayLength(jplaintext);
        const unsigned char* plaintext =
                (unsigned char*) env->GetByteArrayElements(jplaintext, 0);
        const std::string postDecryptionInstructionJson(
                env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
        );

        const SodiumBuffer ciphertext = symmetricKeyPtr->seal(
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

JNIEXPORT jbyteArray JNICALL Java_org_dicekeys_keys_SymmetricKey_unsealJNI(
        JNIEnv* env,
        jobject obj,
        jlong symmetricKeyPtrAsJavaLong,
        jbyteArray jciphertext,
        jstring jpostDecryptionInstructionJson
) {
    try {
        const SymmetricKey* symmetricKeyPtr = (SymmetricKey*)symmetricKeyPtrAsJavaLong;
        size_t ciphertextLength = (size_t) env->GetArrayLength(jciphertext);
        const jbyte *ciphertext = env->GetByteArrayElements(jciphertext, 0);

        const std::string postDecryptionInstructionJson(
                env->GetStringUTFChars( jpostDecryptionInstructionJson, NULL )
        );

        const SodiumBuffer message = symmetricKeyPtr->unseal(
                (const unsigned char*) ciphertext,
                ciphertextLength,
                postDecryptionInstructionJson
        );
        jbyteArray ret = env->NewByteArray(message.length);
        env->SetByteArrayRegion(ret, 0, message.length, (jbyte*) message.data);
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}

} // extern "C"
