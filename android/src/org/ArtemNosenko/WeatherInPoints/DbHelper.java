package org.ArtemNosenko.WeatherInPoints;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


import java.util.Date;
import java.util.Calendar;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
private static final String TAG = "DbHelper";
    public DbHelper(Context context) {
      super(context, "/data/user/0/org.ArtemNosenko.WeatherInPoints/files/QML/OfflineStorage/Databases/cd123cf2b8ce6fdec3d6853ff7304ab6.sqlite", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) { }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void updatePointsInfo(Context context){

        SQLiteDatabase db =  getReadableDatabase();
        Cursor c = db.query("Points",null,null, null, null, null, null);
                        if (c.moveToFirst()) {
                            while ( !c.isAfterLast() ) {
                                String id = c.getString(c.getColumnIndex("id"));
                                if ( isPointRepeatToday(id)) {
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

    public ArrayList<String> getJsonPointStringToNotify(){

        ArrayList<String> stringArray = new ArrayList<String>();

        SQLiteDatabase db =   getReadableDatabase();
        Cursor c = db.query("Points",null,null, null, null, null, null);
                        if (c.moveToFirst()) {
                            while ( !c.isAfterLast() ) {
                                String id = c.getString(c.getColumnIndex("id"));
                                if ( isPointRepeatToday(id)) {
                                    String js = c.getString(c.getColumnIndex("point"));
                                    stringArray.add(js);
                             }
                                c.moveToNext();
                            }
                        }

           return stringArray;
        }

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

                    Log.i("DbHelper isPointRepeatToday",ar.toString() );

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

                } catch (JSONException e) {  Log.i("DbHelper isPointRepeatToday1","exeption "); }
                c.moveToNext();
            }
        }

        Log.i("DbHelper isPointRepeatToday4","" );
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
                                       } catch (JSONException e) {Log.i("DbHelper","exeption");}
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
               } catch (JSONException e) { Log.i("DbHelper","exeption"); }
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

       Log.i("DbHelper", "updatePointInDb");
       int updCount = db.update("Points", cv, "id = ?", selectionArgs);
   }

  }
