package com.example.food_front.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_INIT_POINT = "last_init_point";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        Context appContext = context.getApplicationContext();
        sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Log.d("SessionManager", "SessionManager creado con contexto: " + appContext);
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

    // Guardar el email
    public void saveEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
        Log.d("SessionManager", "Email guardado en SharedPreferences: " + email);
    }

    // Leer el email
    public String getUserEmail() {
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        Log.d("SessionManager", "getUserEmail() devuelve: " + email);
        return email;
    }

    // Guardar la última URL de pago (init_point)
    public void saveLastInitPoint(String initPoint) {
        editor.putString(KEY_LAST_INIT_POINT, initPoint);
        editor.apply();
        Log.d("SessionManager", "init_point guardado: " + initPoint);
    }

    // Obtener la última URL de pago (init_point)
    public String getLastInitPoint() {
        return sharedPreferences.getString(KEY_LAST_INIT_POINT, null);
    }

    // Borrar la session
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Método logout como alias de clearSession para mayor legibilidad
    public void logout() {
        clearSession();
        Log.d("auth", "Sesión cerrada correctamente");
    }
}
