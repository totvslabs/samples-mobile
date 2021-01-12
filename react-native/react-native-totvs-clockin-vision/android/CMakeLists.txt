# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.5.1)
set(CMAKE_VERBOSE_MAKEFILE on)

# Configure build library name.
set(TARGET_NAME ai_core)

# Build project shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -std=c++17 -funwind-tables")

# Path to project: REPLACE WITH YOUR PATH!
# set(PROJECT_PATH /home/jro/wk/totvs/clockin/camera-intc/android)

# Configure import libs: MAKE SURE TO HAVE A 'cppLibs' DIRECTORY"
set(LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/cppLibs)

# ------------------------------------------------------------------
# -- OPENCV
# ------------------------------------------------------------------
# Path to OpenCV: REPLACE WITH YOUR PATH!
set(OPENCV_PATH ${LIB_DIR}/opencv)

add_library(
        lib_opencv
        SHARED
        IMPORTED
)

# sets the location of the prebuilt opencv .so
set_target_properties(
        lib_opencv
        PROPERTIES IMPORTED_LOCATION
        ${OPENCV_PATH}/lib/${ANDROID_ABI}/libopencv_java4.so
)
# ------------------------------------------------------------------


# ------------------------------------------------------------------
# -- DLIB
# ------------------------------------------------------------------
set(DLIB_PATH ${LIB_DIR}/dlib)

add_library(
        dlib
        SHARED
        IMPORTED
)

# sets the location of the prebuilt dlib .so
set_target_properties(
        dlib
        PROPERTIES IMPORTED_LOCATION
        ${DLIB_PATH}/lib/${ANDROID_ABI}/libdlib.so
)
# ------------------------------------------------------------------

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# Provides a relative path to your source file(s).
add_library(
        ${TARGET_NAME}  # Sets the name of the library.
        SHARED  # Sets the library as a shared library.
        src/main/cpp/RecognitionInfo.cpp
        src/main/cpp/FaceUtils.cpp
        src/main/cpp/EmbeddingsManager.cpp
        src/main/cpp/FaceRecognizer.cpp
)


target_include_directories(
        ${TARGET_NAME} PRIVATE
        ${CMAKE_SOURCE_DIR}/include
        ${DLIB_PATH}/include
        ${OPENCV_PATH}/include
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(
        log-lib  # Sets the name of the path variable.
        log  # Specifies the name of the NDK library that you want CMake to locate.
        android
)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(
        ${TARGET_NAME}  # Specifies the target library.
        -ljnigraphics
        lib_opencv
        dlib
        ${log-lib}  # Links the target library to the log library included in the NDK.
        android
)
