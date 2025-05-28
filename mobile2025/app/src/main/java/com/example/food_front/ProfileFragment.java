package com.example.food_front;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Encontrar el TextView de "Mis pedidos"
        TextView viewOrders = view.findViewById(R.id.view_orders);
        viewOrders.setOnClickListener(v -> {
            String url = "https://backmobile1.onrender.com/appCART/ver_dashboard/";
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    StringBuilder pedidosStr = new StringBuilder();
                    try {
                        if (response.has("results")) {
                            org.json.JSONArray pedidos = response.getJSONArray("results");
                            if (pedidos.length() == 0) {
                                pedidosStr.append("No tienes pedidos.");
                            } else {
                                for (int i = 0; i < pedidos.length(); i++) {
                                    org.json.JSONObject pedido = pedidos.getJSONObject(i);
                                    pedidosStr.append("\uD83D\uDCC5 Pedido #").append(i + 1).append("\n");
                                    if (pedido.has("fecha_pedido")) pedidosStr.append("  Fecha: ").append(pedido.getString("fecha_pedido")).append("\n");
                                    if (pedido.has("direccion_entrega")) pedidosStr.append("  Entrega: ").append(pedido.getString("direccion_entrega")).append("\n");
                                    if (pedido.has("estado")) pedidosStr.append("  Estado: ").append(pedido.getString("estado")).append("\n");
                                    if (pedido.has("detalles")) {
                                        org.json.JSONArray detalles = pedido.getJSONArray("detalles");
                                        pedidosStr.append("  Productos:\n");
                                        for (int j = 0; j < detalles.length(); j++) {
                                            org.json.JSONObject detalle = detalles.getJSONObject(j);
                                            pedidosStr.append("    - Cantidad: ").append(detalle.optInt("cantidad_productos", 0));
                                            pedidosStr.append(", Precio: $").append(detalle.optDouble("precio_producto", 0));
                                            pedidosStr.append(", Subtotal: $").append(detalle.optDouble("subtotal", 0)).append("\n");
                                        }
                                    }
                                    pedidosStr.append("\n────────────────────────────\n\n");
                                }
                            }
                        } else {
                            pedidosStr.append("No se encontraron pedidos.\n\nRespuesta completa:\n");
                            pedidosStr.append(response.toString());
                        }
                    } catch (Exception e) {
                        pedidosStr.append("Error al procesar los pedidos.\n\nRespuesta completa:\n");
                        pedidosStr.append(response.toString());
                    }
                    // Crear un TextView scrollable para mostrar los pedidos
                    android.widget.ScrollView scrollView = new android.widget.ScrollView(requireContext());
                    android.widget.TextView textView = new android.widget.TextView(requireContext());
                    textView.setText(pedidosStr.toString());
                    textView.setTextSize(16);
                    textView.setPadding(32, 32, 32, 32);
                    textView.setVerticalScrollBarEnabled(true);
                    scrollView.addView(textView);
                    int dp = (int) (350 * getResources().getDisplayMetrics().density); // altura máxima
                    scrollView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp));
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Mis pedidos")
                        .setView(scrollView)
                        .setPositiveButton("OK", null)
                        .show();
                },
                error -> {
                    Toast.makeText(requireContext(), "Error al obtener pedidos", Toast.LENGTH_SHORT).show();
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
        });

        return view;
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
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
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

    private void uploadImage(byte[] imageBytes) {
        String url = "https://backmobile1.onrender.com/appUSERS/upload_profile_image/";

        // Mostrar un mensaje mientras se sube la imagen
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        // Crear una solicitud multipart para subir la imagen
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    // Procesar la respuesta del servidor
                    String responseBody = new String(response.data);
                    Log.d("ImagenPerfil", "Respuesta del servidor: " + responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("imagen_perfil_url");
                        // Limpiar completamente la caché de Glide
                        com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
                        
                        // Guardar la nueva URL de la imagen en SharedPreferences (una sola vez)
                        profileManager.saveProfileImageUrl(imageUrl);
                        
                        // Agregar un pequeño retraso para asegurar que la caché se limpie
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        // Actualizar la interfaz con la nueva imagen usando timestamp para forzar recarga
                        String imageUrlWithTimestamp = imageUrl + "?refresh=" + Math.random() + "&t=" + System.currentTimeMillis();
                        Log.d("ImagenPerfil", "Usando URL con parámetros aleatorios: " + imageUrlWithTimestamp);

                        // Forzar la descarga directa de la imagen sin usar Glide (solución más drástica)
                        new Thread(() -> {
                            try {
                                // Descargar la imagen directamente
                                java.net.URL url1 = new java.net.URL(imageUrl);
                                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                                
                                // Actualizar UI en el hilo principal
                                requireActivity().runOnUiThread(() -> {
                                    if (bitmap != null) {
                                        // Actualizar la ImageView con el bitmap descargado
                                        profileImage.setImageBitmap(bitmap);
                                        Log.d("ImagenPerfil", "Imagen actualizada directamente con Bitmap");
                                    } else {
                                        // Si falla, intentar con Glide como respaldo
                                        Glide.with(requireContext())
                                            .load(imageUrlWithTimestamp)
                                            .skipMemoryCache(true)
                                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                            .placeholder(R.drawable.default_profile)
                                            .error(R.drawable.default_profile)
                                            .into(profileImage);
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("ImagenPerfil", "Error al descargar directamente: " + e.getMessage());
                                // Si falla, intentar con Glide como respaldo en el hilo principal
                                requireActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext())
                                        .load(imageUrlWithTimestamp)
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .into(profileImage);
                                });
                            }
                        }).start();

                        // Ya no usamos el BroadcastReceiver, solo actualizamos la UI directamente
                        // y usamos el método simplificado de MainActivity
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.actualizarImagenPerfil(imageUrl);
                        }

                        Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Manejar el error
                    Log.e("ImagenPerfil", "Error al subir la imagen: " + error.toString());
                    Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", "1"); // Ajustar esto para usar el ID real del usuario
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir el token de autenticación si es necesario
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        // Añadir la imagen como un archivo al cuerpo de la solicitud
        multipartRequest.addByteData("image", imageBytes, "profile_image.jpg", "image/jpeg");

        // Añadir la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(multipartRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Llamar al backend para obtener la última información del perfil
        fetchProfileDataFromBackend();
    }    
    
    /**
     * Carga los datos del perfil desde el backend y actualiza la UI
     * Este método es público para permitir refrescar el perfil desde fuera del fragmento
     */
    public void fetchProfileDataFromBackend() {
        String url = "https://backmobile1.onrender.com/appUSERS/profile/";
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String name = response.getString("nombre");
                        String surname = response.getString("apellido");
                        String email = response.getString("email");
                        String phone = response.optString("telefono", "");
                        String profileImageUrl = response.optString("imagen_perfil_url", "");
                        
                        Log.d("ImagenPerfil", "URL recibida del backend en fetchProfileData: " + profileImageUrl);
                        
                        // Limpiar la caché de Glide antes de actualizar
                        com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
                        
                        // Si la URL de la imagen cambió, también eliminar específicamente ese archivo
                        String oldUrl = profileManager.getProfileImageUrl();
                        if (oldUrl != null && !oldUrl.equals(profileImageUrl)) {
                            com.example.food_front.utils.ImageCacheManager.removeFileFromCache(requireContext(), oldUrl);
                        }
                        
                        // Actualizar los datos del perfil
                        profileManager.saveInfo(name, surname, email, phone, profileImageUrl);
                        
                        // Refrescar la interfaz de usuario
                        displayUserProfile();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ProfileFragment", "Error al procesar la respuesta: " + e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ProfileFragment", "Error al obtener datos del perfil: " + error.toString());
                }
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
        
        // Aumentar el timeout para manejar servidor lento
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            30000, // 30 segundos de timeout
            1, // Número de reintentos
            1.0f // Sin backoff multiplier
        ));
        
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(request);
    }

    // Quitamos el BroadcastReceiver para simplificar y evitar errores
    @Override
    public void onStart() {
        super.onStart();
        // Ya no usamos BroadcastReceiver
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // Ya no necesitamos desregistrar nada
    }
    
    /**
     * Método público para actualizar la imagen de perfil desde fuera del fragmento
     * @param imageUrl URL de la nueva imagen
     */
    public void actualizarImagenDePerfil(String imageUrl) {
        // Limpiar caché
        com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
        
        // Guardar la nueva URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            profileManager.saveProfileImageUrl(imageUrl);
        }
        
        // Cargar la nueva imagen
        fetchProfileDataFromBackend();
    }
}
