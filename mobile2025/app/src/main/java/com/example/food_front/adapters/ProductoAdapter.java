package com.example.food_front.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_front.R;
import com.example.food_front.models.Producto;
import com.bumptech.glide.Glide;


import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    // Lista de productos
    private List<Producto> listaProductos;


    // Interfaz para manejar clics en el botón de agregar al carrito
    public interface OnProductoClickListener {
        void onAgregarCarritoClick(Producto producto);
    }

    // Listener que será implementado en el Fragment o Activity
    private OnProductoClickListener listener;

    // Constructor que recibe la lista de productos y el listener
    public ProductoAdapter(List<Producto> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Infla el layout para cada ítem de la lista (crea la vista)
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        // Obtener el producto actual
        Producto producto = listaProductos.get(position);

        // Asignar los valores a las vistas
        holder.textViewNombre.setText(producto.getNombre());
        holder.textViewDescripcion.setText(producto.getDescripcion());
        holder.textViewPrecio.setText("$" + producto.getPrecio());

        // Cargar la imagen con Glide
        String imageUrl = producto.getImagenUrl();
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder) // Imagen por defecto mientras se carga
                    .error(R.drawable.error_image) // Imagen de error si no se carga
                    .into(holder.imageViewProducto); // Cargar en imageViewProducto
        } else {
            holder.imageViewProducto.setImageResource(R.drawable.placeholder); // Imagen predeterminada si no hay URL
        }

        // Configurar el clic del botón de "Agregar al Carrito"
        holder.buttonAgregarCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al listener para manejar el clic
                if (listener != null) {
                    listener.onAgregarCarritoClick(producto);
                }
            }
        });
    }

    // Devuelve el tamaño de la lista
    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    // ViewHolder que almacena las referencias a los elementos del layout de cada ítem
    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProducto;
        TextView textViewNombre;
        TextView textViewDescripcion;
        TextView textViewPrecio;
        Button buttonAgregarCarrito; // Referencia al botón

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProducto = itemView.findViewById(R.id.imageViewProducto);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            buttonAgregarCarrito = itemView.findViewById(R.id.button_add_to_cart); // Inicializar el botón
        }
    }
}
