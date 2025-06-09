package com.example.food_front.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.food_front.models.MercadoPagoPreference;

/**
 * Clase utilitaria para probar la conexión con el servicio de MercadoPago
 * y verificar que las respuestas son las esperadas.
 */
public class MercadoPagoTester {
    private static final String TAG = "MPTester";
    
    /**
     * Realizar una prueba de conexión con el servicio de MercadoPago
     * @param context Contexto de la aplicación
     * @param email Email para la prueba
     * @param showToasts Si se deben mostrar toasts con los resultados
     */
    public static void testConnection(Context context, String email, boolean showToasts) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        
        // Primero verificamos si el servidor está disponible
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean serverReachable = NetworkUtils.checkUrlAvailability("backmp.onrender.com", 5000);
                
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (serverReachable) {
                            Log.d(TAG, "✅ Servidor alcanzable");
                            if (showToasts) {
                                Toast.makeText(context, "✅ Servidor de pagos disponible", Toast.LENGTH_SHORT).show();
                            }
                            
                            // Ahora probamos la creación de preferencia
                            testCreatePreference(context, email, showToasts);
                        } else {
                            Log.e(TAG, "❌ Servidor no alcanzable");
                            if (showToasts) {
                                Toast.makeText(context, "❌ Servidor de pagos no disponible", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }).start();
    }
    
    /**
     * Probar la creación de una preferencia de pago
     */
    private static void testCreatePreference(Context context, String email, boolean showToasts) {
        MercadoPagoService mercadoPagoService = new MercadoPagoService(context);
        
        mercadoPagoService.createPreference(email, new MercadoPagoService.MercadoPagoCallback() {
            @Override
            public void onSuccess(MercadoPagoPreference preference) {
                Log.d(TAG, "✅ Preferencia creada correctamente");
                Log.d(TAG, "URL: " + preference.getInitPoint());
                Log.d(TAG, "ID Preferencia: " + preference.getPreferenceId());
                Log.d(TAG, "ID Request: " + preference.getPaymentRequestId());
                
                if (showToasts) {
                    Toast.makeText(context, "✅ Preferencia de pago creada", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "❌ Error al crear preferencia: " + errorMessage);
                
                if (showToasts) {
                    Toast.makeText(context, "❌ Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
