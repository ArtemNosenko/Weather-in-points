package org.ArtemNosenko.WeatherInPoints;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.util.Log;

import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.app.NotificationChannel;

import org.json.JSONObject;
import org.json.JSONException;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyStartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "MyStartServiceReceiver";
    private static native void updateWeatherDatabaseJava(int x);
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MyStartServiceReceiver", "onReceive");
       // String name = new String(intent.getByteArrayExtra("name"));
       updateWeatherDatabaseJava(3);

       DBHelper dbHelper = new DBHelper(context);
       SQLiteDatabase db =  dbHelper.getReadableDatabase();
       Cursor c = db.query("Points",
               null,
               null, null, null, null, null);

                       if (c.moveToFirst()) {
                           while ( !c.isAfterLast() ) {
                               String js = c.getString(0);
                               try {

                                   JSONObject jo = new JSONObject(js);
                                   Log.i(TAG,jo.getString("pointName"));

                                   notifyy(context,jo.getString("pointName") + " temp: " + jo.getString("temp"));
                               } catch (JSONException e) {

                               Log.i(TAG,"exeption");
                               }
                               c.moveToNext();
                           }
                       }
    }

public  void notifyy(Context context, String text){

   NotificationChannel channel = new NotificationChannel("Qt", "Qt Notifier", NotificationManager.IMPORTANCE_HIGH);

   NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);;
   notificationManager.createNotificationChannel(channel);

   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Qt")
                    .setContentTitle("Alarm!")
                    .setContentText(text)
                    .setSmallIcon(R.drawable.icon);
    notificationManager.notify(1, builder.build());
    }
}

class DBHelper extends SQLiteOpenHelper {

   public DBHelper(Context context) {
     // конструктор суперкласса
     super(context, "/data/user/0/org.ArtemNosenko.WeatherInPoints/files/QML/OfflineStorage/Databases/cd123cf2b8ce6fdec3d6853ff7304ab6.sqlite", null, 1);
   }
   @Override
   public void onCreate(SQLiteDatabase db) { }
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
 }
