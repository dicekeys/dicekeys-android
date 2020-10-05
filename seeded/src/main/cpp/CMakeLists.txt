cmake_minimum_required(VERSION 3.14)

project(jni-seeded-crypto
    VERSION 0.10
    DESCRIPTION "Seeded Cryptography JNI Library."
    LANGUAGES CXX
)

add_subdirectory("seeded-crypto")

add_library(
    jni-seeded-crypto
    SHARED
    java-throw-exception.cpp
    packaged-sealed-message-jni.cpp
    sealing-key-jni.cpp
    unsealing-key-jni.cpp
    secret-jni.cpp
    password-jni.cpp
    signature-verification-key-jni.cpp
    signing-key-jni.cpp
    symmetric-key-jni.cpp
)

target_include_directories(
    jni-seeded-crypto
    PRIVATE
    seeded-crypto/extern/libsodium/src/libsodium/include
)

target_link_libraries(
    jni-seeded-crypto
    PRIVATE
    lib-seeded
)
