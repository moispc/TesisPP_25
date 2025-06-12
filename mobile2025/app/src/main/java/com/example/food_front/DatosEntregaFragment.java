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
        // Ir directamente a la pantalla de métodos de pago
        replaceFragment(new PaymentFragment());
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
