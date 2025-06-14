package com.example.food_front;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public class DatosEntregaFragment extends Fragment {

    public DatosEntregaFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_datos_entrega, container, false);

        // Obtener la dirección guardada en el login
        EditText editTextDireccion = view.findViewById(R.id.editTextDireccionEntrega);
        ProfileManager profileManager = new ProfileManager(requireContext());
        String direccion = profileManager.getAddress();
        if (direccion != null && !direccion.isEmpty()) {
            editTextDireccion.setText(direccion);
        }

        // Asignar valores aleatorios a tiempo y costo de envío
        TextView tvTiempo = view.findViewById(R.id.tvSubTitle2);
        TextView tvCosto = view.findViewById(R.id.tvSubTitle3);
        int minTiempo = 20 + (int)(Math.random() * 21); // 20-40 min
        int maxTiempo = minTiempo + 5 + (int)(Math.random() * 11); // +5 a +15 min
        int costoEnvio = 2000 + (int)(Math.random() * 2001); // $2000-$4000
        tvTiempo.setText(minTiempo + " - " + maxTiempo + " min");
        tvCosto.setText("$" + costoEnvio);

        // Find the button and set the click listener
        Button button = view.findViewById(R.id.btnHacerPedido);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the hacerPedido()
                hacerPedido();
            }
        });

        return view;
    }

    /**
     * Makes a POST request
     */
    private void hacerPedido() {
        EditText editTextDireccion = getView().findViewById(R.id.editTextDireccionEntrega);
        String nuevaDireccion = editTextDireccion.getText().toString().trim();
        ProfileManager profileManager = new ProfileManager(requireContext());
        String direccionGuardada = profileManager.getAddress();

        if (nuevaDireccion.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa una dirección de entrega", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nuevaDireccion.equals(direccionGuardada)) {
            // Consumir endpoint para actualizar dirección de perfil
            actualizarDireccionPerfil(nuevaDireccion);
        } else {
            // Si no cambió, continuar flujo normal
            replaceFragment(new PaymentFragment());
        }
    }

    private void actualizarDireccionPerfil(String nuevaDireccion) {
        String url = "https://backmobile1.onrender.com/appUSERS/update/";
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }
        // Mostrar progreso
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
        progressDialog.setMessage("Actualizando dirección...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        org.json.JSONObject body = new org.json.JSONObject();
        try {
            body.put("direccion", nuevaDireccion);
        } catch (org.json.JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.PUT,
                url,
                body,
                response -> {
                    progressDialog.dismiss();
                    // Guardar localmente la nueva dirección usando saveInfo con los datos actuales
                    ProfileManager profileManager = new ProfileManager(requireContext());
                    String nombre = profileManager.getName();
                    String apellido = profileManager.getSurname();
                    String email = profileManager.getEmail();
                    String telefono = profileManager.getPhone();
                    String imagen = profileManager.getProfileImageUrl();
                    profileManager.saveInfo(nombre, apellido, email, telefono, imagen, nuevaDireccion);
                    Toast.makeText(requireContext(), "Dirección actualizada", Toast.LENGTH_SHORT).show();
                    // Continuar con el flujo normal
                    replaceFragment(new PaymentFragment());
                },
                error -> {
                    progressDialog.dismiss();
                    String msg = "Error al actualizar dirección";
                    if (error.networkResponse != null) {
                        msg += ". Código: " + error.networkResponse.statusCode;
                        try {
                            String bodyStr = new String(error.networkResponse.data, "UTF-8");
                            msg += "\n" + bodyStr;
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void replaceFragment(Fragment newFragment) {
        if (isAdded()) {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
