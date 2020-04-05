#include <string>
#include <jni.h>
#include <exception>
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

void javaThrow(
     JNIEnv* env,
     const char* exceptionClassName,
     const char* message
) {
    env->ExceptionClear();
    const jclass exClass = env->FindClass(exceptionClassName);
    if (exClass == NULL) {
        std::string noClassMessage =
                std::string("Exception class not found for exception ") +
                std::string(exceptionClassName) +
                std::string(" for message ") +
                std::string(message);
        const jclass noClassDefFoundExceptionClass = env->FindClass("java/lang/NoClassDefFoundError");
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
    javaThrow(env, "org/dicekeys/crypto/seeded/UnknownException", "Unknown exception type");
    return;
  }
  try {
    std::rethrow_exception(unknownException);
  } catch (JsonParsingException e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/JsonParsingException", e.what());
    return;

  } catch (ClientNotAuthorizedException e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/ClientNotAuthorizedException", e.what());
    return;
  } catch (InvalidKeyDerivationOptionsJsonException e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/InvalidKeyDerivationOptionsJsonException", e.what());
    return;
  } catch (InvalidKeyDerivationOptionValueException e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/InvalidKeyDerivationOptionValueException", e.what());
    return;
  } catch (KeyLengthException e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/KeyLengthException", e.what());
    return;
  } catch (CryptographicVerificationFailure e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/CryptographicVerificationFailure", e.what());
    return;
  } catch (std::bad_alloc e) {
    javaThrow(env, "java/lang/OutOfMemoryException", e.what());
    return;
  } catch (std::invalid_argument e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/InvalidArgumentException", e.what());
    return;
  } catch (nlohmann::json::exception e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/UnknownException", e.what());
    return;
  } catch (std::exception e) {
    javaThrow(env, "org/dicekeys/crypto/seeded/UnknownException", e.what());
    return;
  } catch (...) {
    javaThrow(env, "org/dicekeys/crypto/seeded/UnknownException", "Unknown exception type");
    return;
  }
  javaThrow(env, "org/dicekeys/crypto/seeded/UnknownException", "Unknown exception type");
}
