#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QtQuick>

#include "backendnetworking.h"
#include "notificationclient.h"

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

    NotificationClient *notificationClient = new NotificationClient(&engine);
    engine.rootContext()->setContextProperty(QLatin1String("notificationClient"),
                                             notificationClient);

     return app.exec();
}
