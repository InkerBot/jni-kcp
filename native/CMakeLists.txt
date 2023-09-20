cmake_minimum_required(VERSION 3.25)
project(native)

set(CMAKE_CXX_STANDARD 20)

find_package(Java COMPONENTS Development)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_subdirectory(libs/kcp)
include_directories(libs/kcp)

include_directories(build/generated/sources/headers)
add_library(native SHARED bot_inker_kcp_KcpC.cpp)

target_link_directories(native PUBLIC build/generated/sources/headers)
target_link_libraries(native PRIVATE kcp)