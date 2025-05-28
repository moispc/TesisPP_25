package com.example.food_front.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ProfileManager {
    private static final String PREF_NAME = "user_info";
    private static final String KEY_NAME = "nombre";
    private static final String KEY_SURNAME = "apellido";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "telefono";
    private static final String KEY_PROFILE_IMAGE = "imagen_perfil_url";
    private static final String KEY_LAST_IMAGE_UPDATE = "last_image_update";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ProfileManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Guardar los datos del usuario
    public void saveInfo(String name, String surname, String email, String phone) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_SURNAME, surname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
        Log.d("auth", "Nombre guardado despues del login:" + name );
        Log.d("auth", "Apellido guardado despues del login:" + surname );
        Log.d("auth", "Email guardado despues del login:" + email );
        Log.d("auth", "Telefono guardado despues del login:" + phone );
    }

    // Guardar los datos del usuario incluyendo la imagen de perfil
    public void saveInfo(String name, String surname, String email, String phone, String profileImageUrl) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_SURNAME, surname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_PROFILE_IMAGE, profileImageUrl);
        editor.apply();
        Log.d("auth", "Nombre guardado despues del login:" + name );
        Log.d("auth", "Apellido guardado despues del login:" + surname );
        Log.d("auth", "Email guardado despues del login:" + email );
        Log.d("auth", "Telefono guardado despues del login:" + phone );
        Log.d("auth", "URL de imagen de perfil guardada:" + profileImageUrl);
    }    // Guardar solo la URL de la imagen de perfil
    public void saveProfileImageUrl(String profileImageUrl) {
        // Asegurarse de guardar la URL base sin parámetros de timestamp
        String cleanUrl = profileImageUrl;
        if (profileImageUrl != null && profileImageUrl.contains("?")) {
            cleanUrl = profileImageUrl.substring(0, profileImageUrl.indexOf("?"));
        }
        
        // Verificar si realmente cambió la URL
        String currentUrl = getProfileImageUrl();
        if (currentUrl == null || !currentUrl.equals(cleanUrl)) {
            Log.d("ImagenPerfil", "URL realmente cambió de: " + currentUrl + " a: " + cleanUrl);
            
            // Guardar la nueva URL
            editor.putString(KEY_PROFILE_IMAGE, cleanUrl);
            updateLastImageTime(); // Actualizar el timestamp de la última modificación
            editor.apply();
            
            // Forzar un cambio adicional para que el sistema detecte la modificación
            editor.putLong("last_url_change", System.currentTimeMillis());
            editor.apply();
            
            Log.d("auth", "URL de imagen de perfil actualizada:" + cleanUrl);            // No intentamos usar el broadcast receiver desde aquí para evitar errores
            // Se manejará la actualización directamente desde los métodos específicos en los fragmentos
        } else {
            Log.d("auth", "La URL de la imagen no ha cambiado, no se actualiza");
        }
    }

    // Leer los datos del usuario
    public String getInfo() {
        return sharedPreferences.getString(PREF_NAME, null);
    }

    public String getName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public String getSurname() {
        return sharedPreferences.getString(KEY_SURNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }    public String getProfileImageUrl() {
        String baseUrl = sharedPreferences.getString(KEY_PROFILE_IMAGE, null);
        return baseUrl;
    }
      // Obtener URL con timestamp para forzar recarga
    public String getProfileImageUrlWithTimestamp() {
        String baseUrl = getProfileImageUrl();
        if (baseUrl != null && !baseUrl.isEmpty()) {
            // Usar un timestamp aleatorio para asegurarnos de nunca usar caché
            return baseUrl + "?nocache=" + Math.random() + "&t=" + System.currentTimeMillis();
        }
        return baseUrl;
    }

    // Actualizar el tiempo de la última actualización de imagen
    public void updateLastImageTime() {
        editor.putLong(KEY_LAST_IMAGE_UPDATE, System.currentTimeMillis());
        editor.apply();
    }

    // Obtener el tiempo de la última actualización de imagen
    public long getLastImageUpdate() {
        return sharedPreferences.getLong(KEY_LAST_IMAGE_UPDATE, 0);
    }

    // Borrar la session
    public void clearInfo() {
        editor.clear();
        editor.apply();
    }
}

