package com.example.food_front;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Clase Application personalizada para mantener un estado global de la aplicación.
 * Permite acceder al contexto de la aplicación desde cualquier parte del código.
 */
public class MyApplication extends Application {
    
    private static MyApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d("MyApplication", "Aplicación iniciada");
    }
    
    /**
     * Obtiene la instancia de la aplicación.
     */
    public static MyApplication getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el contexto global de la aplicación.
     */
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
}
