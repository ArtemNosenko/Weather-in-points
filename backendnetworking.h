#ifndef BACKENDNETWORKING_H
#define BACKENDNETWORKING_H

#include <QObject>
#include <string>
class BackEndNetworking : public QObject
{
    Q_OBJECT

    Q_PROPERTY(QString host READ host WRITE setHost NOTIFY hostChanged)
    QString m_host;

    Q_PROPERTY(QString request READ request WRITE setRequest NOTIFY requestChanged)
    QString m_request;



public:

    explicit BackEndNetworking(QObject *parent = nullptr);



    QString host() const
    {
        return m_host;
    }
    QString request() const
    {
        return m_request;
    }

public slots:
    void setHost(QString host)
    {
        if (m_host == host)
            return;

        m_host = host;
        emit hostChanged(m_host);
    }
    void setRequest(QString request)
    {
        if (m_request == request)
            return;

        m_request = request;
        emit requestChanged(m_request);
    }
    QString run();

signals:
    void hostChanged(QString host);
    void requestChanged(QString request);
};

#endif // BACKENDNETWORKING_H
