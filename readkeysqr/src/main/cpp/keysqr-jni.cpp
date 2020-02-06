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
    jclass exClass = env->FindClass( exceptionClassName);
    if (exClass == NULL) {
        env->ThrowNew(
            env->FindClass( "java/lang/NoClassDefFoundError"),
            (
                std::string("Exception class not found for exception") +
                exceptionClassName +
                " for message " + message
            ).c_str()
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
    javaThrow(env, "com/keysqr/UnknownKeySqrApiException", "Unknown exception type");
    return;
  }
  try {
    std::rethrow_exception(unknownException);
  } catch (ClientNotAuthorizedException e) {
    javaThrow(env, "com/keysqr/ClientNotAuthorizedException", e.what());
    return;
  } catch (InvalidJsonKeyDerivationOptionsException e) {
    javaThrow(env, "com/keysqr/InvalidJsonKeyDerivationOptionsException", e.what());
    return;
  } catch (InvalidKeyDerivationOptionValueException e) {
    javaThrow(env, "com/keysqr/InvalidKeyDerivationOptionValueException", e.what());
    return;
  } catch (std::bad_alloc e) {
    javaThrow(env, "java/lang/OutOfMemoryException", e.what());
    return;
  } catch (nlohmann::json::exception e) {
    javaThrow(env, "com/keysqr/UnknownKeySqrApiException", e.what());
    return;
  } catch (std::exception e) {
    javaThrow(env, "com/keysqr/UnknownKeySqrApiException", e.what());
    return;
  } catch (...) {
    javaThrow(env, "com/keysqr/UnknownKeySqrApiException", "Unknown exception type");
    return;
  }
  javaThrow(env, "com/keysqr/UnknownKeySqrApiException", "Unknown exception type");
}


JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrGetSeed(
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

JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrGetPublicKey(
		JNIEnv* env,
		jobject  obj,
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
        const PublicPrivateKeyPair gkp(
            keySqr,
            jsonKeyDerivationOptions,
            clientsApplicationId
        );
        const std::vector<unsigned char> publicKey = gkp.getPublicKey().getPublicKeyBytes();
        jbyteArray ret = env->NewByteArray(publicKey.size());
        env->SetByteArrayRegion(ret, 0, publicKey.size(), (jbyte*) publicKey.data());
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL; //env->NewByteArray(0);
    }
}


JNIEXPORT jlong JNICALL Java_com_keysqr_KeySqrKt_keySqrGetPublicPrivateKeyPairPtr(
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
        PublicPrivateKeyPair *publicPrivateKeyPairPtr =
            new PublicPrivateKeyPair(keySqr, jsonKeyDerivationOptions, clientsApplicationId);
        jlong publicPrivateKeyPairPtrAsJavaLong = (long)publicPrivateKeyPairPtr;
        return publicPrivateKeyPairPtrAsJavaLong;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return 0L;
    }
}

JNIEXPORT void JNICALL Java_com_keysqr_KeySqrKt_keySqrDisposePublicPrivateKeyPairPtr(
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

JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrPublicPrivateKeyPairGetPublicKey(
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

JNIEXPORT jbyteArray JNICALL Java_com_keysqr_KeySqrKt_keySqrPublicPrivateKeyPairUnseal(
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

        const Message message = ((PublicPrivateKeyPair*)publicPrivateKeyPair)->unseal(
            (const unsigned char*) ciphertext,
            ciphertextLength,
            postDecryptionInstructionJson
        );
        jbyteArray ret = env->NewByteArray(message.contents.length);
        // FIXME -- should also return post-decryption instructions
        env->SetByteArrayRegion(ret, 0, message.contents.length, (jbyte*) message.contents.data);
        return ret;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}


}
