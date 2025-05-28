package com.example.food_front;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.adapters.CarritoAdapter;
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

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CarritoAdapter adapter;
    private List<Carrito> carritoList;
    private int precioTotal;
    TextView tvTotal;
    Button btnConfirmar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerView = view.findViewById(R.id.recyclerview_carrito);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvTotal = view.findViewById(R.id.text_total_precio);
        btnConfirmar = view.findViewById(R.id.button_confirmar_pedido);

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediretToCheckout();
            }
        });

        carritoList = new ArrayList<>();

        CarritoAdapter.OnClickListener carritoListener = new CarritoAdapter.OnClickListener() {
            @Override
            public void onDeleteClick(Carrito carrito) {
                borrardelCarrito(carrito.getIdCarrito());
                Toast.makeText(getContext(), "Producto borrado del carrito", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddClick(Carrito carrito) {
                int cantidad = carrito.getCantidad() + 1;

                actualizarCantidad(carrito.getIdCarrito(), cantidad);
                Toast.makeText(getContext(), "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMinusClick(Carrito carrito) {
                int cantidad = carrito.getCantidad() - 1;
                if (cantidad == 0 ) {
                    return;
                }
                actualizarCantidad(carrito.getIdCarrito(), cantidad);
                Toast.makeText(getContext(), "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
            }
        };

        adapter = new CarritoAdapter(carritoList, carritoListener);



        recyclerView.setAdapter(adapter);
        cargarCarritos();

        return view;
    }

    private void cargarCarritos() {
        String url = "https://backmobile1.onrender.com/appCART/ver/";

        SessionManager sessionManager = new SessionManager(getContext());
        String token = sessionManager.getToken();
        Log.d("AuthToken", "Token usado en la solicitud: " + token);

        if (token != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                // Parsear el JSON
                                JSONArray jsonArray = new JSONArray(response);
                                carritoList.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    // Obtener los detalles del producto desde el JSON
                                    int id_carrito = jsonObject.getInt("id");
                                    String nombre_producto = jsonObject.getString("producto");
                                    int cantidad = jsonObject.getInt("cantidad");
                                    double precio = jsonObject.getDouble("precio");
                                    String imagenUrl = jsonObject.getString("imageURL");

                                    precioTotal += cantidad * precio;

                                    // Crear un nuevo objeto Producto
                                    Carrito carrito = new Carrito(id_carrito, nombre_producto, cantidad, precio, imagenUrl);
                                    carritoList.add(carrito);  // Añadir a la lista
                                }


                                if (carritoList.isEmpty()) {
                                    tvTotal.setVisibility(View.GONE);
                                    btnConfirmar.setEnabled(false);
                                    btnConfirmar.setVisibility(View.GONE);
                                    rediretToEmptyCart();
                                } else {
                                    tvTotal.setVisibility(View.VISIBLE);
                                }

                                tvTotal.setText("Total: $" + precioTotal);


                                // Notificar al adaptador que los datos han cambiado
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("CarritoFragment", "Error al parsear el JSON: " + e.getMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("CarritoFragment", "Error en la solicitud: " + error.getMessage());
                    Toast.makeText(getContext(), "Error al cargar carritos", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token); // Usa el token almacenado
                    Log.d("HeadersDebug", "Headers: " + headers); // Verificar que se envíen los headers
                    return headers;
                }
            };
            // Añadir la solicitud a la cola
            Volley.newRequestQueue(getContext()).add(stringRequest);
        } else {
            // Maneja el caso en que no hay token
            Toast.makeText(getContext(), "Debes iniciar sesión para agregar productos al carrito", Toast.LENGTH_SHORT).show();
        }




    }

    private void borrardelCarrito(int idProducto) {
        String url = "https://backmobile1.onrender.com/appCART/eliminar/" + idProducto + "/";

        SessionManager sessionManager = new SessionManager(getContext());
        String token = sessionManager.getToken();
        Log.d("AuthToken", "Token usado en la solicitud: " + token);

        if (token != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        // Maneja la respuesta aquí, por ejemplo, muestra un mensaje de éxito
                        Toast.makeText(getContext(), "Carrito borrado de la base de datos", Toast.LENGTH_SHORT).show();
                        cargarCarritos();
                    },
                    error -> {
                        // Maneja el error aquí
                        Log.e("CartFragment", "Error al borrar carrito: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e("CartFragment", "Código de respuesta: " + error.networkResponse.statusCode);
                        }
                        Toast.makeText(getContext(), "Error al borrar carrito", Toast.LENGTH_SHORT).show();
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

    private void actualizarCantidad(int idProducto, int cantidad) {
        String url = "https://backmobile1.onrender.com/appCART/modificar_cantidad/" + idProducto + "/";

        SessionManager sessionManager = new SessionManager(getContext());
        String token = sessionManager.getToken();
        Log.d("AuthToken", "Token usado en la solicitud: " + token);

        // Crear el json que se enviará en el body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cantidad", cantidad);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (token != null) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(getContext(), "update successful", Toast.LENGTH_SHORT).show();
                            cargarCarritos();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
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
                    return params;
                }
            };

            // Añadir la solicitud a la cola
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(request);
        } else {
            // Maneja el caso en que no hay token
            Toast.makeText(getContext(), "Debes iniciar sesión para agregar productos al carrito", Toast.LENGTH_SHORT).show();
        }
    }

    private void rediretToCheckout() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new DatosEntregaFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        }


    private void rediretToEmptyCart() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new EmptyCartFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
