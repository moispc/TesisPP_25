package com.example.food_front.models;

public class Carrito {
    private int idCarrito;
    private String producto;
    private int cantidad;
    private double precio;
    private String imageURL;

    // Constructor
    public Carrito(int idCarrito, String producto, int cantidad, double precio, String imageURL) {
        this.idCarrito = idCarrito;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio * cantidad;
        // Verifica si la URL de la imagen es null y maneja esto adecuadamente
        this.imageURL = (imageURL != null) ? imageURL : ""; // Asignar una cadena vacía si es null
    }

    // Método para obtener el ID
    public int getIdCarrito() {
        return idCarrito; // Este es el método que necesitas
    }

    // Getters
    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public String getImagenUrl() {
        return imageURL;
    }

}
