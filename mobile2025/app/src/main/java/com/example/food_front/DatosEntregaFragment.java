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
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

        // POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // If the request is successful, show a Toast and switch fragments
                        Toast.makeText(requireContext(), "Pedido confirmado", Toast.LENGTH_SHORT).show();
                        replaceFragment(new PaymentFragment());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
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
