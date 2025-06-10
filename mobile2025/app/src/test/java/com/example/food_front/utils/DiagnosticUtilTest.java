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
 * Pruebas unitarias para la clase DiagnosticUtil
 */
@RunWith(MockitoJUnitRunner.class)
public class DiagnosticUtilTest {

    @Mock
    Context mockContext;
    
    @Mock
    SharedPreferences mockSharedPreferences;
    
    @Mock
    SharedPreferences.Editor mockEditor;

    @Before
    public void setUp() {
        // Configurar el SharedPreferences mock
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }

    @Test
    public void collectDiagnosticInfo_returnsNonEmptyMap() {
        // Configurar el paquete y nombre de versión para el contexto mock
        when(mockContext.getPackageName()).thenReturn("com.example.food_front");
        
        // Ejecutar el método bajo prueba
        var result = DiagnosticUtil.collectDiagnosticInfo(mockContext);
        
        // Verificar que el mapa no está vacío
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que contiene las claves esperadas
        assertTrue(result.containsKey("device_model"));
        assertTrue(result.containsKey("device_manufacturer"));
        assertTrue(result.containsKey("android_version"));
        assertTrue(result.containsKey("api_level"));
        assertTrue(result.containsKey("payment_server"));
    }

    @Test
    public void generateDiagnosticReport_containsExpectedSections() {
        // Configurar el paquete y nombre de versión para el contexto mock
        when(mockContext.getPackageName()).thenReturn("com.example.food_front");
        
        // Ejecutar el método bajo prueba
        String report = DiagnosticUtil.generateDiagnosticReport(mockContext);
        
        // Verificar que el informe contiene las secciones esperadas
        assertNotNull(report);
        assertTrue(report.contains("DIAGNÓSTICO"));
        assertTrue(report.contains("DISPOSITIVO"));
        assertTrue(report.contains("APLICACIÓN"));
        assertTrue(report.contains("SERVIDOR"));
    }

    @Test
    public void logDiagnosticInfo_callsCollectDiagnosticInfo() {
        // Esta prueba verifica que el método logDiagnosticInfo llama a collectDiagnosticInfo
        
        // No podemos verificar directamente la llamada a Log.d en una prueba unitaria estándar,
        // pero podemos verificar que se recopila la información de diagnóstico
        
        // Configurar el contexto mock
        when(mockContext.getPackageName()).thenReturn("com.example.food_front");
        
        // Ejecutar el método bajo prueba
        DiagnosticUtil.logDiagnosticInfo(mockContext);
        
        // El método no tiene valor de retorno, así que esto simplemente verifica
        // que no se lanza ninguna excepción
        
        // Idealmente, podríamos verificar con un espía o un mock que collectDiagnosticInfo
        // fue llamado, pero como es un método estático, es más complicado
        // En un entorno real, podríamos usar PowerMockito o similar
    }
}
