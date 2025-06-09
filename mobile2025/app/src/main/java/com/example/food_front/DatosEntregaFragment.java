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
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = "https://backmobile1.onrender.com/appCART/confirmar/";

        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        // POST request para confirmar pedido
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si el pedido se confirma, crear preferencia de pago en backmp
                        crearPreferenciaPago(token);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(requireView(), "Error al confirmar pedido. Inténtalo más tarde.",
                        Snackbar.LENGTH_LONG).show();
                Log.e("VolleyError", "Detalles del error", error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Añadir el token aquí
                return headers;
            }
        };

        // Add the request to the queue
        queue.add(stringRequest);
    }

    /**
     * Crea la preferencia de pago en el backend de MercadoPago
     */
    private void crearPreferenciaPago(String token) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = "https://backmp.onrender.com/payment/create-preference/";

        ProfileManager profileManager = new ProfileManager(requireContext());
        String email = profileManager.getEmail();
        if (email == null) email = "";

        // Construir el body JSON
        String body = String.format("{\"user_token\":\"%s\",\"email\":\"%s\"}", token, email);

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si la preferencia se crea correctamente, pasar a PaymentFragment
                        Toast.makeText(requireContext(), "Pedido confirmado", Toast.LENGTH_SHORT).show();
                        replaceFragment(new PaymentFragment());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(requireView(), "Error al crear preferencia de pago.", Snackbar.LENGTH_LONG).show();
                        Log.e("VolleyError", "Error al crear preferencia de pago", error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return body.getBytes();
            }
        };
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
