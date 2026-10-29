export interface Categoria {
  idCategoria: number;
  nombre: string;
  vigente?: boolean;
}

export interface Marca {
  idMarca: number;
  nombre: string;
  logoUrl?: string;
  vigente?: boolean;
}

export interface ProductoImagen {
  idProductoImagen: number;
  urlImagen: string;
  esPrincipal?: boolean;
  idProducto?: number;
  nombreProducto?: string;
  skuProducto?: string;
  categoriaProducto?: string;
  marcaProducto?: string;
  precioProducto?: number;
}

export interface Producto {
  idProducto: number;
  nombre: string;
  descripcion?: string;
  precioVenta: number;
  stock: number;
  codigoSku?: string;
  caracteristicas?: string;
  vigente?: boolean;
  categoria?: Categoria;
  marca?: Marca;
  imagenes?: ProductoImagen[];
}
