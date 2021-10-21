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


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;



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

      DBHelper dbHelper = new DBHelper(context);
      JSONObject jo = dbHelper.getFirstPointInTime();

  //    Log.i(TAG,jo.toString());

      Date pDate = new Date();
      try {
        pDate.setHours(Integer.parseInt(jo.getString("hour")));
        pDate.setMinutes(Integer.parseInt(jo.getString("minute")));
       }catch (JSONException e) {Log.i("MyStartServiceReceiver","JSONException");}

//   Log.i(TAG, String.valueOf(pDate.getTime()/1000 - System.currentTimeMillis()/1000));

      if (pDate.getTime() < System.currentTimeMillis() + 1.5*3600*1000 &&  pDate.getTime()  >= System.currentTimeMillis() + 3600*1000) {//updateDb

       SQLiteDatabase db =  dbHelper.getReadableDatabase();
       Cursor c = db.query("Points",null,null, null, null, null, null);
                       if (c.moveToFirst()) {
                           while ( !c.isAfterLast() ) {
                               String id = c.getString(c.getColumnIndex("id"));
                               if (dbHelper.isPointRepeatToday(id)) {
                                   try {
                                       String js = c.getString(c.getColumnIndex("point"));
                                       JSONObject joPointToUpdate = new JSONObject(js);

                                       Log.i(TAG, joPointToUpdate.getString("pointName") + " " + "updateDbOnReceive");

                                       HTTPrequestHelper httpHelper = new HTTPrequestHelper(context);
                                       httpHelper.updatePoint(id);

                                   } catch (JSONException e) {
                                       Log.i(TAG, "exeption");
                                   }
                               }

                               c.moveToNext();
                           }
                       }
      }

//Каждый пол часа alarm. Если до ближайшей точки менее 1.5  но более 1 часов, то update всех точек.
//Если Ближайшая прошла, то игнор. Если до ближайшей точки меньше часа, но больше получаса, то notify
      if (pDate.getTime() > System.currentTimeMillis() &&  pDate.getTime()  <= System.currentTimeMillis() + 3600*1000)
      {//notify
       SQLiteDatabase db =  dbHelper.getReadableDatabase();
       Cursor c = db.query("Points",null,null, null, null, null, null);
        int notifyId = 1;
                       if (c.moveToFirst()) {
                           while ( !c.isAfterLast() ) {
                               String id = c.getString(c.getColumnIndex("id"));
                               if (dbHelper.isPointRepeatToday(id)) {
                               try {
                                   String js = c.getString(c.getColumnIndex("point"));
                                   JSONObject joPoint = new JSONObject(js);

                                   Log.i(TAG,joPoint.getString("pointName") + " " + "notifyOnReceive");

                                   notifyy(context,joPoint.getString("pointName") , "Temp: " + joPoint.getString("temp"),notifyId);
                                   notifyId++;
                               } catch (JSONException e) {
                               Log.i(TAG,"exeption1");
                               }
                               }
                               c.moveToNext();
                           }
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

class DBHelper extends SQLiteOpenHelper {

   public DBHelper(Context context) {
     super(context, "/data/user/0/org.ArtemNosenko.WeatherInPoints/files/QML/OfflineStorage/Databases/cd123cf2b8ce6fdec3d6853ff7304ab6.sqlite", null, 1);
   }
   @Override
   public void onCreate(SQLiteDatabase db) { }
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

   public boolean isPointRepeatToday(String pointID){

       SQLiteDatabase db =  getReadableDatabase();
       String[] column = new String[]{"point", "daysToRepeat"};
       String selection = "id == ?";
       String[]  selectionArgs = new String[] { pointID };


       Cursor c = db.query("Points", column, selection , selectionArgs, null, null, null);

       JSONArray ar = new JSONArray();
       boolean isRepeat = false;

       if (c.moveToFirst()) {
           while ( !c.isAfterLast() ) {
               String js = c.getString(c.getColumnIndex("daysToRepeat"));
               try {
                   ar = new JSONArray(js);

                   Calendar rightNow = Calendar.getInstance();

                   int curDay = rightNow.get(Calendar.DAY_OF_WEEK);
                   //Week day order
                   if (curDay != 1)
                       curDay = curDay - 2;
                   else
                       curDay = 6;
                   JSONObject joRepeat = new JSONObject();

                   Log.i("DBHelper isPointRepeatToday",ar.toString() );

                   String repeatStr = ar.getString(curDay);
                   joRepeat = new  JSONObject(repeatStr);
                   isRepeat = joRepeat.getBoolean("repeat");

                   if (isRepeat == true)
                   {

                        js = c.getString(c.getColumnIndex("point"));

                        JSONObject joPoint = new JSONObject(js);
                        boolean isActive = joPoint.getBoolean("activated");
                           if (!isActive)
                               isRepeat = false;
                   }

               } catch (JSONException e) {  Log.i("DBHelper isPointRepeatToday1","exeption "); }
               c.moveToNext();
           }
       }

       Log.i("DBHelper isPointRepeatToday4","" );
        return isRepeat ;
   }


   public JSONObject getFirstPointInTime(){
              SQLiteDatabase db =  getReadableDatabase();
              Cursor c = db.query("Points", null,null, null, null, null, null);
              JSONObject obj = new JSONObject();
              long closestTime = -1;
                              if (c.moveToFirst()) {
                                  while ( !c.isAfterLast() ) {
                                      String js = c.getString(c.getColumnIndex("point"));
                                      try {
                                          JSONObject jo = new JSONObject(js);
                                          Date pDate = new Date();
                                          pDate.setHours(Integer.parseInt(jo.getString("hour")));
                                          pDate.setMinutes(Integer.parseInt(jo.getString("minute")));

                                          String id = c.getString(c.getColumnIndex("id"));
                                          boolean pointIsActive = isPointRepeatToday(id);

                                          if ((closestTime == -1 || pDate.getTime() < closestTime) && pointIsActive)
                                          {
                                             obj = jo;
                                             closestTime = pDate.getTime();
                                          }
                                      } catch (JSONException e) {Log.i("DBHelper","exeption");}
                                      c.moveToNext();
                                  }
                              }
               return obj;
       }

  public JSONObject getPoint(String id){
      SQLiteDatabase db =  getReadableDatabase();
      String[] column = new String[]{"point"};
      String selection = "id == ?";
      String[]  selectionArgs = new String[] { id };


      Cursor c = db.query("Points", column, selection , selectionArgs, null, null, null);
      JSONObject obj = new JSONObject();
      if (c.moveToFirst()) {
          while ( !c.isAfterLast() ) {
              String js = c.getString(c.getColumnIndex("point"));
              try {
                  obj = new JSONObject(js);
              } catch (JSONException e) { Log.i("DBHelper","exeption"); }
              c.moveToNext();
          }
      }
      return  obj;
  }

  public  void updatePointInDb(String id, String pointStr){

      SQLiteDatabase db = getWritableDatabase();

      ContentValues cv = new ContentValues();
      cv.put("Point", pointStr);

      String[]  selectionArgs = new String[] { id };

      Log.i("DBHelper", "updatePointInDb");
      int updCount = db.update("Points", cv, "id = ?", selectionArgs);
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
        DBHelper db = new DBHelper(_cont);
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

                    DBHelper db = new DBHelper(_cont);
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
