package com.example.food_front;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;
import com.example.food_front.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView tvNombre, tvEmail;
    private CircleImageView profileImage;
    private ProfileManager profileManager;
    private SessionManager sessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar las vistas
        tvNombre = view.findViewById(R.id.user_name);
        tvEmail = view.findViewById(R.id.user_email);
        profileImage = view.findViewById(R.id.profile_image);

        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Llamar al backend para obtener los datos del perfil
        displayUserProfile();

        // Hacer la imagen de perfil clickable
        profileImage.setOnClickListener(v -> selectImage());

        // Encontrar el TextView de "Datos personales"
        TextView personalData = view.findViewById(R.id.personal_data);

        // Añadir el listener de clic para "Datos personales"
        personalData.setOnClickListener(v -> {
            // Navegar al fragmento de datos personales
            PersonalDataFragment personalDataFragment = new PersonalDataFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view, personalDataFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Encontrar el TextView de "Cerrar sesion"
        TextView closeSession = view.findViewById(R.id.logout);

        // Añadir el listener de clic para "Cerrar sesion"
        closeSession.setOnClickListener(v -> {
            // Mostrar un diálogo de confirmación antes de cerrar la sesión
            new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar tu sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar la sesión
                    sessionManager.logout();

                    // Mostrar mensaje
                    Toast.makeText(requireContext(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();

                    // Redirigir a la pantalla principal donde se mostrará el LoginFragment
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
        });

        // Encontrar el TextView de "Mis pedidos"
        TextView viewOrders = view.findViewById(R.id.view_orders);
        viewOrders.setOnClickListener(v -> {
            String url = "https://backmobile1.onrender.com/appCART/ver_dashboard/";
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("DASHBOARD", "Respuesta cruda: " + response.toString());
                        if (response.has("results")) {
                            org.json.JSONArray pedidos = response.getJSONArray("results");
                            Log.d("DASHBOARD", "Pedidos recibidos: " + pedidos.length());
                            if (pedidos.length() == 0) {
                                mostrarMensajeNoPedidos();
                            } else {
                                mostrarDialogoConPedidos(pedidos);
                            }
                        } else {
                            Log.e("DASHBOARD", "No hay campo 'results' en la respuesta: " + response.toString());
                            mostrarMensajeSinResultados(response);
                        }
                    } catch (Exception e) {
                        Log.e("DASHBOARD", "Error parseando respuesta: " + Log.getStackTraceString(e));
                        mostrarMensajeError(e, response);
                    }
                },
                error -> {
                    String msg = "Error al obtener pedidos";
                    if (error.networkResponse != null) {
                        msg += ". Código: " + error.networkResponse.statusCode;
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            Log.e("DASHBOARD", "Respuesta error: " + body);
                            msg += "\n" + body;
                        } catch (Exception ex) {
                            Log.e("DASHBOARD", "Error leyendo body de error", ex);
                        }
                    } else {
                        Log.e("DASHBOARD", "Volley error sin networkResponse", error);
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = sessionManager.getToken();
                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    return headers;
                }
            };
            queue.add(request);
            // Aumentar el timeout solo para esta petición
            request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                60000, // 60 segundos de timeout
                1,     // 1 reintento
                1.5f   // backoff multiplier
            ));
        });

        // Encontrar el TextView de "Información legal"
        TextView legalInfo = view.findViewById(R.id.legal_info);
        legalInfo.setOnClickListener(v -> {
            // Navegar al fragmento del EULA
            EulaFragment eulaFragment = new EulaFragment();
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, eulaFragment)
                .addToBackStack(null)
                .commit();
        });

        // Botón Eliminar cuenta
        Button btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
        });

        return view;
    }

    /**
     * Muestra un mensaje cuando no hay pedidos registrados
     */
    private void mostrarMensajeNoPedidos() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Mis pedidos")
            .setMessage("No tienes pedidos registrados.")
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Muestra mensaje de error cuando la respuesta no tiene resultados
     */
    private void mostrarMensajeSinResultados(JSONObject response) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Sin resultados")
            .setMessage("No se encontraron pedidos.\n\nRespuesta del servidor: " + response.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Muestra un mensaje de error cuando hay un problema al procesar la respuesta
     */
    private void mostrarMensajeError(Exception e, JSONObject response) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage("Ocurrió un problema al procesar los pedidos: " + e.getMessage() +
                       "\n\nRespuesta: " + response.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Muestra un diálogo con la lista de pedidos y opciones para editar la dirección de entrega
     */
    private void mostrarDialogoConPedidos(org.json.JSONArray pedidos) throws JSONException {
        // Crear un layout vertical para contener la lista de pedidos
        LinearLayout contenedorPrincipal = new LinearLayout(requireContext());
        contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);
        contenedorPrincipal.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contenedorPrincipal.setPadding(32, 32, 32, 32);

        // ScrollView para hacer desplazable la lista
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (350 * getResources().getDisplayMetrics().density)));

        // Crear un LinearLayout para los pedidos dentro del ScrollView
        LinearLayout contenedorPedidos = new LinearLayout(requireContext());
        contenedorPedidos.setOrientation(LinearLayout.VERTICAL);
        contenedorPedidos.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Procesar cada pedido y mostrar en el contenedor
        for (int i = 0; i < pedidos.length(); i++) {
            org.json.JSONObject pedido = pedidos.getJSONObject(i);

            // Usar id_pedidos si está disponible, o el índice + 1 como respaldo
            int numeroPedido = pedido.has("id_pedidos") ?
                    pedido.getInt("id_pedidos") : (i + 1);

            // Crear un CardView para cada pedido
            CardView cardPedido = new CardView(requireContext());
            cardPedido.setLayoutParams(new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT));
            cardPedido.setRadius(16);
            cardPedido.setCardElevation(8);
            cardPedido.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            cardPedido.setUseCompatPadding(true);

            // Layout vertical para el contenido de la tarjeta
            LinearLayout contenidoCard = new LinearLayout(requireContext());
            contenidoCard.setOrientation(LinearLayout.VERTICAL);
            contenidoCard.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            contenidoCard.setPadding(16, 16, 16, 16);

            // Título del pedido
            TextView tvTituloPedido = new TextView(requireContext());
            tvTituloPedido.setText("\uD83D\uDCC5 Pedido #" + numeroPedido);
            tvTituloPedido.setTextSize(18);
            tvTituloPedido.setTypeface(Typeface.DEFAULT_BOLD);
            tvTituloPedido.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvTituloPedido.setPadding(0, 0, 0, 8);
            contenidoCard.addView(tvTituloPedido);

            // Fecha del pedido
            if (pedido.has("fecha_pedido")) {
                TextView tvFecha = new TextView(requireContext());
                tvFecha.setText("Fecha: " + pedido.getString("fecha_pedido"));
                tvFecha.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tvFecha.setPadding(16, 4, 0, 4);
                contenidoCard.addView(tvFecha);
            }

            // Estado del pedido
            if (pedido.has("estado")) {
                TextView tvEstado = new TextView(requireContext());
                tvEstado.setText("Estado: " + pedido.getString("estado"));
                tvEstado.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tvEstado.setPadding(16, 4, 0, 4);
                contenidoCard.addView(tvEstado);
            }





            // Layout horizontal para dirección y botón
            LinearLayout layoutDireccion = new LinearLayout(requireContext());
            layoutDireccion.setOrientation(LinearLayout.HORIZONTAL);
            layoutDireccion.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));





            // ID de pedido para la actualización
            final int pedidoId = numeroPedido;




            // Detalles del pedido (productos)
            if (pedido.has("detalles")) {
                org.json.JSONArray detalles = pedido.getJSONArray("detalles");
                if (detalles.length() > 0) {
                    TextView tvProductosHeader = new TextView(requireContext());
                    tvProductosHeader.setText("Productos:");
                    tvProductosHeader.setTypeface(Typeface.DEFAULT_BOLD);
                    tvProductosHeader.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tvProductosHeader.setPadding(16, 8, 0, 4);
                    contenidoCard.addView(tvProductosHeader);

                    for (int j = 0; j < detalles.length(); j++) {
                        org.json.JSONObject detalle = detalles.getJSONObject(j);
                        TextView tvProducto = new TextView(requireContext());
                        StringBuilder productoInfo = new StringBuilder();
                        productoInfo.append("- Cantidad: ").append(detalle.optInt("cantidad_productos", 0));
                        productoInfo.append(", Precio: $").append(detalle.optDouble("precio_producto", 0));
                        productoInfo.append(", Subtotal: $").append(detalle.optDouble("subtotal", 0));

                        tvProducto.setText(productoInfo.toString());
                        tvProducto.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tvProducto.setPadding(32, 2, 0, 2);
                        contenidoCard.addView(tvProducto);
                    }
                }
            }

            // Agregar el contenido a la tarjeta
            cardPedido.addView(contenidoCard);

            // Agregar la tarjeta al contenedor principal
            contenedorPedidos.addView(cardPedido);
        }

        // Agregar el contenedor de pedidos al ScrollView
        scrollView.addView(contenedorPedidos);

        // Agregar el ScrollView al contenedor principal
        contenedorPrincipal.addView(scrollView);

        // Mostrar el diálogo con todos los pedidos
        new AlertDialog.Builder(requireContext())
            .setTitle("Mis pedidos")
            .setView(contenedorPrincipal)
            .setPositiveButton("Cerrar", null)
            .show();
    }

    /**
     * Muestra un diálogo para editar la dirección de entrega de un pedido
     * @param pedidoId ID del pedido a actualizar
     * @param direccionActual La dirección actual del pedido
     * @param tvDireccion El TextView que muestra la dirección para actualizarlo
     */
    private void mostrarDialogoEditarDireccion(int pedidoId, String direccionActual, TextView tvDireccion) {
        // Crear un EditText para la nueva dirección
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(direccionActual);
        input.setSingleLine(false);
        input.setLines(3);
        input.setMaxLines(5);
        input.setGravity(Gravity.TOP | Gravity.START);

        // Crear y mostrar el diálogo
        new AlertDialog.Builder(requireContext())
            .setTitle("Editar dirección de entrega")
            .setView(input)
            .setPositiveButton("Guardar", (dialog, which) -> {
                String nuevaDireccion = input.getText().toString().trim();
                if (!nuevaDireccion.isEmpty()) {
                    // Actualizar la dirección en el backend
                    actualizarDireccionEnBackend(pedidoId, nuevaDireccion, tvDireccion);
                } else {
                    Toast.makeText(requireContext(), "La dirección no puede estar vacía", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    /**
     * Actualiza la dirección de entrega en el backend
     * @param pedidoId ID del pedido a actualizar
     * @param nuevaDireccion Nueva dirección de entrega
     * @param tvDireccion TextView para actualizar si la operación es exitosa
     */
    private void actualizarDireccionEnBackend(int pedidoId, String nuevaDireccion, TextView tvDireccion) {
        // URL para actualizar la dirección de entrega
        String url = "https://backmobile1.onrender.com/appCART/actualizar_direccion/";

        // Mostrar progreso
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Actualizando dirección...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Crear el cuerpo de la solicitud
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id_pedidos", pedidoId);
            requestBody.put("direccion_entrega", nuevaDireccion);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear la solicitud
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.PUT,
            url,
            requestBody,
            response -> {
                progressDialog.dismiss();
                try {
                    boolean success = response.optBoolean("success", true);
                    if (success) {
                        // Actualizar la UI con la nueva dirección
                        tvDireccion.setText("Dirección: " + nuevaDireccion);
                        Toast.makeText(requireContext(), "Dirección actualizada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = response.optString("message", "Error desconocido");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                progressDialog.dismiss();

                String mensaje = "Error al actualizar la dirección";
                if (error.networkResponse != null) {
                    mensaje += " (Código: " + error.networkResponse.statusCode + ")";
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorObj = new JSONObject(responseBody);
                        if (errorObj.has("message")) {
                            mensaje += ": " + errorObj.getString("message");
                        }
                    } catch (Exception e) {
                        // Ignorar errores al leer el mensaje
                    }
                }

                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Agregar la solicitud a la cola
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(request);
    }

    private void displayUserProfile() {
        // Usar los métodos específicos para obtener los datos
        String name = profileManager.getName();
        String surname = profileManager.getSurname();
        String email = profileManager.getEmail();
        String imageUrl = profileManager.getProfileImageUrl();

        // Mostrar los datos en los TextViews
        tvNombre.setText(name + " " + surname);  // Mostrar nombre completo
        tvEmail.setText(email);
        
        // Cargar la imagen de perfil directamente mediante HttpURLConnection
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d("ImagenPerfil", "Cargando imagen en ProfileFragment: " + imageUrl);
            
            // Limpiar caché de Glide
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
            
            // Usar un hilo secundario para la descarga directa
            new Thread(() -> {
                try {
                    // URL con timestamp para evitar caché de red
                    String imageUrlNoCache = imageUrl + "?nocache=" + System.currentTimeMillis() + "&random=" + Math.random();
                    
                    // Configurar conexión HTTP sin caché
                    java.net.URL url = new java.net.URL(imageUrlNoCache);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setUseCaches(false);
                    connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                    connection.addRequestProperty("Pragma", "no-cache");
                    connection.addRequestProperty("Expires", "0");
                    connection.connect();
                    
                    // Obtener la imagen como Bitmap
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(connection.getInputStream());
                    
                    // Actualizar UI en hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                profileImage.setImageBitmap(bitmap);
                                Log.d("ImagenPerfil", "Imagen cargada exitosamente mediante HttpURLConnection");
                            } else {
                                // Si falla, intentar con Glide como respaldo
                                cargarImagenConGlide(imageUrl);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("ImagenPerfil", "Error al descargar imagen: " + e.getMessage());
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> cargarImagenConGlide(imageUrl));
                    }
                }
            }).start();
        } else {
            Log.d("ImagenPerfil", "No hay URL de imagen, usando imagen predeterminada en ProfileFragment");
            // Usar imagen predeterminada
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }
    
    private void cargarImagenConGlide(String imageUrl) {
        // Agregar timestamp para forzar recarga
        String imageUrlWithTimestamp = imageUrl + "?t=" + System.currentTimeMillis() + "&r=" + Math.random();
        Log.d("ImagenPerfil", "Intentando cargar con Glide como respaldo: " + imageUrlWithTimestamp);
        
        // Intentar cargar con Glide sin caché
        Glide.with(requireContext())
            .load(imageUrlWithTimestamp)
            .skipMemoryCache(true) // Para evitar problemas con la caché
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .into(profileImage);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Comprimimos la imagen antes de subirla
                android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

                // Redimensionamos la imagen para reducir su tamaño
                android.graphics.Bitmap resizedBitmap = resizeImage(bitmap, 800); // máximo 800px en su dimensión más grande

                // Convertimos el bitmap a bytes con compresión
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // calidad 70%
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Mostramos el tamaño de la imagen comprimida para diagnóstico
                Log.d("ImagenPerfil", "Tamaño de la imagen comprimida: " + (imageBytes.length / 1024) + " KB");

                // Intentamos subir la imagen al servidor
                uploadImage(imageBytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Archivo no encontrado", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error al leer el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Redimensiona una imagen manteniendo su relación de aspecto
     * @param image La imagen original
     * @param maxSize El tamaño máximo para el lado más largo
     * @return La imagen redimensionada
     */
    private android.graphics.Bitmap resizeImage(android.graphics.Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float ratio = (float) width / (float) height;

        int newWidth;
        int newHeight;

        if (width > height) {
            newWidth = maxSize;
            newHeight = Math.round(maxSize / ratio);
        } else {
            newHeight = maxSize;
            newWidth = Math.round(maxSize * ratio);
        }

        return android.graphics.Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    private void uploadImage(byte[] imageBytes) {
        // Mostrar un diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Preparando imagen...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Si la imagen es mayor a 1MB, mostrar una advertencia
        if (imageBytes.length > 1024 * 1024) {
            progressDialog.dismiss();
            new AlertDialog.Builder(requireContext())
                .setTitle("Imagen demasiado grande")
                .setMessage("La imagen seleccionada es de " + (imageBytes.length / 1024 / 1024) + "MB. Es posible que la subida sea lenta. ¿Deseas intentar subir la imagen de todos modos?")
                .setPositiveButton("Sí, intentar", (dialog, which) -> {
                    // Reiniciar el proceso con la imagen original
                    uploadToCloudinary(imageBytes, progressDialog);
                })
                .setNegativeButton("No, cancelar", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Subida cancelada", Toast.LENGTH_SHORT).show();
                })
                .show();
        } else {
            // Si la imagen es pequeña, continuar directamente
            uploadToCloudinary(imageBytes, progressDialog);
        }
    }

    /**
     * Sube una imagen a Cloudinary
     * @param imageBytes Bytes de la imagen a subir
     * @param progressDialog Diálogo de progreso existente o null para crear uno nuevo
     */
    private void uploadToCloudinary(byte[] imageBytes, ProgressDialog progressDialog) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Subiendo imagen...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.setMessage("Subiendo imagen a Cloudinary...");
        }

        final ProgressDialog finalProgressDialog = progressDialog;

        // Subir imagen usando el CloudinaryManager
        com.example.food_front.utils.CloudinaryManager.uploadImage(
            requireContext(),
            imageBytes,
            "profile_images",
            new com.example.food_front.utils.CloudinaryManager.CloudinaryUploadCallback() {
                @Override
                public void onStart() {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            finalProgressDialog.setMessage("Iniciando subida...");
                        });
                    }
                }

                @Override
                public void onProgress(double progress) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            int progressInt = (int) (progress * 100);
                            finalProgressDialog.setMessage("Subiendo imagen: " + progressInt + "%");
                        });
                    }
                }

                @Override
                public void onSuccess(String imageUrl) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            finalProgressDialog.dismiss();

                            // Guardar URL de imagen en SharedPreferences
                            profileManager.saveProfileImageUrl(imageUrl);

                            // Actualizar la imagen local
                            cargarImagenConGlide(imageUrl);

                            // Actualizar imagen en otros fragmentos
                            MainActivity mainActivity = (MainActivity) getActivity();
                            if (mainActivity != null) {
                                mainActivity.actualizarTodasLasImagenes();
                            }

                            Toast.makeText(requireContext(), "Imagen subida correctamente", Toast.LENGTH_SHORT).show();

                            // También actualizar en el backend si es necesario
                            actualizarImagenEnBackend(imageUrl);
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            finalProgressDialog.dismiss();
                            Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();

                            // Si hay un error con Cloudinary, mostrar un diálogo para usar el método anterior
                            new AlertDialog.Builder(requireContext())
                                .setTitle("Error con Cloudinary")
                                .setMessage("¿Deseas intentar subir la imagen usando el método alternativo?")
                                .setPositiveButton("Sí, intentar", (dialog, which) -> {
                                    // Usar el método original (backend propio)
                                    continueUpload(imageBytes, "https://backmobile1.onrender.com/appUSERS/upload_profile_image/");
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                        });
                    }
                }
            }
        );
    }

    /**
     * Actualiza la URL de la imagen del perfil en el backend
     * @param imageUrl URL de Cloudinary
     */
    private void actualizarImagenEnBackend(String imageUrl) {
        String url = "https://backmobile1.onrender.com/appUSERS/update_profile_image_url/";

        try {
            // Crear solicitud JSON
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("imagen_perfil_url", imageUrl);

            // Crear solicitud Volley
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d("ImagenPerfil", "URL actualizada en backend: " + response.toString());
                },
                error -> {
                    Log.e("ImagenPerfil", "Error al actualizar URL en backend", error);
                }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = sessionManager.getToken();
                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    return headers;
                }
            };

            // Añadir a la cola de Volley
            RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
            requestQueue.add(request);

        } catch (Exception e) {
            Log.e("ImagenPerfil", "Error al crear solicitud para actualizar URL", e);
        }
    }

    /**
     * Método público para actualizar la imagen de perfil desde MainActivity
     * @param imageUrl URL de la nueva imagen
     */
    public void actualizarImagenDePerfil(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        Log.d("ImagenPerfil", "actualizarImagenDePerfil llamado con URL: " + imageUrl);

        // Limpiar caché
        com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());

        // Guardar la nueva URL
        profileManager.saveProfileImageUrl(imageUrl);

        // Actualizar la interfaz de usuario
        if (profileImage != null) {
            cargarImagenConGlide(imageUrl);
        }
    }

    /**
     * Continúa la subida de la imagen usando el método convencional (backend propio)
     * @param imageBytes Bytes de la imagen
     * @param url URL del endpoint para subir la imagen
     */
    private void continueUpload(byte[] imageBytes, String url) {
        // Mostrar un diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Subiendo imagen...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Crear una solicitud multipart para subir la imagen
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();

                    // Procesar la respuesta del servidor
                    String responseBody = new String(response.data);
                    Log.d("ImagenPerfil", "Respuesta del servidor: " + responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Verificar si hay un mensaje de error en la respuesta
                        if (jsonObject.has("error")) {
                            String error = jsonObject.getString("error");
                            Log.e("ImagenPerfil", "Error del servidor: " + error);
                            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Obtener URL de la imagen (normalizando la obtención de la URL)
                        String imageUrl = null;
                        if (jsonObject.has("imagen_perfil_url")) {
                            imageUrl = jsonObject.getString("imagen_perfil_url");
                        } else if (jsonObject.has("image_url")) {
                            imageUrl = jsonObject.getString("image_url");
                        } else if (jsonObject.has("url")) {
                            imageUrl = jsonObject.getString("url");
                        }

                        if (imageUrl == null || imageUrl.isEmpty()) {
                            Log.e("ImagenPerfil", "URL de imagen no encontrada en la respuesta: " + responseBody);
                            Toast.makeText(requireContext(), "Error: No se recibió la URL de la imagen", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Guardar la URL final para usar en el código
                        final String finalImageUrl = imageUrl;
                        Log.d("ImagenPerfil", "URL de imagen recibida: " + finalImageUrl);

                        // Limpiar caché de Glide
                        com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());

                        // Guardar la nueva URL en SharedPreferences
                        profileManager.saveProfileImageUrl(finalImageUrl);

                        // Actualizar la interfaz con la nueva imagen
                        cargarImagenConGlide(finalImageUrl);

                        // Actualizar en otros fragmentos
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.actualizarTodasLasImagenes();
                        }

                        Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.e("ImagenPerfil", "Error al procesar la respuesta JSON: " + e.getMessage() + "\nRespuesta: " + responseBody);
                        Toast.makeText(requireContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    handleNetworkError(error);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir el token de autenticación
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        // Añadir la imagen como archivo al cuerpo de la solicitud
        multipartRequest.addByteData("image", imageBytes, "profile_image.jpg", "image/jpeg");

        // Establecer una política de reintento
        multipartRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                90000, // 90 segundos de timeout
                2,     // 2 reintentos
                1.5f   // backoff multiplier
        ));

        // Añadir la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(multipartRequest);
    }

    /**
     * Maneja errores de red comunes
     * @param error Error de Volley
     */
    private void handleNetworkError(VolleyError error) {
        String errorMsg = "Error al subir la imagen: ";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMsg += "Código: " + statusCode;

            // Intentar obtener el cuerpo de la respuesta de error
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                Log.e("ImagenPerfil", "Error response body: " + responseBody);

                // Para errores 502, ofrecer alternativa
                if (statusCode == 502) {
                    showCloudflareErrorDialog();
                    return;
                }

                errorMsg += " - " + responseBody;
            } catch (Exception e) {
                Log.e("ImagenPerfil", "Error al leer el cuerpo de la respuesta de error", e);
            }
        } else if (error instanceof com.android.volley.TimeoutError) {
            errorMsg = "El servidor tardó demasiado en responder. La imagen podría ser demasiado grande.";
            showCloudflareErrorDialog();
            return;
        } else if (error instanceof com.android.volley.NoConnectionError) {
            errorMsg = "No hay conexión a internet. Por favor, verifica tu conexión e intenta nuevamente.";
        } else {
            errorMsg += error.toString();
        }

        Log.e("ImagenPerfil", errorMsg, error);
        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    /**
     * Muestra un diálogo específico para errores de Cloudflare (502 Bad Gateway)
     */
    private void showCloudflareErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
            .setTitle("Problema con el servidor")
            .setMessage("El servidor está experimentando problemas temporales (Error 502 Bad Gateway). Esto puede deberse a que:\n\n" +
                      "1. El servidor está ocupado o en mantenimiento\n" +
                      "2. La imagen es demasiado grande para ser procesada\n\n" +
                      "¿Qué deseas hacer?")
            .setPositiveButton("Intentar con otra imagen", (dialog, which) -> {
                // Volver a abrir el selector de imágenes
                selectImage();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    // Método para eliminar la cuenta
    private void eliminarCuenta() {
        String url = "https://backmobile1.onrender.com/appUSERS/delete/";
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Eliminando cuenta...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.DELETE,
            url,
            response -> {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Cuenta eliminada satisfactoriamente", Toast.LENGTH_LONG).show();
                sessionManager.logout();
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            },
            error -> {
                progressDialog.dismiss();
                
                // Mejor manejo de errores
                String errorMsg = "Error al eliminar la cuenta: ";
                
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    errorMsg += "Código: " + statusCode;
                    
                    // Intentar obtener el cuerpo de la respuesta de error
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        Log.e("EliminarCuenta", "Error response body: " + responseBody);
                        errorMsg += " - " + responseBody;
                        
                        // Si es error 500 pero la cuenta fue desactivada correctamente en el backend
                        if (statusCode == 500 && responseBody.contains("Cuenta desactivada correctamente")) {
                            // A pesar del error 500, el backend procesó correctamente la solicitud
                            Toast.makeText(requireContext(), "Cuenta eliminada satisfactoriamente", Toast.LENGTH_LONG).show();
                            sessionManager.logout();
                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("EliminarCuenta", "Error al leer el cuerpo de la respuesta de error", e);
                    }
                } else if (error instanceof com.android.volley.TimeoutError) {
                    errorMsg = "El servidor tardó demasiado en responder.";
                } else if (error instanceof com.android.volley.NoConnectionError) {
                    errorMsg = "No hay conexión a internet. Por favor, verifica tu conexión e intenta nuevamente.";
                } else {
                    errorMsg += error.toString();
                }
                
                Log.e("EliminarCuenta", errorMsg, error);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        
        // Establecer una política de reintento con timeout más largo
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            30000, // 30 segundos de timeout
            1,     // 1 reintento
            1.5f   // backoff multiplier
        ));
        
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request);
    }
}
