package com.example.food_front.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase de utilidad para diagnóstico de integración con MercadoPago
 * Proporciona información útil para depuración y envío de informes de error
 */
public class DiagnosticUtil {
    private static final String TAG = "MPDiagnostic";
    
    /**
     * Recolecta información de diagnóstico del dispositivo y la app
     * @param context Contexto de la aplicación
     * @return Mapa con información de diagnóstico
     */
    public static Map<String, String> collectDiagnosticInfo(Context context) {
        Map<String, String> info = new HashMap<>();
        
        try {
            // Información del dispositivo
            info.put("device_model", Build.MODEL);
            info.put("device_manufacturer", Build.MANUFACTURER);
            info.put("android_version", Build.VERSION.RELEASE);
            info.put("api_level", String.valueOf(Build.VERSION.SDK_INT));
            
            // Información de la aplicación
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            int versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            
            info.put("app_version", versionName);
            info.put("app_version_code", String.valueOf(versionCode));
            
            // URL del servidor de pago
            info.put("payment_server", "backmp.onrender.com");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al recolectar información de diagnóstico", e);
        }
        
        return info;
    }
    
    /**
     * Imprime un log de diagnóstico completo
     * @param context Contexto de la aplicación
     */
    public static void logDiagnosticInfo(Context context) {
        Map<String, String> info = collectDiagnosticInfo(context);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== DIAGNÓSTICO MERCADOPAGO ===\n");
        
        for (Map.Entry<String, String> entry : info.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        Log.d(TAG, sb.toString());
    }
    
    /**
     * Genera un reporte de diagnóstico en formato legible
     * @param context Contexto de la aplicación
     * @return Cadena con el reporte de diagnóstico
     */
    public static String generateDiagnosticReport(Context context) {
        Map<String, String> info = collectDiagnosticInfo(context);
        
        StringBuilder sb = new StringBuilder();
        sb.append("DIAGNÓSTICO MERCADOPAGO\n\n");
        
        // Información del dispositivo
        sb.append("DISPOSITIVO:\n");
        sb.append("  Modelo: ").append(info.get("device_model")).append("\n");
        sb.append("  Fabricante: ").append(info.get("device_manufacturer")).append("\n");
        sb.append("  Android: ").append(info.get("android_version")).append(" (API ").append(info.get("api_level")).append(")\n\n");
        
        // Información de la aplicación
        sb.append("APLICACIÓN:\n");
        sb.append("  Versión: ").append(info.get("app_version")).append(" (").append(info.get("app_version_code")).append(")\n\n");
        
        // Servidor de pago
        sb.append("SERVIDOR DE PAGO:\n");
        sb.append("  URL: ").append(info.get("payment_server")).append("\n");
        sb.append("  Conectividad: ").append(NetworkUtils.canReachMercadoPagoServer() ? "DISPONIBLE" : "NO DISPONIBLE").append("\n");
        
        return sb.toString();
    }
}
