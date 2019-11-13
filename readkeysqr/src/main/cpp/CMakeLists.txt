cmake_minimum_required(VERSION 3.14)

project(jni-read-keysqr
    VERSION 0.10
    DESCRIPTION "JNI Library."
    LANGUAGES CXX
)

add_subdirectory("read-keysqr")

add_library(
    jni-read-keysqr
    SHARED
    "read-keysqr-jni.cpp"
)

target_link_libraries( jni-read-keysqr
    PRIVATE
    lib-read-keysqr
)
