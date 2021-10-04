cmake_minimum_required(VERSION 3.5)

project(weatherInPoints LANGUAGES CXX)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_compile_options(
     -Wall -Wextra -Wpedantic -Wcast-align -Wcast-qual -Wconversion -Wctor-dtor-privacy
         -Wenum-compare -Wfloat-equal -Wnon-virtual-dtor -Wold-style-cast -Woverloaded-virtual
          -Wredundant-decls
     )

# QtCreator supports the following variables for Android, which are identical to qmake Android variables.
# Check http://doc.qt.io/qt-5/deployment-android.html for more information.
# They need to be set before the find_package(Qt5 ...) call.

#if(ANDROID)
#    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/android")
#    if (ANDROID_ABI STREQUAL "armeabi-v7a")
#        set(ANDROID_EXTRA_LIBS
#            ${CMAKE_CURRENT_SOURCE_DIR}/path/to/libcrypto.so
#            ${CMAKE_CURRENT_SOURCE_DIR}/path/to/libssl.so)
#    endif()
#endif()

#include_directories(/thirdparty/boost)


set(THREADS_PREFER_PTHREAD_FLAG ON)

find_package(Threads REQUIRED)
find_package(Qt5 COMPONENTS Core Quick Location REQUIRED)


#Заголовочные файлы
set(HEADERS
    backendnetworking.h)
#Файлы исходных кодов
set(SOURCES
        main.cpp
        backendnetworking.cpp
        qml.qrc
    )



if(ANDROID)
    add_library(weatherInPoints SHARED ${HEADERS} ${SOURCES} )
else()
    add_executable(weatherInPoints ${HEADERS} ${SOURCES})
endif()

target_compile_definitions(weatherInPoints
  PRIVATE $<$<OR:$<CONFIG:Debug>,$<CONFIG:RelWithDebInfo>>:QT_QML_DEBUG>)
target_link_libraries(weatherInPoints
  PRIVATE Qt5::Core Qt5::Quick Threads::Threads)
