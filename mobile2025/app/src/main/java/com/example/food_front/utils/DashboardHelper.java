package com.example.food_front.utils;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class DashboardHelper {
    public interface DashboardCallback {
        void onSuccess(JSONArray pedidos);
        void onError(String error);
    }

    public static void getUltimoPedido(Context context, String token, DashboardCallback callback) {
        String url = "https://backmobile1.onrender.com/appCART/ver_dashboard/";
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    if (response.has("results")) {
                        JSONArray pedidos = response.getJSONArray("results");
                        callback.onSuccess(pedidos);
                    } else {
                        callback.onError("No hay resultados en la respuesta");
                    }
                } catch (Exception e) {
                    callback.onError("Error parseando respuesta: " + e.getMessage());
                }
            },
            error -> {
                String msg = "Error al obtener pedidos";
                if (error.networkResponse != null) {
                    msg += ". Código: " + error.networkResponse.statusCode;
                }
                callback.onError(msg);
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        queue.add(request);
    }
}
