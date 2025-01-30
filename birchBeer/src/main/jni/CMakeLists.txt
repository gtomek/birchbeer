cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fstack-protector-all")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fstack-protector-all")

add_library(toolChecker SHARED
        toolChecker.cpp)

target_link_libraries(toolChecker
        android
        log)

# Use 16 KB ELF alignment to support devices with page size greater than 4 KB
target_link_options(realm-jni PRIVATE "-Wl,-z,max-page-size=16384")