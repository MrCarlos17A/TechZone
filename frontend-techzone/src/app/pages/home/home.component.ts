import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { Categoria, Marca, Producto } from '../../models/producto.models';
import { ProductCardComponent } from '../../components/product-card/product-card.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardComponent],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private readonly productoService = inject(ProductoService);

  destacados = signal<Producto[]>([]);
  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);
  cargando = signal(true);

  heroSlides = [
    {
      title: 'Equípate con lo Mejor del Gaming',
      desc: 'Rendimiento sin límites para tus partidas más competitivas y componentes de última generación.',
      badge: 'Temporada Gaming 2026',
      gradient: 'from-blue-950/80 via-slate-950 to-indigo-950/80',
      icon: 'memory',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero1.png'
    },
    {
      title: 'Tarjetas Gráficas de Última Generación',
      desc: 'Arquitectura RTX, Ray Tracing cinematográfico, DLSS acelerado por IA y máxima potencia.',
      badge: 'Nuevos Arribos',
      gradient: 'from-indigo-950/80 via-slate-950 to-cyan-950/80',
      icon: 'developer_board',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero2.png'
    },
    {
      title: 'Monitores de Alta Tasa de Refresco',
      desc: 'Claridad 4K HDR, paneles Fast IPS de 240Hz y velocidad de respuesta ultra rápida de 1ms.',
      badge: 'Visual Ultra Precisa',
      gradient: 'from-cyan-950/80 via-slate-950 to-blue-950/80',
      icon: 'desktop_windows',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero3.png'
    }
  ];
  slideActivo = signal(0);

  ngOnInit(): void {
    this.cargarDatos();
    setInterval(() => {
      this.slideActivo.update(idx => (idx + 1) % this.heroSlides.length);
    }, 5000);
  }

  cargarDatos(): void {
    this.productoService.getDestacados().subscribe({
      next: (prods) => {
        this.destacados.set(prods);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });

    this.productoService.getCategorias().subscribe({
      next: (cats) => this.categorias.set(cats)
    });

    this.productoService.getMarcas().subscribe({
      next: (marcas) => this.marcas.set(marcas)
    });
  }
}
