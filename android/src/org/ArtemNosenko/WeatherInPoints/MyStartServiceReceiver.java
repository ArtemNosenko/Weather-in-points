package org.ArtemNosenko.WeatherInPoints;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.util.Log;

import android.app.PendingIntent;

import java.util.Date;
import java.util.Calendar;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;




public class MyStartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "MyStartServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

       Log.i("MyStartServiceReceiver", "onReceive ");

        String action = intent.getAction();
        if(action != null) {
            Log.i(TAG + " Action",action);
            //Если произошла перезагрузка, то нужно запустить сервер
            if(!action.equals("notifyIfNeeded")) {
                Intent startServiceIntent = new Intent(context, QtAndroidService.class);
                context.startService(startServiceIntent);
            }
        }

      DbHelper dbhelper = new DbHelper(context);
      JSONObject jo =  dbhelper.getFirstPointInTime();


      Date pDate = new Date();
      try {
        pDate.setHours(Integer.parseInt(jo.getString("hour")));
        pDate.setMinutes(Integer.parseInt(jo.getString("minute")));
       }catch (JSONException e) {Log.i("MyStartServiceReceiver","JSONException");}


      if (pDate.getTime() < System.currentTimeMillis() + 1.5*3600*1000 &&  pDate.getTime()  >= System.currentTimeMillis() + 3600*1000) {//updateDb
       dbhelper.updatePointsInfo(context);
      }

//Каждый пол часа alarm. Если до ближайшей точки менее 1.5  но более 1 часов, то update всех точек.
//Если Ближайшая прошла, то игнор. Если до ближайшей точки меньше часа, но больше получаса, то notify
      if (pDate.getTime() > System.currentTimeMillis() &&  pDate.getTime()  <= System.currentTimeMillis() + 3600*1000)
      {//notify

       ArrayList<String> listToNotify =   dbhelper.getJsonPointStringToNotify();
       int notifyId = 1;

        for (String js : listToNotify)
        {
             try {
                 JSONObject joPoint = new JSONObject(js);

                 Log.i(TAG,joPoint.getString("pointName") + " " + "notifyOnReceive");
                 NotificationClient.notify(context,joPoint.getString("pointName") , "Temp: " + joPoint.getString("temp"),notifyId);
                 notifyId++;
                 } catch (JSONException e) {Log.i(TAG,"exeption1");}
          }

     }
    }
}



