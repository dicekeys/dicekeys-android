#pragma once

#include <string>
#include <jni.h>
#include <exception>
#include "./seeded-crypto/lib-seeded/lib-seeded.hpp"

void javaThrow(
     JNIEnv* env,
     const char* exceptionClassName,
     const char* message
);

void throwCppExceptionAsJavaException(
     JNIEnv* env,
     const std::exception_ptr unknownException
);