import { Injectable } from '@angular/core';

export interface VentaExportData {
  idVenta: number;
  tipoComprobante: string;
  fechaVenta: string;
  clienteNombre: string;
  clienteDocumento: string;
  clienteEmail?: string;
  clienteTelefono?: string;
  direccion?: string;
  total: number;
  subtotal: number;
  igv: number;
  estado: string;
  medioPago: string;
  detalles: {
    idDetalleVenta: number;
    idProducto: number;
    codigoSku: string;
    nombreProducto: string;
    categoria: string;
    marca: string;
    cantidad: number;
    precioUnitario: number;
    subtotal: number;
  }[];
}

export interface FiltrosExportInfo {
  periodo: string;
  mes?: string;
  anio?: string;
  estado: string;
  categoria?: string;
  marca?: string;
  busqueda?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExcelExportService {

  exportarBalanceGerencial(
    ventas: VentaExportData[],
    filtros: FiltrosExportInfo,
    nombreArchivo: string = 'TechZone_Balance_Gerencial'
  ): void {
    const xmlContent = this.generarWorkbookXml(ventas, filtros);
    const blob = new Blob([xmlContent], { type: 'application/vnd.ms-excel;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const timestamp = new Date().toISOString().slice(0, 10);
    a.download = `${nombreArchivo}_${timestamp}.xls`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }


  generarBarraVisual(porcentaje: number, longitud: number = 14): string {
    const pct = Math.max(0, Math.min(100, Math.round(porcentaje || 0)));
    const llenos = Math.round((pct / 100) * longitud);
    const vacios = longitud - llenos;
    return '█'.repeat(llenos) + '░'.repeat(vacios) + ' ' + pct + '%';
  }
  private escaparXml(str: any): string {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&apos;');
  }

  private generarWorkbookXml(ventas: VentaExportData[], filtros: FiltrosExportInfo): string {
    // 1. Cálculos de balances y métricas clave
    const totalVentas = ventas.reduce((acc, v) => acc + (v.estado !== 'CANCELADA' ? v.total : 0), 0);
    const totalSubtotal = ventas.reduce((acc, v) => acc + (v.estado !== 'CANCELADA' ? v.subtotal : 0), 0);
    const totalIgv = ventas.reduce((acc, v) => acc + (v.estado !== 'CANCELADA' ? v.igv : 0), 0);
    const totalPedidosValidos = ventas.filter(v => v.estado !== 'CANCELADA').length;
    const totalPedidosCancelados = ventas.filter(v => v.estado === 'CANCELADA').length;
    const totalPedidosGeneral = ventas.length;
    const ticketPromedio = totalPedidosValidos > 0 ? (totalVentas / totalPedidosValidos) : 0;
    const efectividadEntrega = totalPedidosGeneral > 0 ? ((totalPedidosValidos / totalPedidosGeneral) * 100) : 0;

    // Unidades totales facturadas
    let totalUnidadesVendidas = 0;
    ventas.forEach(v => {
      if (v.estado !== 'CANCELADA') {
        v.detalles.forEach(d => { totalUnidadesVendidas += (d.cantidad || 0); });
      }
    });

    // Desglose por Medio de Pago
    const pagosMap = new Map<string, { transacciones: number; monto: number }>();
    ventas.filter(v => v.estado !== 'CANCELADA').forEach(v => {
      const medio = v.medioPago || 'Otros';
      const actual = pagosMap.get(medio) || { transacciones: 0, monto: 0 };
      pagosMap.set(medio, {
        transacciones: actual.transacciones + 1,
        monto: actual.monto + v.total
      });
    });

    // Desglose por Estado
    const estadosMap = new Map<string, { pedidos: number; monto: number }>();
    ventas.forEach(v => {
      const est = v.estado || 'PENDIENTE';
      const actual = estadosMap.get(est) || { pedidos: 0, monto: 0 };
      estadosMap.set(est, {
        pedidos: actual.pedidos + 1,
        monto: actual.monto + v.total
      });
    });

    // Desglose por Categoría
    const categoriasMap = new Map<string, { unidades: number; monto: number }>();
    const marcasMap = new Map<string, { unidades: number; monto: number }>();
    const productosMap = new Map<string, { sku: string; nombre: string; categoria: string; marca: string; unidades: number; monto: number }>();

    ventas.filter(v => v.estado !== 'CANCELADA').forEach(v => {
      v.detalles.forEach(d => {
        // Categoria
        const cat = d.categoria || 'Sin Categoría';
        const cAct = categoriasMap.get(cat) || { unidades: 0, monto: 0 };
        categoriasMap.set(cat, { unidades: cAct.unidades + d.cantidad, monto: cAct.monto + d.subtotal });

        // Marca
        const mrc = d.marca || 'Sin Marca';
        const mAct = marcasMap.get(mrc) || { unidades: 0, monto: 0 };
        marcasMap.set(mrc, { unidades: mAct.unidades + d.cantidad, monto: mAct.monto + d.subtotal });

        // Producto
        const pKey = d.idProducto ? String(d.idProducto) : d.nombreProducto;
        const pAct = productosMap.get(pKey) || {
          sku: d.codigoSku || '-',
          nombre: d.nombreProducto,
          categoria: cat,
          marca: mrc,
          unidades: 0,
          monto: 0
        };
        productosMap.set(pKey, {
          ...pAct,
          unidades: pAct.unidades + d.cantidad,
          monto: pAct.monto + d.subtotal
        });
      });
    });

    // Tendencias Temporales (por fecha de venta)
    const fechasMap = new Map<string, { pedidos: number; monto: number; unidades: number }>();
    ventas.filter(v => v.estado !== 'CANCELADA').forEach(v => {
      const fechaCorta = v.fechaVenta ? v.fechaVenta.slice(0, 10) : 'Sin Fecha';
      const fAct = fechasMap.get(fechaCorta) || { pedidos: 0, monto: 0, unidades: 0 };
      const unidadesEnVenta = v.detalles.reduce((sum, d) => sum + d.cantidad, 0);
      fechasMap.set(fechaCorta, {
        pedidos: fAct.pedidos + 1,
        monto: fAct.monto + v.total,
        unidades: fAct.unidades + unidadesEnVenta
      });
    });

    return `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <DocumentProperties xmlns="urn:schemas-microsoft-com:office:office">
  <Author>TechZone Gaming Store</Author>
  <LastAuthor>TechZone ERP</LastAuthor>
  <Created>${new Date().toISOString()}</Created>
  <Company>TechZone Gaming Store S.A.C.</Company>
 </DocumentProperties>
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="11" ss:Color="#1E293B"/>
   <Interior/>
   <NumberFormat/>
   <Protection/>
  </Style>
  <!-- Estilos Corporativos -->
  <Style ss:ID="HeaderTitle">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="16" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#0F172A" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="HeaderSubtitle">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="11" ss:Color="#E2E8F0" ss:Italic="1"/>
   <Interior ss:Color="#1E293B" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="SectionHeader">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="12" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#2563EB" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="TableHeader">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Bold="1" ss:Color="#FFFFFF"/>
   <Interior ss:Color="#334155" ss:Pattern="Solid"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CBD5E1"/>
   </Borders>
  </Style>
  <Style ss:ID="CellNormal">
   <Alignment ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="CellBold">
   <Alignment ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Color="#0F172A"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="CellCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="CellCurrency">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="&quot;S/ &quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="CellCurrencyBold">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Color="#0F172A"/>
   <NumberFormat ss:Format="&quot;S/ &quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CBD5E1"/>
   </Borders>
  </Style>
  <Style ss:ID="CellPercent">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="0.0%"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="CellInteger">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <NumberFormat ss:Format="#,##0"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#E2E8F0"/>
   </Borders>
  </Style>
  <Style ss:ID="TotalRow">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Size="11" ss:Color="#0F172A"/>
   <Interior ss:Color="#F1F5F9" ss:Pattern="Solid"/>
   <NumberFormat ss:Format="&quot;S/ &quot;#,##0.00"/>
   <Borders>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#64748B"/>
    <Border ss:Position="Bottom" ss:LineStyle="Double" ss:Weight="3" ss:Color="#0F172A"/>
   </Borders>
  </Style>
  <Style ss:ID="BadgeA">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Color="#065F46"/>
   <Interior ss:Color="#D1FAE5" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="BadgeB">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Color="#1E40AF"/>
   <Interior ss:Color="#DBEAFE" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="BadgeC">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Bold="1" ss:Color="#9A3412"/>
   <Interior ss:Color="#FFEDD5" ss:Pattern="Solid"/>
  </Style>
    <Style ss:ID="StyleVisualBar">
    <Font ss:FontName="Consolas" x:Family="Modern" ss:Size="9" ss:Color="#0369a1" ss:Bold="1"/>
    <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
    <Borders>
     <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#e2e8f0"/>
     <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#e2e8f0"/>
     <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#e2e8f0"/>
    </Borders>
   </Style>
   <Style ss:ID="StyleKPI">
    <Font ss:FontName="Segoe UI" ss:Size="11" ss:Color="#0f172a" ss:Bold="1"/>
    <Interior ss:Color="#f1f5f9" ss:Pattern="Solid"/>
    <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   </Style>
  </Styles>

 ${this.generarHojaDashboard(ventas, filtros, totalVentas, totalSubtotal, totalIgv, totalUnidadesVendidas, ticketPromedio, efectividadEntrega, totalPedidosValidos, totalPedidosCancelados, pagosMap, estadosMap)}
 ${this.generarHojaCategoriasYMarcas(categoriasMap, marcasMap, totalVentas)}
 ${this.generarHojaMatrizPareto(productosMap, totalVentas)}
 ${this.generarHojaTendencias(fechasMap)}
 ${this.generarHojaTransaccional(ventas)}

</Workbook>`;
  }

  // ==========================================
  // HOJA 1: DASHBOARD Y BALANCES GERENCIALES
  // ==========================================
  private generarHojaDashboard(
    ventas: VentaExportData[],
    filtros: FiltrosExportInfo,
    totalVentas: number,
    totalSubtotal: number,
    totalIgv: number,
    totalUnidades: number,
    ticketPromedio: number,
    efectividadEntrega: number,
    pedidosValidos: number,
    pedidosCancelados: number,
    pagosMap: Map<string, { transacciones: number; monto: number }>,
    estadosMap: Map<string, { pedidos: number; monto: number }>
  ): string {
    const fechaEmision = new Date().toLocaleString('es-PE');

    let rowsPagos = '';
    pagosMap.forEach((val, key) => {
      const share = totalVentas > 0 ? (val.monto / totalVentas) : 0;
      const barra = this.generarBarraVisual(share * 100);
      rowsPagos += `
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(key)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${val.transacciones}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${val.monto.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${share.toFixed(4)}</Data></Cell>
    <Cell ss:StyleID="StyleVisualBar"><Data ss:Type="String">${barra}</Data></Cell>
   </Row>`;
    });

    let rowsEstados = '';
    estadosMap.forEach((val, key) => {
      rowsEstados += `
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(key)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${val.pedidos}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${val.monto.toFixed(2)}</Data></Cell>
   </Row>`;
    });

    return `
 <Worksheet ss:Name="Dashboard y Balances">
  <Table ss:DefaultColumnWidth="120">
   <Column ss:Width="200"/>
   <Column ss:Width="130"/>
   <Column ss:Width="130"/>
   <Column ss:Width="110"/>
   <Column ss:Width="170"/>

   <!-- Encabezado Corporativo -->
   <Row ss:Height="30">
    <Cell ss:MergeAcross="3" ss:StyleID="HeaderTitle"><Data ss:Type="String"> TECHZONE GAMING STORE S.A.C. - BALANCE GERENCIAL DE VENTAS</Data></Cell>
   </Row>
   <Row ss:Height="20">
    <Cell ss:MergeAcross="3" ss:StyleID="HeaderSubtitle"><Data ss:Type="String"> RUC: 20608945123 | Emisión: ${fechaEmision} | Período: ${this.escaparXml(filtros.periodo.toUpperCase())} ${filtros.mes ? '| Mes: ' + filtros.mes : ''} ${filtros.anio ? '| Año: ' + filtros.anio : ''}</Data></Cell>
   </Row>
   <Row ss:Height="10"/>

   <!-- Cuadro de Balances Principales -->
   <Row ss:Height="22">
    <Cell ss:MergeAcross="3" ss:StyleID="SectionHeader"><Data ss:Type="String"> 1. CUADRO DE RESULTADOS Y BALANCES FINANCIEROS</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CONCEPTO / INDICADOR</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">VALOR REGISTRADO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">OBSERVACIÓN / DESGLOSE</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CUMPLIMIENTO</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellBold"><Data ss:Type="String">Facturación Total Bruta (S/)</Data></Cell>
    <Cell ss:StyleID="CellCurrencyBold"><Data ss:Type="Number">${totalVentas.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Total recaudado según filtros</Data></Cell>
    <Cell ss:StyleID="BadgeA"><Data ss:Type="String">100%</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Venta Neta (Base Imponible)</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${totalSubtotal.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Ingreso neto antes de impuestos</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${(totalVentas > 0 ? totalSubtotal / totalVentas : 0).toFixed(4)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">I.G.V. Recaudado (18% SUNAT)</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${totalIgv.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Impuesto fiscal a liquidar</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${(totalVentas > 0 ? totalIgv / totalVentas : 0).toFixed(4)}</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Ticket Promedio por Pedido</Data></Cell>
    <Cell ss:StyleID="CellCurrencyBold"><Data ss:Type="Number">${ticketPromedio.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Facturación / Pedidos Válidos</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">-</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Unidades Facturadas en Catálogo</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${totalUnidades}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Artículos vendidos en el período</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">Unidades</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">Tasa de Efectividad Operativa</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${(efectividadEntrega / 100).toFixed(4)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${pedidosValidos} válidos / ${pedidosCancelados} cancelados</Data></Cell>
    <Cell ss:StyleID="${efectividadEntrega >= 80 ? 'BadgeA' : 'BadgeC'}"><Data ss:Type="String">${efectividadEntrega.toFixed(1)}%</Data></Cell>
   </Row>

   <Row ss:Height="15"/>

   <!-- Desglose por Medio de Pago -->
   <Row ss:Height="22">
    <Cell ss:MergeAcross="3" ss:StyleID="SectionHeader"><Data ss:Type="String"> 2. RENDIMIENTO POR CANAL Y MEDIO DE PAGO</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MEDIO DE PAGO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">TRANSACCIONES</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MONTO TOTAL (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">% PARTICIPACIÓN</Data></Cell>
   </Row>
   ${rowsPagos}

   <Row ss:Height="15"/>

   <!-- Estado de Pedidos -->
   <Row ss:Height="22">
    <Cell ss:MergeAcross="3" ss:StyleID="SectionHeader"><Data ss:Type="String"> 3. RESUMEN LOGÍSTICO POR ESTADO DE ORDEN</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">ESTADO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CANTIDAD PEDIDOS</Data></Cell>
    <Cell ss:MergeAcross="1" ss:StyleID="TableHeader"><Data ss:Type="String">MONTO ASOCIADO (S/)</Data></Cell>
   </Row>
   ${rowsEstados}
  </Table>
 </Worksheet>`;
  }

  // ==========================================
  // HOJA 2: CATEGORÍAS Y MARCAS
  // ==========================================
  private generarHojaCategoriasYMarcas(
    categoriasMap: Map<string, { unidades: number; monto: number }>,
    marcasMap: Map<string, { unidades: number; monto: number }>,
    totalVentas: number
  ): string {
    let rowsCat = '';
    categoriasMap.forEach((val, cat) => {
      const share = totalVentas > 0 ? (val.monto / totalVentas) : 0;
      const asp = val.unidades > 0 ? (val.monto / val.unidades) : 0;
      rowsCat += `
   <Row>
    <Cell ss:StyleID="CellBold"><Data ss:Type="String">${this.escaparXml(cat)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${val.unidades}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${val.monto.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${share.toFixed(4)}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${asp.toFixed(2)}</Data></Cell>
   </Row>`;
    });

    let rowsMarcas = '';
    marcasMap.forEach((val, mrc) => {
      const share = totalVentas > 0 ? (val.monto / totalVentas) : 0;
      const asp = val.unidades > 0 ? (val.monto / val.unidades) : 0;
      rowsMarcas += `
   <Row>
    <Cell ss:StyleID="CellBold"><Data ss:Type="String">${this.escaparXml(mrc)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${val.unidades}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${val.monto.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${share.toFixed(4)}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${asp.toFixed(2)}</Data></Cell>
   </Row>`;
    });

    return `
 <Worksheet ss:Name="Categorías y Marcas">
  <Table ss:DefaultColumnWidth="120">
   <Column ss:Width="180"/>
   <Column ss:Width="120"/>
   <Column ss:Width="140"/>
   <Column ss:Width="120"/>
   <Column ss:Width="140"/>
   <Column ss:Width="170"/>

   <Row ss:Height="24">
    <Cell ss:MergeAcross="4" ss:StyleID="SectionHeader"><Data ss:Type="String"> RENDIMIENTO COMERCIAL POR LÍNEA Y CATEGORÍA</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CATEGORÍA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">UNIDADES FACTURADAS</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">FACTURACIÓN TOTAL (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">% DE PARTICIPACIÓN</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">PRECIO PROM. (ASP)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">GRÁFICO DE PARTICIPACIÓN</Data></Cell>
   </Row>
   ${rowsCat}

   <Row ss:Height="15"/>

   <Row ss:Height="24">
    <Cell ss:MergeAcross="4" ss:StyleID="SectionHeader"><Data ss:Type="String"> PARTICIPACIÓN DE MARCAS FABRICANTES</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MARCA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">UNIDADES FACTURADAS</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">FACTURACIÓN TOTAL (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">% DE PARTICIPACIÓN</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">PRECIO PROM. (ASP)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">GRÁFICO DE PARTICIPACIÓN</Data></Cell>
   </Row>
   ${rowsMarcas}
  </Table>
 </Worksheet>`;
  }

  // ==========================================
  // HOJA 3: MATRIZ ABC / ANÁLISIS DE PARETO
  // ==========================================
  private generarHojaMatrizPareto(
    productosMap: Map<string, { sku: string; nombre: string; categoria: string; marca: string; unidades: number; monto: number }>,
    totalVentas: number
  ): string {
    const list = Array.from(productosMap.values());
    list.sort((a, b) => b.monto - a.monto);

    let acum = 0;
    let rows = '';

    list.forEach(p => {
      acum += p.monto;
      const pctAcum = totalVentas > 0 ? (acum / totalVentas) : 0;
      let clase = 'C';
      let styleBadge = 'BadgeC';

      if (pctAcum <= 0.80 || list.indexOf(p) === 0) {
        clase = 'A (Estrella)';
        styleBadge = 'BadgeA';
      } else if (pctAcum <= 0.95) {
        clase = 'B (Regular)';
        styleBadge = 'BadgeB';
      } else {
        clase = 'C (Rotación Baja)';
        styleBadge = 'BadgeC';
      }

      rows += `
   <Row>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(p.sku)}</Data></Cell>
    <Cell ss:StyleID="CellBold"><Data ss:Type="String">${this.escaparXml(p.nombre)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(p.categoria)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(p.marca)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${p.unidades}</Data></Cell>
    <Cell ss:StyleID="CellCurrencyBold"><Data ss:Type="Number">${p.monto.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellPercent"><Data ss:Type="Number">${pctAcum.toFixed(4)}</Data></Cell>
    <Cell ss:StyleID="${styleBadge}"><Data ss:Type="String">${clase}</Data></Cell>
    <Cell ss:StyleID="StyleVisualBar"><Data ss:Type="String">${this.generarBarraVisual(pctAcum * 100)}</Data></Cell>
   </Row>`;
    });

    return `
 <Worksheet ss:Name="Matriz ABC Pareto">
  <Table ss:DefaultColumnWidth="120">
   <Column ss:Width="90"/>
   <Column ss:Width="260"/>
   <Column ss:Width="140"/>
   <Column ss:Width="110"/>
   <Column ss:Width="90"/>
   <Column ss:Width="130"/>
   <Column ss:Width="110"/>
   <Column ss:Width="130"/>
   <Column ss:Width="170"/>

   <Row ss:Height="24">
    <Cell ss:MergeAcross="8" ss:StyleID="SectionHeader"><Data ss:Type="String"> ANÁLISIS DE PARETO (REGLA 80/20) - PRODUCTOS CLAVE PARA EL NEGOCIO</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">SKU</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">DESCRIPCIÓN DEL PRODUCTO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CATEGORÍA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MARCA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">UNID.</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">RECAUDACIÓN (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">% ACUMULADO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CLASIFICACIÓN ABC</Data></Cell>
   </Row>
   ${rows}
  </Table>
 </Worksheet>`;
  }

  // ==========================================
  // HOJA 4: TENDENCIAS TEMPORALES
  // ==========================================
  private generarHojaTendencias(
    fechasMap: Map<string, { pedidos: number; monto: number; unidades: number }>
  ): string {
    const list = Array.from(fechasMap.entries());
    list.sort((a, b) => a[0].localeCompare(b[0]));

    let rows = '';
    list.forEach(([fecha, data]) => {
      const aov = data.pedidos > 0 ? (data.monto / data.pedidos) : 0;
      rows += `
   <Row>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${this.escaparXml(fecha)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${data.pedidos}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${data.unidades}</Data></Cell>
    <Cell ss:StyleID="CellCurrencyBold"><Data ss:Type="Number">${data.monto.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${aov.toFixed(2)}</Data></Cell>
   </Row>`;
    });

    return `
 <Worksheet ss:Name="Tendencias Temporales">
  <Table ss:DefaultColumnWidth="130">
   <Column ss:Width="130"/>
   <Column ss:Width="120"/>
   <Column ss:Width="120"/>
   <Column ss:Width="140"/>
   <Column ss:Width="140"/>

   <Row ss:Height="24">
    <Cell ss:MergeAcross="4" ss:StyleID="SectionHeader"><Data ss:Type="String"> EVOLUCIÓN DIARIA Y RITMO DE VENTAS</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">FECHA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">PEDIDOS REALIZADOS</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">UNIDADES FACTURADAS</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">TOTAL DIARIO (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">TICKET DIARIO (S/)</Data></Cell>
   </Row>
   ${rows}
  </Table>
 </Worksheet>`;
  }

  // ==========================================
  // HOJA 5: LIBRO TRANSACCIONAL COMPLETO
  // ==========================================
  private generarHojaTransaccional(ventas: VentaExportData[]): string {
    let rows = '';
    ventas.forEach(v => {
      v.detalles.forEach(d => {
        rows += `
   <Row>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">#${v.idVenta}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${this.escaparXml(v.tipoComprobante)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${this.escaparXml(v.fechaVenta ? v.fechaVenta.slice(0, 19).replace('T', ' ') : '-')}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(v.clienteDocumento || '-')}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(v.clienteNombre || 'Cliente')}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(d.codigoSku || '-')}</Data></Cell>
    <Cell ss:StyleID="CellBold"><Data ss:Type="String">${this.escaparXml(d.nombreProducto)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(d.categoria)}</Data></Cell>
    <Cell ss:StyleID="CellNormal"><Data ss:Type="String">${this.escaparXml(d.marca)}</Data></Cell>
    <Cell ss:StyleID="CellInteger"><Data ss:Type="Number">${d.cantidad}</Data></Cell>
    <Cell ss:StyleID="CellCurrency"><Data ss:Type="Number">${d.precioUnitario.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellCurrencyBold"><Data ss:Type="Number">${d.subtotal.toFixed(2)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${this.escaparXml(v.estado)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${this.escaparXml(v.medioPago || '-')}</Data></Cell>
   </Row>`;
      });
    });

    return `
 <Worksheet ss:Name="Libro Transaccional">
  <Table ss:DefaultColumnWidth="120">
   <Column ss:Width="70"/>
   <Column ss:Width="90"/>
   <Column ss:Width="130"/>
   <Column ss:Width="100"/>
   <Column ss:Width="180"/>
   <Column ss:Width="90"/>
   <Column ss:Width="240"/>
   <Column ss:Width="120"/>
   <Column ss:Width="100"/>
   <Column ss:Width="70"/>
   <Column ss:Width="110"/>
   <Column ss:Width="110"/>
   <Column ss:Width="110"/>
   <Column ss:Width="110"/>

   <Row ss:Height="24">
    <Cell ss:MergeAcross="13" ss:StyleID="SectionHeader"><Data ss:Type="String"> AUDITORÍA Y DETALLE TRANSACCIONAL COMPLETO</Data></Cell>
   </Row>
   <Row>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String"># VENTA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">COMPROBANTE</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">FECHA / HORA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">DOCUMENTO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CLIENTE</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">SKU</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">PRODUCTO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CATEGORÍA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MARCA</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">CANT.</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">P. UNIT. (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">SUBTOTAL (S/)</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">ESTADO</Data></Cell>
    <Cell ss:StyleID="TableHeader"><Data ss:Type="String">MEDIO PAGO</Data></Cell>
   </Row>
   ${rows}
  </Table>
 </Worksheet>`;
  }
}

