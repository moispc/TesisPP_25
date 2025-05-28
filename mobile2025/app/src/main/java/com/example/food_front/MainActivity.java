package com.example.food_front;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.food_front.databinding.ActivityMainBinding;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private ProfileManager profileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());        // Initialize SessionManager and ProfileManager
        sessionManager = new SessionManager(this);
        profileManager = new ProfileManager(this);

        // Cargar el LoginFragment al inicio

        // Check if a token exists to determine the initial fragment
        if (sessionManager.getToken() != null) {
            mostrarHome(); // Show HomeFragment if logged in
        } else {
            mostrarLogin(); // Show LoginFragment if not logged in
        }

//        mostrarLogin(); // Carga el LoginFragment al iniciar la aplicación


        binding.bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    mostrarHome(); // Show HomeFragment
                    return true;
                } else if (itemId == R.id.profile || itemId == R.id.menu || itemId == R.id.carrito) {
                    if (sessionManager.getToken() == null ){
                        mostrarLogin();
                        return false;
                    } else {
                        if (itemId == R.id.profile){
                            mostrarPerfil();
                            return true;
                        }else if (itemId == R.id.menu ){
                            mostrarProductos();
                            return true;
                        }else if (itemId == R.id.carrito){
                            mostrarCarrito(); // Show CartFragment
                            return true;
                        }
                    }
                } else if (itemId == R.id.contact) {
                    mostrarContact(); // Show ContactFragment
                    return true;
                }
                return false;
            }
        });
    }

    public void mostrarLogin() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new LoginFragment());
        fragmentTransaction.commit();
    }
    public void mostrarHome() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new HomeFragment(), "HomeFragment");
        fragmentTransaction.commit();
    }

    public void mostrarPerfil() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ProfileFragment());
        fragmentTransaction.commit();
    }

    public void mostrarProductos() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ProductsFragment());
        fragmentTransaction.commit();
    }

    public void mostrarCarrito() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new CartFragment());
        fragmentTransaction.commit();
    }

    public void mostrarContact() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container_view, new ContactFragment());
        fragmentTransaction.commit();
    }

    // Método para actualizar la imagen de perfil en todos los fragmentos
    public void actualizarImagenPerfil(String imageUrl) {
        try {
            // Limpiar caché primero
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(this);
            
            // Guarda la nueva URL en el ProfileManager
            ProfileManager profileManager = new ProfileManager(this);
            profileManager.saveProfileImageUrl(imageUrl);
            
            Log.d("MainActivity", "Actualizando imagen de perfil en todos los fragmentos: " + imageUrl);
            
            // Actualizar todos los fragmentos directamente en el hilo UI
            actualizarFragmentosDirectamente(imageUrl);
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error al actualizar imagen de perfil: " + e.getMessage());
        }
    }
    
    /**
     * Método simplificado y seguro para actualizar los fragmentos directamente en el hilo UI
     */
    private void actualizarFragmentosDirectamente(String imageUrl) {
        try {
            runOnUiThread(() -> {
                try {
                    // Actualizar HomeFragment si está visible
                    HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                            .findFragmentByTag("HomeFragment");
                    if (homeFragment != null) {
                        homeFragment.actualizarImagenPerfil(imageUrl);
                    }
                    
                    // Intentar actualizar ProfileFragment si está visible
                    for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
                        if (fragment instanceof ProfileFragment && fragment.isVisible()) {
                            ((ProfileFragment)fragment).actualizarImagenDePerfil(imageUrl);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error al actualizar fragmentos directamente: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error en actualizarFragmentosDirectamente: " + e.getMessage());
        }
    }
    
    /**
     * Método para forzar la recarga de todas las imágenes en la aplicación.
     * Este método se puede llamar desde cualquier fragmento cuando se necesite
     * actualizar las imágenes en toda la aplicación.
     * Método simplificado para actualizar todas las imágenes sin usar hilos extras
     */
    public void actualizarTodasLasImagenes() {
        try {
            // Opción más segura: Simplemente llamar al método seguro que ya tenemos
            actualizarFragmentosDirectamente(profileManager.getProfileImageUrl());
        } catch (Exception e) {
            Log.e("MainActivity", "Error en actualizarTodasLasImagenes: " + e.getMessage());
        }
    }
}
