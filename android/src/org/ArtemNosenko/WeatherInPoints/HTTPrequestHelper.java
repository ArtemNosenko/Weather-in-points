package org.ArtemNosenko.WeatherInPoints;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;


public class HTTPrequestHelper{
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
