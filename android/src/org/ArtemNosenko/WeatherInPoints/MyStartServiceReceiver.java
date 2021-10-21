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

import java.util.Date;
import java.util.Calendar;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


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

  //    Log.i(TAG,jo.toString());

      Date pDate = new Date();
      try {
        pDate.setHours(Integer.parseInt(jo.getString("hour")));
        pDate.setMinutes(Integer.parseInt(jo.getString("minute")));
       }catch (JSONException e) {Log.i("MyStartServiceReceiver","JSONException");}

//   Log.i(TAG, String.valueOf(pDate.getTime()/1000 - System.currentTimeMillis()/1000));

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

                 notifyy(context,joPoint.getString("pointName") , "Temp: " + joPoint.getString("temp"),notifyId);
                 notifyId++;
                 } catch (JSONException e) {Log.i(TAG,"exeption1");}
          }

     }

}

public  void notifyy(Context context, String title, String text,int notifyId){

   NotificationChannel channel = new NotificationChannel("Qt", "Qt Notifier", NotificationManager.IMPORTANCE_HIGH);

   NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   notificationManager.createNotificationChannel(channel);

   NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Qt")
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.icon);
    notificationManager.notify(notifyId, builder.build());
    }
}



class HTTPrequestHelper{
    RequestQueue mRequestQueue;
    private String _pointId;
    Context _cont;

    public HTTPrequestHelper(Context context){
        _cont = context;
        mRequestQueue = Volley.newRequestQueue(_cont);
        Log.i("HTTPrequestHelper","Constr");
        }

    void updatePoint(String pointId){
        _pointId = pointId;

        Log.i("HTTPrequestHelper","updatePoint");
        DbHelper db = new DbHelper(_cont);
        JSONObject point = db.getPoint(_pointId);
        String lat = new String();
        String lon = new String();
        try{
        lat =  point.getString("lat");
        lon =  point.getString("lon");

        Log.i("HTTPrequestHelper",lat + " " + lon);
        } catch (JSONException e) { e.printStackTrace(); }

        Log.i("HTTPrequestHelper request","https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon +"&units=metric&exclude=daily&appid=491a54922af0f56f87b30ee988483263");
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, //получение данных
                "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon +
                        "&units=metric&exclude=daily&appid=491a54922af0f56f87b30ee988483263", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.i("HTTPrequestHelper","onResponse");
                try {
                    //Второй объект будет довольно близко ко времени к времени точки
                    JSONObject hourly = response.getJSONArray("hourly").getJSONObject(1);
                    JSONObject weather = hourly.getJSONArray("weather").getJSONObject(0);

                    DbHelper db = new DbHelper(_cont);
                    JSONObject point = db.getPoint(_pointId);
                    point.put("icon","http://openweathermap.org/img/wn/" + weather.getString("icon") + "@2x.png");
                    point.put("weatherDescription",weather.getString("description"));
                    point.put("temp",hourly.getString("temp"));
                    point.put("feelsLike",hourly.getString("feels_like"));
                    point.put("pressure",hourly.getString("pressure"));
                    point.put("humidity",hourly.getString("humidity"));
                    point.put("dewPoint",hourly.getString("dew_point"));
                    point.put("clouds",hourly.getString("clouds"));
                    point.put("visibility",hourly.getString("visibility"));
                    point.put("windSpeed",hourly.getString("wind_speed"));
                    point.put("windDeg",hourly.getString("wind_deg"));

                    db.updatePointInDb(_pointId,point.toString());

                } catch (JSONException e) { e.printStackTrace(); }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { error.printStackTrace();}
        });

        mRequestQueue.add(request);
    }
}
