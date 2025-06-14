package com.example.food_front;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TicketDetailDialogFragment extends DialogFragment {
    private String fecha;
    private String nroPedido;
    private String metodoPago;
    private String total;

    public static TicketDetailDialogFragment newInstance(String fecha, String nroPedido, String metodoPago, String total, String productos) {
        TicketDetailDialogFragment fragment = new TicketDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString("fecha", fecha);
        args.putString("nroPedido", nroPedido);
        args.putString("metodoPago", metodoPago);
        args.putString("total", total);
        args.putString("productos", productos);
        fragment.setArguments(args);
        return fragment;
    }

    private String productos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            fecha = getArguments().getString("fecha", "-");
            nroPedido = getArguments().getString("nroPedido", "-");
            metodoPago = getArguments().getString("metodoPago", "-");
            total = getArguments().getString("total", "-");
            productos = getArguments().getString("productos", "-");
        }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64); // Más grande
        layout.setBackgroundColor(android.graphics.Color.WHITE);
        layout.setMinimumWidth(900); // Más ancho
        layout.setMinimumHeight(900); // Más alto

        TextView title = new TextView(requireContext());
        title.setText("Detalles del Ticket");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(android.graphics.Color.BLACK);
        layout.addView(title);

        TextView tvFecha = new TextView(requireContext());
        tvFecha.setText("Fecha: " + fecha);
        layout.addView(tvFecha);

        TextView tvNro = new TextView(requireContext());
        tvNro.setText("N° Pedido: " + nroPedido);
        layout.addView(tvNro);

        TextView tvPago = new TextView(requireContext());
        tvPago.setText("Método de pago: " + metodoPago);
        layout.addView(tvPago);

        TextView tvTotal = new TextView(requireContext());
        tvTotal.setText("Total: $ " + total);
        layout.addView(tvTotal);

        // Productos reales
        TextView tvProductos = new TextView(requireContext());
        tvProductos.setText("Productos:\n" + productos);
        layout.addView(tvProductos);

        TextView tvGracias = new TextView(requireContext());
        tvGracias.setText("¡Gracias por tu compra!");
        tvGracias.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        tvGracias.setTextSize(18);
        tvGracias.setPadding(0, 24, 0, 0);
        layout.addView(tvGracias);

        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Detalles del Ticket");
        return dialog;
    }
}
