cmake_minimum_required(VERSION 3.14)

project(jni-read-dicekey
    VERSION 0.11
    DESCRIPTION "JNI Library."
    LANGUAGES CXX
)

add_subdirectory("read-dicekey")

add_library(
    jni-read-dicekey
    SHARED
    "read-dicekey-jni.cpp"
)

target_link_libraries( jni-read-dicekey
    PRIVATE
    lib-read-dicekey
    lib-dicekey
)
