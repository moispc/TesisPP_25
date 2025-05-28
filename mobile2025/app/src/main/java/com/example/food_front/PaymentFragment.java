package com.example.food_front;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioButtonCreditCard, radioButtonPayPal;
    private Button buttonNext;
    private static final String PAYMENT_PREFS = "payment_preferences";
    private static final String SELECTED_PAYMENT_METHOD = "selected_payment_method";
    private static final String TAG = "PaymentFragment";

    private RequestQueue requestQueue;
    private SessionManager sessionManager;
    private String paymentId;

    public PaymentFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Inicializar RequestQueue y SessionManager
        requestQueue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Inicializar vistas
        radioButtonCreditCard = view.findViewById(R.id.radioButton4);
        radioButtonPayPal = view.findViewById(R.id.radioButton6);
        buttonNext = view.findViewById(R.id.button2);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar al SuccessFragment
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main, new SuccessFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}
