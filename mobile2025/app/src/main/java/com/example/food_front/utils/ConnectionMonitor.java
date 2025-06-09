package com.example.food_front.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Monitor de conexión a Internet para detectar cambios en el estado de la red.
 * Útil para la integración con MercadoPago, que requiere una conexión estable.
 */
public class ConnectionMonitor {
    private static final String TAG = "ConnectionMonitor";
    private final ConnectivityManager connectivityManager;
    private final ConnectivityListener listener;
    private final Context context;
    private boolean isRegistered = false;

    /**
     * Interfaz para escuchar cambios en el estado de la conexión
     */
    public interface ConnectivityListener {
        void onConnected();
        void onDisconnected();
    }

    /**
     * Constructor
     * @param context Contexto de la aplicación
     * @param listener Listener para recibir notificaciones de cambios en la conexión
     */
    public ConnectionMonitor(Context context, ConnectivityListener listener) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    /**
     * Inicia el monitoreo de la conexión
     */
    public void startMonitoring() {
        if (isRegistered) {
            return;
        }

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        if (connectivityManager != null) {
            try {
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                isRegistered = true;
                Log.d(TAG, "Monitoreo de conexión iniciado");
                
                // Verificar estado actual
                if (NetworkUtils.isNetworkAvailable(context)) {
                    if (listener != null) {
                        listener.onConnected();
                    }
                } else {
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al registrar callback de red", e);
            }
        }
    }

    /**
     * Detiene el monitoreo de la conexión
     */
    public void stopMonitoring() {
        if (!isRegistered) {
            return;
        }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isRegistered = false;
            Log.d(TAG, "Monitoreo de conexión detenido");
        } catch (Exception e) {
            Log.e(TAG, "Error al detener monitoreo", e);
        }
    }

    /**
     * Callback para recibir eventos de cambios en la red
     */
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.d(TAG, "Red disponible");
            if (listener != null) {
                listener.onConnected();
            }
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Log.d(TAG, "Red perdida");
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    };
}
