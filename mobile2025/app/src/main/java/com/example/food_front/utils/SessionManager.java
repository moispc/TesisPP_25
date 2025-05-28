package com.example.food_front.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Guardar el token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
        Log.d("auth", "Token guardado despues del login:" + token );
    }

    // leer el token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Borrar la session
    public void clearSession() {
        editor.clear();
        editor.apply();
    }


}
