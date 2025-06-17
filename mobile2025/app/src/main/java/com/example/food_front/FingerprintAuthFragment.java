package com.example.food_front;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executor;

public class FingerprintAuthFragment extends Fragment {

    private TextView tvTotal;
    private TextView tvStatus;
    private Button btnCancel;
    private ImageView backButton;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private double totalCompra = 0;

    public FingerprintAuthFragment() {
        // Constructor vacío requerido
    }

    // Método estático para crear una nueva instancia con el total de la compra
    public static FingerprintAuthFragment newInstance(double total) {
        FingerprintAuthFragment fragment = new FingerprintAuthFragment();
        Bundle args = new Bundle();
        args.putDouble("totalCompra", total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalCompra = getArguments().getDouble("totalCompra", 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_fingerprint_auth, container, false);

        // Inicializar vistas
        tvTotal = view.findViewById(R.id.tvTotal);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnCancel = view.findViewById(R.id.btnCancel);
        backButton = view.findViewById(R.id.backButton);

        // Formatear y mostrar el total de la compra
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        tvTotal.setText("Monto de compra: " + formatoMoneda.format(totalCompra));

        // Configurar botones
        btnCancel.setOnClickListener(v -> {
            // Volver al fragmento anterior
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        backButton.setOnClickListener(v -> {
            // Volver al fragmento anterior
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Configurar autenticación biométrica
        setupBiometricAuthentication();

        return view;
    }

    private void setupBiometricAuthentication() {
        // Verificar si el dispositivo soporta autenticación biométrica
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // El dispositivo puede usar biometría
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                tvStatus.setText("Este dispositivo no tiene sensor de huella digital");
                new Handler().postDelayed(() -> navigateToPayment(), 2000);
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                tvStatus.setText("Sensor biométrico no disponible");
                new Handler().postDelayed(() -> navigateToPayment(), 2000);
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                tvStatus.setText("No hay huellas registradas");
                new Handler().postDelayed(() -> navigateToPayment(), 2000);
                return;
        }

        // Configurar executor para la autenticación biométrica
        executor = ContextCompat.getMainExecutor(requireContext());

        // Configurar BiometricPrompt
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        tvStatus.setText("Error de autenticación: " + errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        tvStatus.setText("¡Autenticación exitosa!");
                        // Esperar un momento y navegar a la pantalla de pago
                        new Handler().postDelayed(() -> navigateToPayment(), 1000);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        tvStatus.setText("La autenticación falló, inténtalo de nuevo");
                    }
                });

        // Configurar el diálogo de autenticación
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verificación de identidad")
                .setSubtitle("Autentícate con tu huella digital")
                .setDescription("Se requiere verificación para compras superiores a $50.000")
                .setNegativeButtonText("Cancelar")
                .build();

        // Mostrar diálogo de autenticación biométrica cuando el fragmento se haya cargado
        new Handler().postDelayed(() -> biometricPrompt.authenticate(promptInfo), 500);
    }

    private void navigateToPayment() {
        // Navegar al fragmento de pago
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new PaymentFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
