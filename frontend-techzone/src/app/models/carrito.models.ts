export interface CarritoItem {
  idDetalle: number;
  idProducto: number;
  nombre: string;
  codigoSku?: string;
  precioVenta: number;
  cantidad: number;
  stock: number;
  subtotal: number;
  imagenUrl?: string;
}

export interface CarritoResumen {
  idCarrito: number;
  items: CarritoItem[];
  cantidadItems: number;
  subtotal: number;
  igv: number;
  total: number;
}
