package com.example.food_front.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_front.R;
import com.example.food_front.models.Carrito;
import com.example.food_front.models.Producto;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    // Lista de carritos
    private List<Carrito> listaCarritos;

    // Interfaz para manejar clics en el botón de sumar cantidad
    public interface OnClickListener {
        void onDeleteClick(Carrito carrito);
        void onAddClick(Carrito carrito);
        void onMinusClick(Carrito carrito);
    }

    // Listener que será implementado en el Fragment o Activity
    private CarritoAdapter.OnClickListener listener;

    // Constructor que recibe la lista de productos y el listener
    public CarritoAdapter(List<Carrito> listaCarritos, CarritoAdapter.OnClickListener listener) {
        this.listaCarritos = listaCarritos;
        this.listener = listener;
    }


    @NonNull
    @Override
    // Infla el layout para cada ítem de la lista (crea la vista)
    public CarritoAdapter.CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new CarritoAdapter.CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoAdapter.CarritoViewHolder holder, int position) {
        // Obtener el producto actual
        Carrito carrito = listaCarritos.get(position);

        // Asignar los valores a las vistas
        holder.textViewProducto.setText(carrito.getProducto());
        holder.textViewPrecio.setText("$" + carrito.getPrecio());
        holder.textCantidad.setText(String.valueOf(carrito.getCantidad()));

        // Cargar la imagen con Glide
        String imageUrl = carrito.getImagenUrl();
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder) // Imagen por defecto mientras se carga
                    .error(R.drawable.error_image) // Imagen de error si no se carga
                    .into(holder.imageViewProducto); // Cargar en imageViewProducto
        } else {
            holder.imageViewProducto.setImageResource(R.drawable.placeholder); // Imagen predeterminada si no hay URL
        }

//         Configurar el clic del botón de "Agregar al Carrito"
        holder.buttonBorrarCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al listener para manejar el clic
                if (listener != null) {
                    listener.onDeleteClick(carrito);
                }
            }
        });

//        Configurar el clic del botón de "Agregar al Carrito"
        holder.buttonAddCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al listener para manejar el clic
                if (listener != null) {
                    listener.onAddClick((carrito));
                }
            }
        });

        //        Configurar el clic del botón de "Agregar al Carrito"
        holder.buttonMinusCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al listener para manejar el clic
                if (listener != null) {
                    listener.onMinusClick((carrito));
                }
            }
        });
    }


    // Devuelve el tamaño de la lista
    @Override
    public int getItemCount() {
        return listaCarritos.size();
    }

    // ViewHolder que almacena las referencias a los elementos del layout de cada ítem
    static class CarritoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProducto, buttonBorrarCarrito;
        TextView textViewProducto, textViewPrecio, textCantidad;
        Button buttonAddCarrito, buttonMinusCarrito;


        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProducto = itemView.findViewById(R.id.image_producto);
            textViewProducto = itemView.findViewById(R.id.text_nombre_producto);
            textViewPrecio = itemView.findViewById(R.id.text_precio_producto);
            textCantidad = itemView.findViewById(R.id.text_cantidad);
            buttonBorrarCarrito = itemView.findViewById(R.id.image_eliminar_producto); // Inicializar el botón
            buttonAddCarrito = itemView.findViewById(R.id.button_plus);
            buttonMinusCarrito = itemView.findViewById(R.id.button_minus);
        }
    }

}
