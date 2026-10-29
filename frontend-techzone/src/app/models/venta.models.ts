export interface ItemCheckout {
  idProducto: number;
  cantidad: number;
}

export interface SolicitudCheckout {
  direccionEnvio: string;
  referencia?: string;
  idUbigeo?: number;
  telefono?: string;
  numeroDocumento?: string;
  idMedioPago?: number;
  codigoOperacion?: string;
  celularPago?: string;
  items?: ItemCheckout[];
}

export interface DetalleVentaItem {
  idDetalleVenta: number;
  idProducto: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subTotalVenta: number;
}

export interface PagoItem {
  idPago: number;
  medioPago: string;
  monto: number;
  fecha: string;
  codigoOperacion?: string;
  estadoPago: string;
}

export interface VentaResumen {
  idVenta: number;
  fechaVenta: string;
  estado: string;
  subtotal: number;
  igv: number;
  total: number;
  direccionEnvio: string;
  clienteNombre?: string;
  clienteDocumento?: string;
  idCliente?: number;
  medioPago?: string;
}

export interface VentaCompleta extends VentaResumen {
  detalles: DetalleVentaItem[];
  pagos: PagoItem[];
}
