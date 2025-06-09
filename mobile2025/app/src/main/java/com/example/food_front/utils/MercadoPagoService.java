package com.example.food_front.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.models.MercadoPagoPreference;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para interactuar con la API de MercadoPago
 */
public class MercadoPagoService {
    private static final String TAG = "MercadoPagoService";
    private static final String BASE_URL = "https://backmp.onrender.com"; // URL donde está desplegado el servicio de MP
    
    private final Context context;
    private final RequestQueue requestQueue;
    private final SessionManager sessionManager;
    private final Gson gson;
    
    // Constantes para los estados de pago
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_IN_PROCESS = "in_process";

    public interface MercadoPagoCallback {
        void onSuccess(MercadoPagoPreference preference);
        void onError(String errorMessage);
    }    public MercadoPagoService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.sessionManager = new SessionManager(context);
        this.gson = new Gson();
    }
    
    /**
     * Crea una preferencia de pago en MercadoPago
     * @param email Email del usuario (opcional)
     * @param callback Callback para manejar la respuesta
     */
    public void createPreference(String email, MercadoPagoCallback callback) {
        String url = BASE_URL + "/payment/create-preference/";
        String token = sessionManager.getToken();
        
        if (token == null) {
            callback.onError("Necesitas iniciar sesión para realizar un pago");
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_token", token);
            
            // Si el email es null, intentamos usar un email predeterminado para evitar errores
            if (email != null && !email.isEmpty()) {
                jsonBody.put("email", email);
            } else {
                // Usar un valor por defecto para evitar problemas en el servidor
                jsonBody.put("email", "usuario@example.com");
                Log.w(TAG, "Email no disponible. Usando email predeterminado.");
            }

            // Indicar el entorno para el backend (mobile)
            jsonBody.put("env", "mobile");

            // Log para depuración
            Log.d(TAG, "Enviando solicitud a: " + url);
            Log.d(TAG, "Cuerpo de la solicitud: " + jsonBody.toString());            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Respuesta MercadoPago: " + response.toString());
                                
                                // Verificar si la respuesta contiene los campos necesarios
                                if (!response.has("init_point")) {
                                    throw new JSONException("La respuesta no contiene la URL de pago (init_point)");
                                }
                                
                                // Extraer los datos de la respuesta
                                String initPoint = response.getString("init_point");
                                String preferenceId = response.optString("preference_id", "");
                                String paymentRequestId = response.optString("payment_request_id", "");
                                
                                // Crear el objeto de preferencia
                                MercadoPagoPreference preference = new MercadoPagoPreference(
                                        initPoint,
                                        preferenceId,
                                        paymentRequestId
                                );
                                
                                Log.d(TAG, "Preferencia creada con URL: " + initPoint);
                                callback.onSuccess(preference);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error al parsear la respuesta", e);
                                callback.onError("Error al procesar la respuesta del servidor: " + e.getMessage());
                            }
                        }
                    },                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error en la solicitud a MercadoPago", error);
                            String errorMsg = "Error al conectar con el servicio de pagos";
                            
                            if (error.networkResponse != null) {
                                // Log para depuración
                                Log.d(TAG, "Código de estado: " + error.networkResponse.statusCode);
                                
                                // Manejar específicamente diferentes códigos de error
                                switch (error.networkResponse.statusCode) {
                                    case 404:
                                        errorMsg = "El servicio de pagos no está disponible en este momento. Por favor, intenta más tarde.";
                                        break;
                                    case 500:
                                        errorMsg = "Error interno del servidor de pagos. Por favor, intenta más tarde.";
                                        // Añadir enlaces a la documentación para ayudar en el desarrollo
                                        errorMsg += " busca info de la implementación https://central.sonatype.com/artifact/com.mercadopago/sdk-java?smo=true https://github.com/mercadopago/sdk-java";
                                        break;
                                    default:
                                        // Intentar procesamiento normal
                                        if (error.networkResponse.data != null) {
                                            try {
                                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                                Log.d(TAG, "Cuerpo de respuesta de error: " + responseBody);
                                                
                                                // Verificar si la respuesta es HTML
                                                if (responseBody.trim().startsWith("<!DOCTYPE") || 
                                                    responseBody.trim().startsWith("<html")) {
                                                    // Es una respuesta HTML, manejar como error general
                                                    errorMsg = "Error en la comunicación con el servidor de pagos.";
                                                } else {
                                                    // Intentar procesar como JSON
                                                    try {
                                                        JSONObject data = new JSONObject(responseBody);
                                                        if (data.has("error")) {
                                                            errorMsg = data.getString("error");
                                                        } else if (data.has("message")) {
                                                            errorMsg = data.getString("message");
                                                        }
                                                    } catch (JSONException je) {
                                                        // No es un JSON válido
                                                        Log.e(TAG, "La respuesta no es un JSON válido", je);
                                                        errorMsg = "Error en la comunicación con el servidor. Por favor, intenta más tarde.";
                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error al procesar la respuesta de error", e);
                                            }
                                        }
                                }
                                errorMsg += " (código: " + error.networkResponse.statusCode + ")";
                            }
                            
                            callback.onError(errorMsg);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el cuerpo de la solicitud", e);
            callback.onError("Error al preparar la solicitud de pago");
        }
    }

    /**
     * Crea una preferencia de pago en MercadoPago (adaptado para enviar body completo como el frontend web)
     * @param email Email del usuario (opcional)
     * @param items Lista de productos del carrito (JSONArray)
     * @param payer JSONObject con datos del comprador
     * @param externalReference Referencia externa (idPedido o timestamp)
     * @param callback Callback para manejar la respuesta
     */
    public void createPreferenceFull(String email, org.json.JSONArray items, org.json.JSONObject payer, String externalReference, MercadoPagoCallback callback) {
        String url = BASE_URL + "/payment/create-preference/";
        String token = sessionManager.getToken();
        if (token == null) {
            callback.onError("Necesitas iniciar sesión para realizar un pago");
            return;
        }
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_token", token);
            jsonBody.put("email", email != null && !email.isEmpty() ? email : "usuario@example.com");
            jsonBody.put("items", items); // array de productos
            jsonBody.put("payer", payer); // objeto con datos del comprador
            JSONObject backUrls = new JSONObject();
            backUrls.put("success", "https://backmp.onrender.com/payment/success/");
            backUrls.put("failure", "https://ispcfood.netlify.app/checkout");
            backUrls.put("pending", "https://ispcfood.netlify.app/exito");
            jsonBody.put("back_urls", backUrls);
            jsonBody.put("auto_return", "approved");
            jsonBody.put("external_reference", externalReference);

            Log.d(TAG, "Enviando solicitud a: " + url);
            Log.d(TAG, "Cuerpo de la solicitud: " + jsonBody.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Respuesta MercadoPago: " + response.toString());
                                if (!response.has("init_point")) {
                                    throw new JSONException("La respuesta no contiene la URL de pago (init_point)");
                                }
                                String initPoint = response.getString("init_point");
                                String preferenceId = response.optString("preference_id", "");
                                String paymentRequestId = response.optString("payment_request_id", "");
                                MercadoPagoPreference preference = new MercadoPagoPreference(
                                        initPoint,
                                        preferenceId,
                                        paymentRequestId
                                );
                                callback.onSuccess(preference);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error al parsear la respuesta", e);
                                callback.onError("Error al procesar la respuesta del servidor: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error en la solicitud a MercadoPago", error);
                            String errorMsg = "Error al conectar con el servicio de pagos";
                            if (error.networkResponse != null) {
                                Log.d(TAG, "Código de estado: " + error.networkResponse.statusCode);
                            }
                            callback.onError(errorMsg);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el body JSON", e);
            callback.onError("Error al crear el body JSON: " + e.getMessage());
        }
    }

    /**
     * Verifica el estado de un pago
     * @param paymentRequestId ID de la solicitud de pago
     * @param callback Callback para manejar la respuesta
     */
    public void checkPaymentStatus(String paymentRequestId, PaymentStatusCallback callback) {
        String url = BASE_URL + "/payment/status/" + paymentRequestId + "/";
        String token = sessionManager.getToken();
        
        if (token == null) {
            callback.onError("Necesitas iniciar sesión para verificar el estado del pago");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Respuesta estado de pago: " + response.toString());
                            String status = response.getString("status");
                            callback.onSuccess(status);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear la respuesta de estado", e);
                            callback.onError("Error al procesar la respuesta del servidor");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error al verificar estado del pago", error);
                        String errorMsg = "Error al verificar el estado del pago";
                        
                        if (error.networkResponse != null) {
                            errorMsg += " (código: " + error.networkResponse.statusCode + ")";
                        }
                        
                        callback.onError(errorMsg);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Token " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Interfaz para el callback de confirmación de órdenes
     */
    public interface OrderConfirmCallback {
        void onSuccess(String orderId);
        void onError(String errorMessage);
    }

    /**
     * Confirma un pedido en el backend después de un pago exitoso
     * @param paymentRequestId ID de la solicitud de pago
     * @param callback Callback para manejar la respuesta
     */
    public void confirmOrder(String paymentRequestId, OrderConfirmCallback callback) {
        String url = BASE_URL + "/payment/confirm/" + paymentRequestId + "/";
        String token = sessionManager.getToken();
        
        if (token == null) {
            callback.onError("Necesitas iniciar sesión para confirmar el pedido");
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_token", token);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Respuesta confirmación pedido: " + response.toString());
                                String orderId = response.optString("order_id", "");
                                callback.onSuccess(orderId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear la respuesta de confirmación", e);
                                callback.onError("Error al procesar la respuesta del servidor");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error en la solicitud de confirmación", error);
                            String errorMsg = "Error al confirmar el pedido";
                            
                            if (error.networkResponse != null) {
                                errorMsg += " (código: " + error.networkResponse.statusCode + ")";
                            }
                            
                            callback.onError(errorMsg);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Token " + token);
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el cuerpo de la solicitud", e);
            callback.onError("Error al preparar la confirmación del pedido");
        }
    }

    /**
     * Interfaz para el callback de verificación de estado de pago
     */
    public interface PaymentStatusCallback {
        void onSuccess(String status);
        void onError(String errorMessage);
    }
}
