#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QtQuick>
#include <QDebug>
#include "backendnetworking.h"
#if defined Q_OS_ANDROID
#include "qtandroidservice.h"
#include <QAndroidJniEnvironment>
#include <QAndroidJniObject>

static QObject *rootQmlObject = nullptr;

static void updateWeatherDatabaseCpp(JNIEnv *env, jobject thiz,jint x)
{
    Q_UNUSED(env)
    Q_UNUSED(thiz)
    Q_UNUSED(x)
    QMetaObject::invokeMethod(rootQmlObject, "updateWeatherDatabase");

}

void registerNativeMethods() {

    JNINativeMethod methods[] {{"updateWeatherDatabaseJava", "(I)V", reinterpret_cast<void *>(updateWeatherDatabaseCpp)}};

    QAndroidJniObject javaClass("org/ArtemNosenko/WeatherInPoints/MyStartServiceReceiver");
    QAndroidJniEnvironment env;
    jclass objectClass = env->GetObjectClass(javaClass.object<jobject>());
    env->RegisterNatives(objectClass,
                         methods,
                         sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(objectClass);
}
#endif

int main(int argc, char *argv[])
{
    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);

    QGuiApplication app(argc, argv);

    qmlRegisterType<BackEndNetworking>("io.qt.backendnetworking", 1, 0, "BackEndNetworking");

    QQmlApplicationEngine engine;
    //engine.setOfflineStoragePath("/data/user/0/org.ArtemNosenko.WeatherInPoints/databases/MapWeatherPoints");
    const QUrl url(QStringLiteral("qrc:/main.qml"));
    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
        if (!obj && url == objUrl)
            QCoreApplication::exit(-1);
    }, Qt::QueuedConnection);
    engine.load(url);

    #if defined Q_OS_ANDROID
    rootQmlObject = engine.rootObjects().first();
    registerNativeMethods();
    QtAndroidService *qtAndroidService = new QtAndroidService(&app);
    engine.rootContext()->setContextProperty(QLatin1String("qtAndroidService"), qtAndroidService);

    #endif
   // qDebug()<<engine.offlineStorageDatabaseFilePath("MapWeatherPoints")<<"jepa";
     return app.exec();
}
