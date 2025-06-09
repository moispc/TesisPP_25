package com.example.food_front.utils;

import android.content.Context;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase para gestionar las operaciones con Cloudinary
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static boolean isInitialized = false;

    // Configuración de Cloudinary
    private static final String CLOUD_NAME = "djp80kwaj";
    private static final String API_KEY = "285359299675698";
    private static final String API_SECRET = "CILwUfSuiDsJ977SrrCvPQcgJz";

    /**
     * Inicializa la configuración de Cloudinary
     * @param context El contexto de la aplicación
     * @return true si la inicialización fue exitosa, false en caso contrario
     */
    public static boolean initialize(Context context) {
        if (isInitialized) {
            return true;
        }

        try {
            // Configurar Cloudinary para subida sin firma
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            // Para subidas sin firma, solo necesitamos el cloud_name
            config.put("secure", "true");

            try {
                // Intentar obtener la instancia actual (verificará si ya está inicializado)
                MediaManager.get();
                // Si llegamos aquí, ya está inicializado
                Log.d(TAG, "MediaManager ya estaba inicializado");
                isInitialized = true;
            } catch (IllegalStateException e) {
                // No está inicializado, así que lo inicializamos
                MediaManager.init(context, config);
                Log.d(TAG, "MediaManager inicializado correctamente para subidas sin firma");
                isInitialized = true;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Cloudinary: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sube una imagen a Cloudinary
     * @param context Contexto de la aplicación
     * @param imageBytes Array de bytes de la imagen
     * @param folderName Carpeta donde guardar la imagen (opcional)
     * @param callback Callback para recibir la URL de la imagen subida
     */
    public static void uploadImage(Context context, byte[] imageBytes, String folderName, final CloudinaryUploadCallback callback) {
        if (!initialize(context)) {
            callback.onError("No se pudo inicializar Cloudinary");
            return;
        }

        try {
            // Creamos una copia de los bytes para evitar modificaciones concurrentes
            final byte[] imageBytesToUpload = imageBytes.clone();

            // Timestamp único para el nombre del archivo
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Usamos un nombre de archivo más seguro, sin espacios ni caracteres especiales
            String uniqueFileName = "user_" + timestamp;

            // Realizamos la subida - Simplificamos las opciones para reducir problemas de firma
            String requestId = MediaManager.get().upload(imageBytesToUpload)
                    .option("folder", "profile_images")
                    .option("public_id", uniqueFileName)
                    .unsigned("ml_default") // Usar un preset de subida sin firma para evitar problemas de autenticación
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Comenzando subida a Cloudinary...");
                            callback.onStart();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            double progress = (double) bytes / totalBytes;
                            callback.onProgress(progress);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("url");
                            String secureUrl = (String) resultData.get("secure_url");
                            // Preferimos la URL segura
                            String finalUrl = secureUrl != null ? secureUrl : url;

                            Log.d(TAG, "Subida exitosa a Cloudinary. URL: " + finalUrl);
                            callback.onSuccess(finalUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Error en subida a Cloudinary: " + error.getDescription());
                            callback.onError(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d(TAG, "Reprogramando subida a Cloudinary: " + error.getDescription());
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "Error al subir imagen a Cloudinary: " + e.getMessage());
            callback.onError("Error: " + e.getMessage());
        }
    }

    /**
     * Interface para el callback de subida a Cloudinary
     */
    public interface CloudinaryUploadCallback {
        void onStart();
        void onProgress(double progress);
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
}
