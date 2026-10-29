import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../services/producto.service';
import { Categoria, Marca, Producto } from '../../models/producto.models';
import { ProductCardComponent } from '../../components/product-card/product-card.component';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCardComponent],
  templateUrl: './productos.component.html'
})
export class ProductosComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  productos = signal<Producto[]>([]);
  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);
  cargando = signal(true);

  busqueda = signal('');
  categoriaSeleccionada = signal<number | null>(null);
  marcaSeleccionada = signal<number | null>(null);
  ordenSeleccionado = signal('defecto');

  ngOnInit(): void {
    this.productoService.getCategorias().subscribe(cats => this.categorias.set(cats));
    this.productoService.getMarcas().subscribe(m => this.marcas.set(m));

    this.route.queryParams.subscribe(params => {
      if (params['q']) this.busqueda.set(params['q']);
      if (params['categoria']) this.categoriaSeleccionada.set(+params['categoria']);
      if (params['marca']) this.marcaSeleccionada.set(+params['marca']);
      this.cargarProductos();
    });
  }

  cargarProductos(): void {
    this.cargando.set(true);
    const filtros: any = {};
    if (this.busqueda()) filtros.q = this.busqueda();
    if (this.categoriaSeleccionada()) filtros.categoria = this.categoriaSeleccionada()!;
    if (this.marcaSeleccionada()) filtros.marca = this.marcaSeleccionada()!;

    this.productoService.getProductos(filtros).subscribe({
      next: (data) => {
        let lista = [...data];
        if (this.ordenSeleccionado() === 'precio-asc') {
          lista.sort((a, b) => a.precioVenta - b.precioVenta);
        } else if (this.ordenSeleccionado() === 'precio-desc') {
          lista.sort((a, b) => b.precioVenta - a.precioVenta);
        }
        this.productos.set(lista);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  filtrar(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        q: this.busqueda() || null,
        categoria: this.categoriaSeleccionada() || null,
        marca: this.marcaSeleccionada() || null
      },
      queryParamsHandling: 'merge'
    });
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoriaSeleccionada.set(null);
    this.marcaSeleccionada.set(null);
    this.ordenSeleccionado.set('defecto');
    this.router.navigate(['/productos']);
  }
}
