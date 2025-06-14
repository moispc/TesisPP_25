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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_front.utils.DashboardHelper;
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
        
        // Mini-ticket visual
        ViewGroup ticketLayout = new android.widget.LinearLayout(requireContext());
        ((android.widget.LinearLayout) ticketLayout).setOrientation(android.widget.LinearLayout.VERTICAL);
        ticketLayout.setPadding(32, 32, 32, 32);
        ticketLayout.setBackgroundResource(android.R.color.white);
        ticketLayout.setElevation(8f);
        ticketLayout.setClickable(true);
        ticketLayout.setFocusable(true);
        ticketLayout.setForeground(requireContext().getDrawable(android.R.drawable.list_selector_background));
        android.widget.TextView tvTitulo = new android.widget.TextView(requireContext());
        tvTitulo.setText("Ticket de compra");
        tvTitulo.setTextSize(18);
        tvTitulo.setTextColor(android.graphics.Color.BLACK);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        ticketLayout.addView(tvTitulo);
        // Fecha
        String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
        android.widget.TextView tvFecha = new android.widget.TextView(requireContext());
        tvFecha.setText("Fecha: " + fecha);
        tvFecha.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvFecha);
        // Número de pedido simulado
        int nroPedido = (int) (Math.random() * 90000 + 10000);
        android.widget.TextView tvNro = new android.widget.TextView(requireContext());
        tvNro.setText("N° Pedido: " + nroPedido);
        tvNro.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvNro);
        // Método de pago
        android.widget.TextView tvPago = new android.widget.TextView(requireContext());
        String metodo = paymentMethod != null ? paymentMethod : "-";
        tvPago.setText("Pago: " + metodo);
        tvPago.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvPago);
        // Total (simulado, ya que no hay acceso directo al monto)
        android.widget.TextView tvTotal = new android.widget.TextView(requireContext());
        String totalSimulado = String.valueOf((int)(Math.random()*2000+2000));
        tvTotal.setText("Total: $ " + totalSimulado);
        tvTotal.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvTotal);
        // Separador visual
        android.view.View sep = new android.view.View(requireContext());
        sep.setBackgroundColor(android.graphics.Color.LTGRAY);
        sep.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2));
        ticketLayout.addView(sep);
        // Mensaje de éxito
        android.widget.TextView tvGracias = new android.widget.TextView(requireContext());
        tvGracias.setText("¡Gracias por tu compra!");
        tvGracias.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        tvGracias.setTextSize(16);
        tvGracias.setPadding(0, 16, 0, 0);
        ticketLayout.addView(tvGracias);
        // Agregar el ticket al contenedor del layout XML
        FrameLayout ticketContainer = view.findViewById(R.id.ticket_container);
        ticketContainer.removeAllViews();
        ticketContainer.addView(ticketLayout);
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
        // Obtener productos reales del último pedido
        String token = sessionManager.getToken();
        DashboardHelper.getUltimoPedido(requireContext(), token, new DashboardHelper.DashboardCallback() {
            @Override
            public void onSuccess(org.json.JSONArray pedidos) {
                if (pedidos.length() > 0) {
                    try {
                        org.json.JSONObject ultimoPedido = pedidos.getJSONObject(0); // El más reciente
                        StringBuilder productosBuilder = new StringBuilder();
                        if (ultimoPedido.has("detalles")) {
                            org.json.JSONArray detalles = ultimoPedido.getJSONArray("detalles");
                            for (int i = 0; i < detalles.length(); i++) {
                                org.json.JSONObject detalle = detalles.getJSONObject(i);
                                String nombre = detalle.optString("nombre_producto", "Producto");
                                int cantidad = detalle.optInt("cantidad_productos", 1);
                                productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");
                            }
                        }
                        String productos = productosBuilder.toString().trim();
                        // Hacer el ticket ampliable al click con productos reales
                        ticketLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                                        fecha,
                                        String.valueOf(nroPedido),
                                        metodo,
                                        totalSimulado,
                                        productos
                                );
                                dialog.show(getParentFragmentManager(), "TicketDetailDialog");
                            }
                        });
                    } catch (Exception e) {
                        // Fallback a productos hardcodeados si hay error
                        setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, totalSimulado);
                    }
                } else {
                    // Fallback a productos hardcodeados si no hay pedidos
                    setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, totalSimulado);
                }
            }
            @Override
            public void onError(String error) {
                // Fallback a productos hardcodeados si hay error
                setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, totalSimulado);
            }
        });
        return view;
    }

    private void setTicketClickDefault(View ticketLayout, String fecha, int nroPedido, String metodo, String totalSimulado) {
        String productos = "- Hamburguesa x2\n- Papas Fritas x1\n- Bebida x1";
        ticketLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                        fecha,
                        String.valueOf(nroPedido),
                        metodo,
                        totalSimulado,
                        productos
                );
                dialog.show(getParentFragmentManager(), "TicketDetailDialog");
            }
        });
    }
}
