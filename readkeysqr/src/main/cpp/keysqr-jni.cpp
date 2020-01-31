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


}
