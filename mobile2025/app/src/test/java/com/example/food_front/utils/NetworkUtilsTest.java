package com.example.food_front.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la clase NetworkUtils
 */
@RunWith(MockitoJUnitRunner.class)
public class NetworkUtilsTest {
    
    @Mock
    Context mockContext;
    
    @Mock
    ConnectivityManager mockConnectivityManager;
    
    @Mock
    NetworkInfo mockNetworkInfo;
    
    @Test
    public void isNetworkAvailable_withConnectedNetwork_returnsTrue() {
        // Configurar mocks
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);
        
        // Ejecutar el método que queremos probar
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        
        // Verificar que devuelve true cuando hay una red conectada
        assertTrue(result);
    }
    
    @Test
    public void isNetworkAvailable_withDisconnectedNetwork_returnsFalse() {
        // Configurar mocks
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(false);
        
        // Ejecutar el método que queremos probar
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        
        // Verificar que devuelve false cuando la red no está conectada
        assertFalse(result);
    }
    
    @Test
    public void isNetworkAvailable_withNullNetworkInfo_returnsFalse() {
        // Configurar mocks
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
        
        // Ejecutar el método que queremos probar
        boolean result = NetworkUtils.isNetworkAvailable(mockContext);
        
        // Verificar que devuelve false cuando no hay información de red
        assertFalse(result);
    }
    
    @Test
    public void checkUrlAvailability_withTimeout_respectsTimeout() {
        // Esta prueba verifica que el método respeta el tiempo de espera especificado
        long startTime = System.currentTimeMillis();
        
        // Intentar conectarse a una URL que no responde, con un timeout pequeño
        boolean result = NetworkUtils.checkUrlAvailability("non.existent.url.example.com", 1000);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verificar que devuelve false para una URL que no existe
        assertFalse(result);
        
        // Verificar que respeta el timeout aproximadamente (con un margen de 500ms)
        assertTrue("El tiempo de espera no fue respetado", duration < 1500);
    }
}
