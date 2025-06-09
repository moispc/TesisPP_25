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
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_front.utils.SessionManager;


public class SuccessFragment extends Fragment {

    private static final String TAG = "SuccessFragment";
    private TextView textMessage;
    private String paymentRequestId;
    private String paymentMethod;

    public SuccessFragment() {
        // Required empty public constructor
    }
    
    /**
     * Crea una nueva instancia del fragmento con datos de pago opcional
     * @param paymentRequestId ID de la solicitud de pago (opcional)
     * @param paymentMethod Método de pago utilizado (opcional)
     * @return Una nueva instancia del fragmento
     */
    public static SuccessFragment newInstance(String paymentRequestId, String paymentMethod) {
        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString("payment_request_id", paymentRequestId);
        args.putString("payment_method", paymentMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paymentRequestId = getArguments().getString("payment_request_id");
            paymentMethod = getArguments().getString("payment_method");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_success, container, false);
        
        // Inicializar vistas
        textMessage = view.findViewById(R.id.textView);
        Button button = view.findViewById(R.id.button);
        
        // Personalizar mensaje según el método de pago
        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("mercadopago")) {
            textMessage.setText("¡Compra Finalizada con Éxito!\n\nTu pago con MercadoPago ha sido procesado correctamente.");
        } else {
            textMessage.setText("¡Compra Finalizada con Éxito!");
        }
        
        // Limpiar carrito en memoria (opcional)
        SessionManager sessionManager = new SessionManager(requireContext());
        
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Llamar a MainActivity para limpiar la pila y mostrar HomeFragment correctamente
                    if (getActivity() instanceof com.example.food_front.MainActivity) {
                        ((com.example.food_front.MainActivity) getActivity()).mostrarHomeLimpiandoBackStack();
                    } else {
                        // Fallback por si acaso
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container_view, new HomeFragment());
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commit();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al navegar al HomeFragment: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error al volver a inicio. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
