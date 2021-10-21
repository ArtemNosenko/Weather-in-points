#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QtQuick>
#include <QDebug>
#include "backendnetworking.h"
#if defined Q_OS_ANDROID
#include "qtandroidservice.h"
#endif
int main(int argc, char *argv[])
{
    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);

    QGuiApplication app(argc, argv);

    qmlRegisterType<BackEndNetworking>("io.qt.backendnetworking", 1, 0, "BackEndNetworking");

    QQmlApplicationEngine engine;
    const QUrl url(QStringLiteral("qrc:/main.qml"));
    QObject::connect(&engine, &QQmlApplicationEngine::objectCreated,
                     &app, [url](QObject *obj, const QUrl &objUrl) {
        if (!obj && url == objUrl)
            QCoreApplication::exit(-1);
    }, Qt::QueuedConnection);
    engine.load(url);

    #if defined Q_OS_ANDROID
    QtAndroidService *qtAndroidService = new QtAndroidService(&app);
    engine.rootContext()->setContextProperty(QLatin1String("qtAndroidService"), qtAndroidService);
    //qtAndroidService->startService();
    #endif
     return app.exec();
}
