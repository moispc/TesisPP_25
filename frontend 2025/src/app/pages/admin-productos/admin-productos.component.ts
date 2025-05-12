import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductsService } from '../../services/products.service';
import { Producto } from '../../model/producto.model';
import { ToastrService } from 'ngx-toastr';
import { ImageUploadService } from '../../services/image-upload.service';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-productos.component.html',
  styleUrl: './admin-productos.component.css',
})
export class AdminProductosComponent implements OnInit {
  productos: Producto[] = [];
  productoForm: FormGroup;
  isLoading = false;
  imagenArchivo: File | null = null;
  imagenError: string | null = null;
  cargandoImagen = false;

  // Modal de confirmación para eliminar producto
  productoAEliminar: Producto | null = null;
  mostrarModalEliminar = false;

  // Edición de producto
  mostrarModalEditar = false;
  productoAEditar: Producto | null = null;
  editarForm: FormGroup = this.fb.group({
    nombre_producto: [''],
    precio: [0],
    stock: [0],
    id_categoria: [1],
    descripcion: [''],
    imageURL: ['']
  });

  // Subida de imagen en edición
  imagenArchivoEdicion: File | null = null;
  imagenErrorEdicion: string | null = null;
  cargandoImagenEdicion = false;

  constructor(
    private fb: FormBuilder,
    private productsService: ProductsService,
    private toastr: ToastrService,
    private imageUploadService: ImageUploadService
  ) {
    this.productoForm = this.fb.group({
      nombre_producto: ['', Validators.required],
      precio: [0, [Validators.required, Validators.min(1)]],
      descripcion: ['', Validators.required],
      imageURL: ['', Validators.required],
      stock: [0, [Validators.required, Validators.min(0)]],
      id_categoria: [1, Validators.required],
    });
  }

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos() {
    this.isLoading = true;
    this.productsService.getProducts().subscribe({
      next: (productos) => {
        this.productos = productos;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Error al cargar productos');
        this.isLoading = false;
      },
    });
  }

  // Manejar selección de imagen
  onImagenSeleccionada(event: any): void {
    this.imagenError = null;
    const file = event.target.files[0];
    if (file) {
      // Validar tamaño (máx 2MB)
      if (file.size > 2 * 1024 * 1024) {
        this.imagenError = 'La imagen no debe superar los 2MB';
        return;
      }
      // Validar tipo
      if (!file.type.match(/image\/(jpeg|jpg|png|gif|webp)/)) {
        this.imagenError = 'Formato de imagen no válido. Use JPG, PNG, GIF o WEBP.';
        return;
      }
      this.imagenArchivo = file;
      this.cargandoImagen = true;
      this.imageUploadService.uploadImage(file).subscribe({
        next: (res) => {
          const nuevaUrl = res.secure_url;
          this.productoForm.patchValue({ imageURL: nuevaUrl });
          this.toastr.success('Imagen subida correctamente');
          this.cargandoImagen = false;
        },
        error: () => {
          this.imagenError = 'Error al subir la imagen';
          this.cargandoImagen = false;
        }
      });
    }
  }

  onImagenSeleccionadaEdicion(event: any): void {
    this.imagenErrorEdicion = null;
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        this.imagenErrorEdicion = 'La imagen no debe superar los 2MB';
        return;
      }
      if (!file.type.match(/image\/(jpeg|jpg|png|gif|webp)/)) {
        this.imagenErrorEdicion = 'Formato de imagen no válido. Use JPG, PNG, GIF o WEBP.';
        return;
      }
      this.imagenArchivoEdicion = file;
      this.cargandoImagenEdicion = true;
      this.imageUploadService.uploadImage(file).subscribe({
        next: (res) => {
          const nuevaUrl = res.secure_url;
          this.editarForm.patchValue({ imageURL: nuevaUrl });
          if (this.productoAEditar) this.productoAEditar.imageURL = nuevaUrl;
          this.cargandoImagenEdicion = false;
        },
        error: () => {
          this.imagenErrorEdicion = 'Error al subir la imagen';
          this.cargandoImagenEdicion = false;
        }
      });
    }
  }

  onSubmit() {
    if (this.productoForm.invalid) {
      this.productoForm.markAllAsTouched();
      return;
    }
    if (!this.productoForm.value.imageURL) {
      this.imagenError = 'Debe subir una imagen para el producto';
      return;
    }
    const nuevoProducto = this.productoForm.value;
    // Validar duplicados por nombre
    if (this.productos.some(p => p.nombre_producto.trim().toLowerCase() === nuevoProducto.nombre_producto.trim().toLowerCase())) {
      this.toastr.error('Ya existe un producto con ese nombre');
      return;
    }
    this.productsService.addProduct(nuevoProducto).subscribe({
      next: (producto) => {
        this.toastr.success('Producto agregado correctamente');
        this.productoForm.reset();
        this.cargarProductos();
      },
      error: () => {
        this.toastr.error('Error al agregar producto');
      },
    });
  }

  abrirModalEliminar(producto: Producto) {
    this.productoAEliminar = producto;
    this.mostrarModalEliminar = true;
  }

  cerrarModalEliminar() {
    this.productoAEliminar = null;
    this.mostrarModalEliminar = false;
  }

  confirmarEliminarProducto() {
    if (!this.productoAEliminar) return;
    this.productsService.deleteProduct(this.productoAEliminar).subscribe({
      next: () => {
        this.toastr.success('Producto eliminado');
        this.cargarProductos();
        this.cerrarModalEliminar();
      },
      error: () => {
        this.toastr.error('Error al eliminar producto');
        this.cerrarModalEliminar();
      }
    });
  }

  eliminarProducto(producto: Producto) {
    this.abrirModalEliminar(producto);
  }

  abrirModalEditar(producto: Producto) {
    this.productoAEditar = { ...producto };
    this.editarForm.patchValue({
      nombre_producto: producto.nombre_producto,
      precio: producto.precio,
      stock: producto.stock,
      id_categoria: producto.id_categoria,
      descripcion: producto.descripcion,
      imageURL: producto.imageURL
    });
    this.mostrarModalEditar = true;
  }

  cerrarModalEditar() {
    this.mostrarModalEditar = false;
    this.productoAEditar = null;
  }

  guardarEdicion() {
    if (!this.productoAEditar) return;
    const editado = {
      ...this.productoAEditar,
      ...this.editarForm.value,
      imageURL: this.editarForm.value.imageURL || this.productoAEditar.imageURL
    };
    this.productsService.updateProduct(editado).subscribe({
      next: () => {
        this.toastr.success('Producto actualizado');
        this.cargarProductos();
        this.cerrarModalEditar();
      },
      error: () => {
        this.toastr.error('Error al actualizar producto');
      }
    });
  }
}
