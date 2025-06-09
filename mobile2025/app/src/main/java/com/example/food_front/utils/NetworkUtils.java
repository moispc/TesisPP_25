package com.example.food_front.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Utilidad para verificar el estado de la conexión a Internet
 */
public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    /**
     * Verifica si el dispositivo tiene una conexión a Internet activa
     * @param context Contexto de la aplicación
     * @return true si hay una conexión a Internet activa, false en caso contrario
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Comprueba si se puede acceder al servidor de MercadoPago
     * @return true si se puede acceder, false en caso contrario
     */
    public static boolean canReachMercadoPagoServer() {
        return checkUrlAvailability("https://backmp.onrender.com", 5000);
    }

    /**
     * Verifica si una URL está disponible con un timeout específico
     * @param urlString URL a verificar
     * @param timeout Tiempo máximo de espera en milisegundos
     * @return true si la URL está disponible, false en caso contrario
     */
    public static boolean checkUrlAvailability(final String urlString, int timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);
                    connection.setRequestMethod("HEAD");
                    int responseCode = connection.getResponseCode();
                    Log.d(TAG, "URL " + urlString + " devuelve código: " + responseCode);
                    return (responseCode >= 200 && responseCode < 400);
                } catch (IOException e) {
                    Log.e(TAG, "Error al verificar disponibilidad de " + urlString, e);
                    return false;
                }
            }
        });

        try {
            return future.get(timeout + 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Timeout al verificar disponibilidad de " + urlString, e);
            future.cancel(true);
            return false;
        } finally {
            executor.shutdownNow();
        }
    }
}
