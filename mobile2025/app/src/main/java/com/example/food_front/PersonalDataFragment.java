package com.example.food_front;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.utils.ProfileManager;
import com.example.food_front.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersonalDataFragment extends Fragment {

    private TextView tvNombre, tvApellido, tvEmail, tvTelefono;
    private EditText etNombre, etApellido, etEmail, etTelefono;
    private Button btnEdit, btnSave, btnCancel;
    private CardView viewModeCard, editModeCard;

    private ProfileManager profileManager;
    private SessionManager sessionManager;
    private RequestQueue requestQueue;
    private static final String UPDATE_URL = "https://backmobile1.onrender.com/appUSERS/update/";

    public PersonalDataFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_data, container, false);

        // Inicializar RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Inicializar ProfileManager y SessionManager
        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Inicializar las vistas - Modo visualización
        viewModeCard = view.findViewById(R.id.view_mode_card);
        tvNombre = view.findViewById(R.id.tv_nombre);
        tvApellido = view.findViewById(R.id.tv_apellido);
        tvEmail = view.findViewById(R.id.tv_email);
        tvTelefono = view.findViewById(R.id.tv_telefono);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Inicializar las vistas - Modo edición
        editModeCard = view.findViewById(R.id.edit_mode_card);
        etNombre = view.findViewById(R.id.et_nombre);
        etApellido = view.findViewById(R.id.et_apellido);
        etEmail = view.findViewById(R.id.et_email);
        etTelefono = view.findViewById(R.id.et_telefono);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Obtener y mostrar datos personales
        displayPersonalData();

        // Configurar el botón de edición
        btnEdit.setOnClickListener(v -> switchToEditMode());

        // Configurar el botón de guardar
        btnSave.setOnClickListener(v -> updateUserData());

        // Configurar el botón de cancelar
        btnCancel.setOnClickListener(v -> switchToViewMode());

        return view;
    }

    private void displayPersonalData() {
        // Obtener datos del ProfileManager
        String nombre = profileManager.getName();
        String apellido = profileManager.getSurname();
        String email = profileManager.getEmail();
        String telefono = profileManager.getPhone();

        // Mostrar los datos en los TextViews
        tvNombre.setText(nombre != null ? nombre : "No disponible");
        tvApellido.setText(apellido != null ? apellido : "No disponible");
        tvEmail.setText(email != null ? email : "No disponible");
        tvTelefono.setText(telefono != null ? telefono : "No disponible");
    }

    private void switchToEditMode() {
        // Cargar los datos actuales en los campos de edición
        etNombre.setText(profileManager.getName());
        etApellido.setText(profileManager.getSurname());
        etEmail.setText(profileManager.getEmail());
        etTelefono.setText(profileManager.getPhone());

        // Cambiar la visibilidad de las tarjetas
        viewModeCard.setVisibility(View.GONE);
        editModeCard.setVisibility(View.VISIBLE);
    }

    private void switchToViewMode() {
        // Cambiar la visibilidad de las tarjetas
        editModeCard.setVisibility(View.GONE);
        viewModeCard.setVisibility(View.VISIBLE);
    }

    private void updateUserData() {
        // Mostrar diálogo de progreso mientras se actualiza
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Actualizando datos...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Obtener los valores ingresados
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        // Validar campos
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        // Crear JSON con los datos del usuario
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("nombre", nombre);
            requestData.put("apellido", apellido);
            requestData.put("email", email);
            requestData.put("telefono", telefono);

            // Mostrar el JSON que estamos enviando para depuración
            Log.d("UpdateUser", "JSON enviado: " + requestData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear la solicitud
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.PUT,
            UPDATE_URL,
            requestData,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("UpdateUser", "Respuesta: " + response.toString());
                    progressDialog.dismiss();
                    try {
                        // Verificar si la respuesta contiene un indicador de éxito
                        // Algunos servidores devuelven "success", otros "status" o "code"
                        if (response.has("success")) {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                handleSuccessfulUpdate(nombre, apellido, email, telefono);
                            } else {
                                String error = response.optString("error", "Error desconocido");
                                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Si no hay campo "success", asumimos que la operación fue exitosa
                            // ya que obtuvimos una respuesta sin errores
                            handleSuccessfulUpdate(nombre, apellido, email, telefono);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    String errorMsg = "Error al actualizar los datos. ";

                    // Intentar obtener más información sobre el error
                    if (error.networkResponse != null) {
                        errorMsg += "Código: " + error.networkResponse.statusCode;

                        // Intentar obtener el cuerpo de la respuesta de error
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e("UpdateUser", "Error response: " + responseBody);
                            errorMsg += " - " + responseBody;
                        } catch (Exception e) {
                            Log.e("UpdateUser", "Error al leer el cuerpo del error", e);
                        }
                    }

                    Log.e("UpdateUser", errorMsg, error);
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Añadir token de autenticación si existe
                String token = sessionManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                    // Mostrar el token que estamos usando para depuración
                    Log.d("UpdateUser", "Token enviado: " + token);
                } else {
                    Log.e("UpdateUser", "No hay token disponible");
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Configurar timeout más largo para la solicitud
        jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            30000, // 30 segundos de timeout
            0, // sin reintentos
            com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Añadir solicitud a la cola
        requestQueue.add(jsonObjectRequest);
    }

    private void handleSuccessfulUpdate(String nombre, String apellido, String email, String telefono) {
        // Actualizar datos en ProfileManager
        profileManager.saveInfo(nombre, apellido, email, telefono);

        // Actualizar la vista
        displayPersonalData();
        switchToViewMode();

        Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
    }
}
