package com.example.food_front;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.adapters.ProductoAdapter;
import com.example.food_front.models.Carrito;
import com.example.food_front.models.Producto;
import com.example.food_front.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> productList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        recyclerView = view.findViewById(R.id.recyclerview_producto);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        adapter = new ProductoAdapter(productList, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onAgregarCarritoClick(Producto producto) {
                Toast.makeText(getContext(), "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
                // Agregar producto al carrito en la base de datos
                agregarProductoAlCarrito(producto.getIdProducto()); // Cambia la cantidad según sea necesario
            }
        });

        recyclerView.setAdapter(adapter);

        // Obtener el id de la categoría desde los argumentos (si existe)
        int categoriaId = 0;
        if (getArguments() != null) {
            categoriaId = getArguments().getInt("categoria_id", 0);
        }
        cargarProductos(categoriaId);

        return view;
    }

    // Modificado para recibir el id de la categoría
    private void cargarProductos(int categoriaId) {
        String url = "https://backmobile1.onrender.com/api/producto/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                            Log.e("ProductsFragment", "Error al parsear el JSON: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ProductsFragment", "Error en la solicitud: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });

        // Añadir la solicitud a la cola
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    private void agregarProductoAlCarrito(int idProducto) {
        String url = "https://backmobile1.onrender.com/appCART/agregar/" + idProducto + "/";

        SessionManager sessionManager = new SessionManager(getContext());
        String token = sessionManager.getToken();
        Log.d("AuthToken", "Token usado en la solicitud: " + token);

        if (token != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // Maneja la respuesta aquí, por ejemplo, muestra un mensaje de éxito
                        Toast.makeText(getContext(), "Producto agregado al carrito en la base de datos", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Maneja el error aquí
                        Log.e("ProductsFragment", "Error al agregar al carrito: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e("ProductsFragment", "Código de respuesta: " + error.networkResponse.statusCode);
                        }
                        Toast.makeText(getContext(), "Error al agregar producto al carrito", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token); // Usa el token almacenado
                    Log.d("HeadersDebug", "Headers: " + headers); // Verificar que se envíen los headers
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("direccion", "casa"); // Dirección hardcodeada
                    params.put("cantidad", "1"); // Cantidad fija
                    Log.d("ParamsDebug", "Params: " + params); // Verificar que se envíen los parámetros
                    return params;
                }
            };

            // Añadir la solicitud a la cola
            Volley.newRequestQueue(getContext()).add(stringRequest);
        } else {
            // Maneja el caso en que no hay token
            Toast.makeText(getContext(), "Debes iniciar sesión para agregar productos al carrito", Toast.LENGTH_SHORT).show();
        }
    }
}
