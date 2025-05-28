package com.example.food_front.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Receptor de broadcast para notificar cuando la imagen de perfil ha sido actualizada.
 * Esto permite que cualquier fragmento o actividad pueda registrarse para ser notificado
 * cuando la imagen de perfil cambie y así actualizar su interfaz.
 */
public class ImageUpdateReceiver extends BroadcastReceiver {
    
    public static final String ACTION_IMAGE_UPDATED = "com.example.food_front.IMAGE_UPDATED";
    public static final String EXTRA_IMAGE_URL = "image_url";
    
    private ImageUpdateListener listener;
    
    /**
     * Constructor que recibe el listener para notificar cambios
     */
    public ImageUpdateReceiver(ImageUpdateListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_IMAGE_UPDATED.equals(intent.getAction())) {
            String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
            Log.d("ImageUpdateReceiver", "Recibida notificación de actualización de imagen: " + imageUrl);
            
            if (listener != null) {
                listener.onImageUpdated(imageUrl);
            }
        }
    }
    
    /**
     * Registra este receptor para recibir notificaciones
     */
    public void register(Context context) {
        IntentFilter filter = new IntentFilter(ACTION_IMAGE_UPDATED);
        context.registerReceiver(this, filter);
    }
    
    /**
     * Desregistra este receptor
     */
    public void unregister(Context context) {
        try {
            context.unregisterReceiver(this);
        } catch (Exception e) {
            Log.e("ImageUpdateReceiver", "Error al desregistrar: " + e.getMessage());
        }
    }
    
    /**
     * Envía una notificación a todos los receptores registrados
     */
    public static void notifyImageUpdated(Context context, String imageUrl) {
        Intent intent = new Intent(ACTION_IMAGE_UPDATED);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        context.sendBroadcast(intent);
        Log.d("ImageUpdateReceiver", "Enviada notificación de actualización de imagen: " + imageUrl);
    }
    
    /**
     * Interfaz para escuchar actualizaciones de imagen
     */
    public interface ImageUpdateListener {
        void onImageUpdated(String imageUrl);
    }
}
