#pragma once

#include <jni.h>
#include <string>
#include "./seeded-crypto/lib-seeded/sodium-buffer.hpp"
#include "java-throw-exception.h"

inline std::string jstringToString(
    JNIEnv *env,
    jstring str
) {
    return std::string(env->GetStringUTFChars( str, NULL ));
}

inline std::vector<unsigned char> jbyteArrayToVector(
    JNIEnv *env,
    jbyteArray a
) {
    size_t arrayLen = (size_t) env->GetArrayLength(a);
    const unsigned char *arrayBytePtr = (unsigned char*) env->GetByteArrayElements(a, 0);
    std::vector<unsigned char> newVector(arrayLen);
    memcpy(newVector.data(), arrayBytePtr, arrayLen);
    return newVector;
};

inline SodiumBuffer jbyteArrayToSodiumBuffer(
  JNIEnv *env,
  jbyteArray a
) {
    size_t arrayLen = (size_t) env->GetArrayLength(a);
    const unsigned char *arrayBytePtr = (unsigned char *) env->GetByteArrayElements(a, 0);
    return SodiumBuffer(arrayLen, arrayBytePtr);
};

inline jbyteArray byteVectorToJbyteArray(
    JNIEnv *env,
    const std::vector<unsigned char>& byteVector
) {
    jbyteArray newJByteArray = env->NewByteArray(byteVector.size());
    env->SetByteArrayRegion(newJByteArray, 0, byteVector.size(), (jbyte*) byteVector.data());
    return newJByteArray;
}

inline jbyteArray sodiumBufferToJbyteArray(
  JNIEnv *env,
  const SodiumBuffer& sodiumBuffer
) {
    jbyteArray newJByteArray = env->NewByteArray(sodiumBuffer.length);
    env->SetByteArrayRegion(newJByteArray, 0, sodiumBuffer.length, (jbyte*) sodiumBuffer.data);
    return newJByteArray;
}


inline jstring stringToJString(
  JNIEnv *env,
  const std::string str
) {
    return env->NewStringUTF(str.c_str());
}

template <typename T>
jfieldID getNativeObjPtrFieldId(
        JNIEnv *env,
        jobject obj,
        const char *nativeObjectPtrFieldName = "nativeObjectPtr"
) {
    static jfieldID fieldId = NULL;
    if (fieldId == NULL) {
        jclass this_class = env->GetObjectClass(obj);
        fieldId = env->GetFieldID(this_class, nativeObjectPtrFieldName, "J");
    }
    return fieldId;
}

template <typename T>
T* getNativeObjectPtr(
    JNIEnv *env,
    jobject obj,
    const char *nativeObjectPtrFieldName = "nativeObjectPtr"
) {
    try {
        return (T *) env->GetLongField(
                obj,
                getNativeObjPtrFieldId<T>(env, obj, nativeObjectPtrFieldName)
        );
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return NULL;
    }
}

template <typename T>
void deleteNativeObjectPtr(
    JNIEnv *env, jobject obj, const char *nativeObjectPtrFieldName = "nativeObjectPtr"
) {
    try {
        const T* nativeObjectPtr = getNativeObjectPtr<T>(env, obj, nativeObjectPtrFieldName);
        delete nativeObjectPtr;
        return;
    } catch (...) {
        throwCppExceptionAsJavaException(env, std::current_exception());
        return;
    }
}