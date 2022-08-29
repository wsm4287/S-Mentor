package com.example.s_mentor.notification;

import android.content.Context;
import android.os.StrictMode;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendMessage {
    private static String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY = "key=AAAAQXnj5Eg:APA91bH7_EruDsX1bfgGx087YFR-XZ0qC5sEBLy71YeUQzxm-7KbCxuaeKaXKH3jS-4_-8ei7_ybdXKEXsUJxm7o1QGczC-P-H_JUFRK5rWeonnJq-SxlG4pjYiwxR1Tlve9wKTONwQb";

    public static void notification(Context context, String token, String title, String message, String name){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("to", token);
            jsonObject.put("name", name);
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);
            JSONObject data = new JSONObject();
            data.put("name", name);
            //data.put("image", encodedImage);
            jsonObject.put("notification", notification);
            jsonObject.put("data", data);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("FCM " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Context-Type", "application/json");
                    params.put("Authorization", SERVER_KEY);
                    return params;
                }
            };

            queue.add(jsonObjectRequest);

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
