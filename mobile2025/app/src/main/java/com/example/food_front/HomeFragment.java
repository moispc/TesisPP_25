package com.example.food_front;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.food_front.adapters.ProductoAdapter;
import com.example.food_front.models.Producto;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private TextView tvName;
    private CircleImageView profileImage;
    private ProfileManager profileManager;
    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> productList;

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        tvName = view.findViewById(R.id.txtUser);
        profileImage = view.findViewById(R.id.profileImage);
        Button button1 = view.findViewById(R.id.btn1);
        Button button2 = view.findViewById(R.id.btn);
        Button button3 = view.findViewById(R.id.btn3);
        Button button4 = view.findViewById(R.id.btn4);
        ImageView imageView1 = view.findViewById(R.id.imageView3);
        ImageView imageView2 = view.findViewById(R.id.imageView4);
        FloatingActionButton btnWebsite = view.findViewById(R.id.btnWebsite);
        
        // Inicializar RecyclerView para productos
        recyclerView = view.findViewById(R.id.recyclerview_productos_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Inicializar lista de productos y adaptador
        productList = new ArrayList<>();
        adapter = new ProductoAdapter(productList, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onAgregarCarritoClick(Producto producto) {
                agregarProductoAlCarrito(producto.getIdProducto());
            }
        });
        
        recyclerView.setAdapter(adapter);

        // Configurar el onClick para el botón de navegación web
        btnWebsite.setOnClickListener(v -> {
            String url = "https://ispcfood.netlify.app/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        profileManager = new ProfileManager(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());

        // Mostrar el nombre del usuario e imagen de perfil
        mostrarNombreUsuario();
        cargarImagenPerfil();
        // ...NO cargar productos automáticamente...
        // cargarProductos(0); // <--- QUITADO para que Home no muestre la carta por defecto

        // Modificamos los listeners para cargar productos en el mismo fragmento
        button1.setOnClickListener(v -> navegarAProductosConFiltro(3)); // Hamburguesas id 3
        button2.setOnClickListener(v -> navegarAProductosConFiltro(1)); // Empanadas id 1
        button3.setOnClickListener(v -> navegarAProductosConFiltro(0)); // Todos los productos
        button4.setOnClickListener(v -> navegarAProductosConFiltro(2)); // Lomitos id 2
        imageView1.setOnClickListener(v -> navegarAProductosConFiltro(3)); // Hamburguesas id 3
        imageView2.setOnClickListener(v -> navegarAProductosConFiltro(2)); // Lomitos id 2

        return view;
    }

    private void mostrarNombreUsuario() {
        String nombreGuardado = profileManager.getName();
        if (nombreGuardado != null) {
            tvName.setText(getString(R.string.bienvenido_usuario, nombreGuardado));
        } else {
            tvName.setText(R.string.usuario);
        }
    }

    private void cargarImagenPerfil() {
        String baseUrl = profileManager.getProfileImageUrl(); // Obtener URL base sin timestamp
        String imageUrl = profileManager.getProfileImageUrlWithTimestamp(); // URL con timestamp para Glide
        
        Log.d(TAG, "URL base recuperada: " + baseUrl);
        Log.d(TAG, "URL con timestamp: " + imageUrl);

        if (baseUrl != null && !baseUrl.isEmpty()) {
            // Limpiar toda caché anterior
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
            
            // Descargar la imagen directamente sin usar Glide
            new Thread(() -> {
                try {
                    // Descargar la imagen directamente
                    java.net.URL url = new java.net.URL(baseUrl);
                    java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setUseCaches(false); // Evitar caché
                    connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                    connection.setRequestProperty("Pragma", "no-cache");
                    connection.connect();
                    
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(connection.getInputStream());
                    
                    // Actualizar UI en hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                profileImage.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen cargada exitosamente con descarga directa");
                            } else {
                                // Si falla, intentar con Glide como respaldo
                                cargarImagenConGlide(imageUrl);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al descargar directamente: " + e.getMessage());
                    // En caso de error, intentar con Glide
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> cargarImagenConGlide(imageUrl));
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "No hay URL de imagen, usando imagen predeterminada");
            // Usar imagen predeterminada
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }
    
    private void cargarImagenConGlide(String imageUrl) {
        // Usar Glide como método alternativo
        Glide.with(requireContext())
            .load(imageUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .placeholder(R.drawable.default_profile)
            .error(R.drawable.default_profile)
            .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model,
                                               @NonNull Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Error al cargar la imagen: " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                  @NonNull Target<Drawable> target,
                                                  @NonNull DataSource dataSource,
                                                  boolean isFirstResource) {
                        Log.d(TAG, "Imagen cargada exitosamente desde: " + model);
                        return false;
                    }
                })
            .into(profileImage);
    }

    // Método para actualizar la imagen de perfil desde otro fragmento
    public void actualizarImagenPerfil(String url) {
        if (profileImage != null && url != null && !url.isEmpty()) {
            // Limpiar la caché primero
            com.example.food_front.utils.ImageCacheManager.clearGlideCache(requireContext());
            
            String imageUrlWithTimestamp = url + "?nocache=" + Math.random() + "&t=" + System.currentTimeMillis();
            Log.d(TAG, "Actualizando imagen desde otro fragmento: " + imageUrlWithTimestamp);

            // Forzar la descarga directa de la imagen sin usar Glide
            new Thread(() -> {
                try {
                    // Descargar la imagen directamente
                    java.net.URL url1 = new java.net.URL(url);
                    final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                    
                    // Actualizar UI en el hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                // Actualizar la ImageView con el bitmap descargado
                                profileImage.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen en HomeFragment actualizada directamente con Bitmap");
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
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al descargar directamente en HomeFragment: " + e.getMessage());
                    // Si falla, intentar con Glide como respaldo en el hilo principal
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            Glide.with(requireContext())
                                .load(imageUrlWithTimestamp)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                        });
                    }
                }
            }).start();
        }
    }

    // Método para cargar productos según la categoría
    private void cargarProductos(int categoriaId) {
        String url = "https://backmobile1.onrender.com/api/producto/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Parsear el JSON
                        JSONArray jsonArray = new JSONArray(response);
                        productList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            // Obtener los detalles del producto desde el JSON
                            int id_producto = jsonObject.getInt("id_producto");
                            String nombre_producto = jsonObject.getString("nombre_producto");
                            String descripcion = jsonObject.getString("descripcion");
                            double precio = jsonObject.getDouble("precio");
                            String imagenUrl = jsonObject.getString("imageURL");
                            int id_categoria = jsonObject.has("id_categoria") ? jsonObject.getInt("id_categoria") : 0;

                            // Filtrar por categoría si corresponde
                            if (categoriaId == 0 || id_categoria == categoriaId) {
                                Producto producto = new Producto(id_producto, nombre_producto, descripcion, precio, imagenUrl, id_categoria);
                                productList.add(producto);  // Añadir a la lista
                            }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error al parsear el JSON: " + e.getMessage());
                    }
                }, error -> {
                    Log.e(TAG, "Error en la solicitud: " + error.getMessage());
                }
        );

        // Añadir la solicitud a la cola
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    // Método para agregar productos al carrito
    private void agregarProductoAlCarrito(int idProducto) {
        String url = "https://backmobile1.onrender.com/appCART/agregar/" + idProducto + "/";

        SessionManager sessionManager = new SessionManager(getContext());
        String token = sessionManager.getToken();
        Log.d("AuthToken", "Token usado en la solicitud: " + token);

        if (token != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // Maneja la respuesta aquí
                        Log.d(TAG, "Producto agregado al carrito: " + response);
                        android.widget.Toast.makeText(getContext(), "Producto agregado al carrito", android.widget.Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Maneja el error aquí
                        Log.e(TAG, "Error al agregar al carrito: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Código de respuesta: " + error.networkResponse.statusCode);
                        }
                        android.widget.Toast.makeText(getContext(), "Error al agregar producto al carrito", android.widget.Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }

                @Override
                protected java.util.Map<String, String> getParams() {
                    java.util.Map<String, String> params = new java.util.HashMap<>();
                    params.put("direccion", "casa"); // Dirección hardcodeada
                    params.put("cantidad", "1"); // Cantidad fija
                    return params;
                }
            };

            // Añadir la solicitud a la cola
            Volley.newRequestQueue(getContext()).add(stringRequest);
        } else {
            // Maneja el caso en que no hay token
            android.widget.Toast.makeText(getContext(), "Debes iniciar sesión para agregar productos al carrito", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar imagen de perfil del servidor
        reloadProfileDataIfNeeded();
    }

    private void reloadProfileDataIfNeeded() {
        // Verificar si han pasado más de 5 minutos desde la última carga
        long lastUpdated = profileManager.getLastImageUpdate();
        long now = System.currentTimeMillis();
        
        if (now - lastUpdated > 5 * 60 * 1000) { // 5 minutos
            Log.d(TAG, "Han pasado más de 5 minutos, recargando datos del perfil");
            cargarImagenPerfil(); // Esto ya usa la URL con timestamp para forzar recarga
        }
    }

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

    // Nueva función para navegar a ProductsFragment con filtro
    private void navegarAProductosConFiltro(int categoriaId) {
        ProductsFragment productsFragment = new ProductsFragment();
        Bundle args = new Bundle();
        args.putInt("categoria_id", categoriaId);
        productsFragment.setArguments(args);
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container_view, productsFragment)
            .addToBackStack(null)
            .commit();
    }
}
