package com.example.food_front.utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Administrador para limpiar la caché de imágenes y forzar recarga
 */
public class ImageCacheManager {
    
    private static final String TAG = "ImageCacheManager";
    
    /**
     * Limpia toda la caché de Glide
     */
    public static void clearGlideCache(final Context context) {
        try {
            Log.d(TAG, "Limpiando caché de Glide...");
            // Ejecutar en un hilo secundario para no bloquear UI
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Limpiar memoria caché
                        Glide.get(context.getApplicationContext()).clearMemory();
                        
                        // Limpiar caché de disco (debe ejecutarse en un hilo secundario)
                        Glide.get(context.getApplicationContext()).clearDiskCache();
                        Log.d(TAG, "Caché de Glide limpiada con éxito");
                    } catch (Exception e) {
                        Log.e(TAG, "Error al limpiar caché: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar limpieza de caché: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un archivo específico de la caché de Glide
     */
    public static void removeFileFromCache(Context context, String url) {
        try {
            Log.d(TAG, "Intentando eliminar archivo de caché: " + url);
            File cacheDir = Glide.getPhotoCacheDir(context);
            if (cacheDir != null && cacheDir.exists()) {
                // Eliminar archivos que podrían contener la URL
                for (File file : cacheDir.listFiles()) {
                    if (file.getName().contains(url.hashCode() + "")) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "Archivo " + file.getName() + " eliminado: " + deleted);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar archivo de caché: " + e.getMessage());
        }
    }
}
