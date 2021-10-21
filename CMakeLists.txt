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



if(ANDROID)
    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/android")
   # if (ANDROID_ABI STREQUAL "armeabi-v7a")
   #     set(ANDROID_EXTRA_LIBS
   #         ${CMAKE_CURRENT_SOURCE_DIR}/path/to/libcrypto.so
   #         ${CMAKE_CURRENT_SOURCE_DIR}/path/to/libssl.so)
   # endif()
    set(DISTFILES
        ${DISTFILES}
        android/AndroidManifest.xml
        android/build.gradle
        android/res/values/libs.xml)

    set(OTHER_FILES
        android/src/org/ArtemNosenko/WeatherInPoints/NotificationClient.java
        android/build.gradle
        android/gradle.properties
        android/gradle/wrapper/gradle-wrapper.jar
        android/gradle/wrapper/gradle-wrapper.properties
        android/gradlew
        android/gradlew.bat
        android/res/values/libs.xml
        android/src/org/ArtemNosenko/WeatherInPoints/MyStartServiceReceiver.java
        android/src/org/ArtemNosenko/WeatherInPoints/QtAndroidService.java
        android/src/org/ArtemNosenko/WeatherInPoints/DbHelper.java
        android/src/org/ArtemNosenko/WeatherInPoints/HTTPrequestHelper.java
        android/AndroidManifest.xml
        )
    find_package(Qt5 COMPONENTS AndroidExtras REQUIRED)



endif()

#Заголовочные файлы
set(HEADERS
    backendnetworking.h
    notificationclient.h)
#Файлы исходных кодов
set(SOURCES
        main.cpp
        backendnetworking.cpp
        notificationclient.cpp
        qml.qrc
    )

if (ANDROID)
    set(HEADERS
        ${HEADERS}
         qtandroidservice.h)

     set(SOURCES
         ${SOURCES}
         qtandroidservice.cpp
         )
endif()

include_directories(/opt/boost_1_76_0)



set(THREADS_PREFER_PTHREAD_FLAG ON)

find_package(Threads REQUIRED)
find_package(Qt5 COMPONENTS Core Quick Location  REQUIRED)






#set(ANDROID_PACKAGE_SOURCE_DIR  $$PWD/android)
if(ANDROID)
    add_library(weatherInPoints SHARED ${HEADERS} ${SOURCES}  ${OTHER_FILES})
else()
    add_executable(weatherInPoints ${HEADERS} ${SOURCES} )
endif()

target_compile_definitions(weatherInPoints
  PRIVATE $<$<OR:$<CONFIG:Debug>,$<CONFIG:RelWithDebInfo>>:QT_QML_DEBUG>)
if (ANDROID)
    target_link_libraries(weatherInPoints PRIVATE Qt5::Core Qt5::Quick Qt5::AndroidExtras Threads::Threads)
else()
    target_link_libraries(weatherInPoints PRIVATE Qt5::Core Qt5::Quick Threads::Threads)
endif()
