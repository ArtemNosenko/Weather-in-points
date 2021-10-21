/****************************************************************************
**
** Copyright (C) 2020 The Qt Company Ltd.
** Contact: https://www.qt.io/licensing/
**
** This file is part of the QtAndroidExtras module of the Qt Toolkit.
**
** $QT_BEGIN_LICENSE:BSD$
** Commercial License Usage
** Licensees holding valid commercial Qt licenses may use this file in
** accordance with the commercial license agreement provided with the
** Software or, alternatively, in accordance with the terms contained in
** a written agreement between you and The Qt Company. For licensing terms
** and conditions see https://www.qt.io/terms-conditions. For further
** information use the contact form at https://www.qt.io/contact-us.
**
** BSD License Usage
** Alternatively, you may use this file under the terms of the BSD license
** as follows:
**
** "Redistribution and use in source and binary forms, with or without
** modification, are permitted provided that the following conditions are
** met:
**   * Redistributions of source code must retain the above copyright
**     notice, this list of conditions and the following disclaimer.
**   * Redistributions in binary form must reproduce the above copyright
**     notice, this list of conditions and the following disclaimer in
**     the documentation and/or other materials provided with the
**     distribution.
**   * Neither the name of The Qt Company Ltd nor the names of its
**     contributors may be used to endorse or promote products derived
**     from this software without specific prior written permission.
**
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
** "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
** LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
** A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
** OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
** SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
** LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
** DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
** THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
** (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
** OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
**
** $QT_END_LICENSE$
**
****************************************************************************/

package org.ArtemNosenko.WeatherInPoints;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.Service;
import android.os.IBinder;

import android.os.CountDownTimer;

import android.app.AlarmManager;
import android.app.PendingIntent;



public class QtAndroidService extends Service
{
    private static final String TAG = "QtAndroidService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Creating Service");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying Service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        Intent intentAlarm = new Intent(this,MyStartServiceReceiver.class);
        String title = new String(intent.getByteArrayExtra("title"));
        String text = new String(intent.getByteArrayExtra("text"));
        Log.i(TAG, "onStartCommand: "  + text);
        intentAlarm.putExtra("title",title);
        intentAlarm.putExtra("text",text);


        PendingIntent pi =  PendingIntent.getBroadcast(this,1,intentAlarm,PendingIntent.FLAG_UPDATE_CURRENT);
        long curTime = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP,curTime + 4000,pi);
        return ret;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}




//        new CountDownTimer(30000, 1000) {

//            public void onTick(long millisUntilFinished) {
//                notifyy(String.valueOf(millisUntilFinished / 1000));
//            }

//            public void onFinish() {
//                notifyy("done!");
//            }
//        }.start();

//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.graphics.Color;
//import android.graphics.BitmapFactory;
//import android.app.NotificationChannel;

//public class NotificationClient
//{
//    private static NotificationManager m_notificationManager;
//    private static Notification.Builder m_builder;

//    public NotificationClient() {}

//    public static void notify(Context context,String title, String message) {
//        try {
//            m_notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                int importance = NotificationManager.IMPORTANCE_DEFAULT;
//                NotificationChannel notificationChannel = new NotificationChannel("Qt", "Qt Notifier", importance);
//                m_notificationManager.createNotificationChannel(notificationChannel);
//                m_builder = new Notification.Builder(context, notificationChannel.getId());
//            } else {
//                m_builder = new Notification.Builder(context);
//            }

//            m_builder.setSmallIcon(R.drawable.icon)
//                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
//                    .setContentTitle(title)
//                    .setContentText(message)
//                    .setDefaults(Notification.DEFAULT_SOUND)
//                    .setColor(Color.GREEN)
//                    .setAutoCancel(true);

//            m_notificationManager.notify(0, m_builder.build());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
