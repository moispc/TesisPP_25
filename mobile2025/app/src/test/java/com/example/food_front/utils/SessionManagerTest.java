package com.example.food_front.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la clase SessionManager
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_INIT_POINT = "last_init_point";
    
    @Mock
    Context mockContext;
    
    @Mock
    Context mockAppContext;
    
    @Mock
    SharedPreferences mockSharedPreferences;
    
    @Mock
    SharedPreferences.Editor mockEditor;
    
    private SessionManager sessionManager;
    
    @Before
    public void setUp() {
        // Configurar el contexto mock
        when(mockContext.getApplicationContext()).thenReturn(mockAppContext);
        when(mockAppContext.getSharedPreferences(eq(PREF_NAME), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        
        // Crear la instancia de SessionManager con el contexto mock
        sessionManager = new SessionManager(mockContext);
    }
    
    @Test
    public void saveToken_storesTokenInSharedPreferences() {
        // Preparar
        String testToken = "test_auth_token_12345";
        
        // Ejecutar
        sessionManager.saveToken(testToken);
        
        // Verificar
        verify(mockEditor).putString(KEY_TOKEN, testToken);
        verify(mockEditor).apply();
    }
    
    @Test
    public void getToken_retrievesTokenFromSharedPreferences() {
        // Preparar
        String expectedToken = "saved_auth_token_12345";
        when(mockSharedPreferences.getString(eq(KEY_TOKEN), eq(null))).thenReturn(expectedToken);
        
        // Ejecutar
        String actualToken = sessionManager.getToken();
        
        // Verificar
        assertEquals(expectedToken, actualToken);
    }
    
    @Test
    public void saveEmail_storesEmailInSharedPreferences() {
        // Preparar
        String testEmail = "test@example.com";
        
        // Ejecutar
        sessionManager.saveEmail(testEmail);
        
        // Verificar
        verify(mockEditor).putString(KEY_EMAIL, testEmail);
        verify(mockEditor).apply();
    }
    
    @Test
    public void getUserEmail_retrievesEmailFromSharedPreferences() {
        // Preparar
        String expectedEmail = "user@example.com";
        when(mockSharedPreferences.getString(eq(KEY_EMAIL), eq(null))).thenReturn(expectedEmail);
        
        // Ejecutar
        String actualEmail = sessionManager.getUserEmail();
        
        // Verificar
        assertEquals(expectedEmail, actualEmail);
    }
    
    @Test
    public void saveLastInitPoint_storesInitPointInSharedPreferences() {
        // Preparar
        String testInitPoint = "https://mercadopago.com/checkout/12345";
        
        // Ejecutar
        sessionManager.saveLastInitPoint(testInitPoint);
        
        // Verificar
        verify(mockEditor).putString(KEY_LAST_INIT_POINT, testInitPoint);
        verify(mockEditor).apply();
    }
    
    @Test
    public void clearSession_removesAllValuesFromSharedPreferences() {
        // Ejecutar
        sessionManager.clearSession();
        
        // Verificar
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
    
    @Test
    public void logout_callsClearSession() {
        // Ejecutar
        sessionManager.logout();
        
        // Verificar
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
}
