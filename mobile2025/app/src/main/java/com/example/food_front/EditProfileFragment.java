package com.example.food_front;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class EditProfileFragment extends Fragment {

    private TextView tvNombre, tvLastname, tvEmail, tvPhone;
    private ProfileManager profileManager;
    private SessionManager sessionManager;
    private Button btnSaveChanges;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Inicializar las vistas
        tvNombre = view.findViewById(R.id.edit_name);
        tvLastname = view.findViewById(R.id.edit_lastname);
        tvEmail = view.findViewById(R.id.edit_email);
        tvPhone = view.findViewById(R.id.edit_phone);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);

        profileManager = new ProfileManager(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Llamar al backend para obtener los datos del perfil
        displayUserProfile();

        // Listener para el botón de guardar cambios
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();  // Método para enviar los cambios al backend
            }
        });

        // Encontrar la flecha de regreso
        ImageView backArrow = view.findViewById(R.id.back_arrow);
        // Agregar un listener para cuando se haga clic en la flecha
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Al hacer clic en la flecha, volver al fragmento anterior
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack(); // Regresar al fragmento anterior
            }
        });
        return view;
    }

    private void displayUserProfile() {
        // Usar los métodos específicos para obtener los datos
        String name = profileManager.getName();
        String surname = profileManager.getSurname();
        String email = profileManager.getEmail();
        String phone = profileManager.getPhone();

        // Mostrar los datos en los TextViews
        tvNombre.setText(name);
        tvLastname.setText(surname);
        tvEmail.setText(email);
        tvPhone.setText(phone);
    }

    private void updateProfile() {
        // Obtener los datos editados
        final String updatedName = tvNombre.getText().toString().trim();
        final String updatedSurname = tvLastname.getText().toString().trim();
        final String updatedEmail = tvEmail.getText().toString().trim();
        final String updatedPhone = tvPhone.getText().toString().trim();

        // Validar que no estén vacíos
        if (updatedName.isEmpty() || updatedSurname.isEmpty() || updatedEmail.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(getContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = " https://backmobile1.onrender.com/appUSERS/update/";

        // Crear el JSON que se enviará en el body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("nombre", updatedName);
            requestBody.put("apellido", updatedSurname);
            requestBody.put("email", updatedEmail);
            requestBody.put("telefono", updatedPhone);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Crear la request de Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Guardar los datos actualizados en ProfileManager
                        profileManager.saveInfo(updatedName, updatedSurname, updatedEmail, updatedPhone);
                        Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

                        // Volver al fragmento anterior
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + sessionManager.getToken());  // Agregar el token
                return headers;
            }
        };

        // Agregar la request a la cola de Volley
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }
}