import { Component, OnInit, inject, signal, computed } from '@angular/core';
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

  // Paginacion en Home a 9 productos
  readonly porPagina = 9;
  paginaActual = signal(1);

  totalPaginas = computed(() => {
    const total = this.destacados().length;
    return Math.max(1, Math.ceil(total / this.porPagina));
  });

  productosPaginados = computed(() => {
    const inicio = (this.paginaActual() - 1) * this.porPagina;
    return this.destacados().slice(inicio, inicio + this.porPagina);
  });

  paginasArray = computed(() => {
    const total = this.totalPaginas();
    return Array.from({ length: total }, (_, i) => i + 1);
  });

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 1 && nuevaPagina <= this.totalPaginas()) {
      this.paginaActual.set(nuevaPagina);
      const el = document.getElementById('seccion-productos');
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }
  }

  getCategoryMeta(nombre: string): { icon: string; desc: string; gradient: string } {
    const n = (nombre || '').toLowerCase();
    if (n.includes('monitor')) {
      return { icon: 'tv', desc: 'OLED, Curvos y 240Hz+', gradient: 'from-blue-600/20 to-cyan-600/20 text-cyan-500' };
    }
    if (n.includes('tarjeta') || n.includes('video')) {
      return { icon: 'videogame_asset', desc: 'RTX serie 50 y serie 40', gradient: 'from-emerald-600/20 to-teal-600/20 text-emerald-500' };
    }
    if (n.includes('procesador')) {
      return { icon: 'developer_board', desc: 'Intel Core y AMD Ryzen', gradient: 'from-purple-600/20 to-indigo-600/20 text-purple-500' };
    }
    if (n.includes('placa')) {
      return { icon: 'space_dashboard', desc: 'LGA1851 y AM5 WiFi 7', gradient: 'from-blue-600/20 to-indigo-600/20 text-blue-500' };
    }
    if (n.includes('memoria') || n.includes('almacenamiento')) {
      return { icon: 'storage', desc: 'SSDs NVMe M.2 y RAM DDR5', gradient: 'from-pink-600/20 to-rose-600/20 text-pink-500' };
    }
    if (n.includes('fuente') || n.includes('refrigeraci')) {
      return { icon: 'mode_fan', desc: 'AIOs 360mm y Fuentes Gold', gradient: 'from-blue-600/20 to-sky-600/20 text-sky-500' };
    }
    return { icon: 'sports_esports', desc: 'Headsets, Teclados y Mouse', gradient: 'from-red-600/20 to-rose-600/20 text-red-500' };
  }

  heroSlides = [
    {
      title: 'Equípate con lo Mejor del Gaming',
      desc: 'Rendimiento sin límites para tus partidas más competitivas y componentes de última generación.',
      badge: 'Temporada Gaming 2026',
      gradient: 'from-blue-950/80 via-slate-950 to-indigo-950/80',
      icon: 'memory',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero1.webp'
    },
    {
      title: 'Tarjetas Gráficas de Última Generación',
      desc: 'Arquitectura RTX, Ray Tracing cinematográfico, DLSS acelerado por IA y máxima potencia.',
      badge: 'Nuevos Arribos',
      gradient: 'from-indigo-950/80 via-slate-950 to-cyan-950/80',
      icon: 'developer_board',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero2.webp'
    },
    {
      title: 'Monitores de Alta Tasa de Refresco',
      desc: 'Claridad 4K HDR, paneles Fast IPS de 240Hz y velocidad de respuesta ultra rápida de 1ms.',
      badge: 'Visual Ultra Precisa',
      gradient: 'from-cyan-950/80 via-slate-950 to-blue-950/80',
      icon: 'desktop_windows',
      image: 'https://polvfrjaecmugwyzyuzm.supabase.co/storage/v1/object/public/imagenes/configurtacion_sitio/Hero3.webp'
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
    // 1. Carga inmediata optimista si el administrador acaba de modificar destacados en esta misma máquina
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem('techzone_admin_cache_v2') || localStorage.getItem('techzone_admin_cache');
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed.productos) && parsed.productos.length > 0) {
            const marcadosCache = parsed.productos.filter((p: Producto) => !!p.destacado);
            if (marcadosCache.length > 0) {
              this.destacados.set(marcadosCache.slice(0, 9));
              this.cargando.set(false);
            }
          }
        }
      }
    } catch {}

    // 2. Consulta autoritativa al endpoint de destacados para reflejar fielmente lo marcado en la BD
    this.productoService.getDestacados(true).subscribe({
      next: (prods) => {
        if (prods && prods.length > 0) {
          this.destacados.set(prods.slice(0, 9));
          this.cargando.set(false);
        } else {
          // Fallback al catálogo general si no hay destacados explícitos
          this.productoService.getProductos().subscribe({
            next: (todos) => {
              if (todos && todos.length > 0) {
                const marcados = todos.filter(p => !!p.destacado);
                this.destacados.set((marcados.length > 0 ? marcados : todos).slice(0, 9));
              }
              this.cargando.set(false);
            },
            error: () => this.cargando.set(false)
          });
        }
      },
      error: () => {
        // Fallback hacia catálogo general
        this.productoService.getProductos().subscribe({
          next: (todos) => {
            if (todos && todos.length > 0) {
              const marcados = todos.filter(p => !!p.destacado);
              this.destacados.set((marcados.length > 0 ? marcados : todos).slice(0, 9));
            }
            this.cargando.set(false);
          },
          error: () => this.cargando.set(false)
        });
      }
    });

    this.productoService.getCategorias().subscribe({
      next: (cats) => this.categorias.set(cats)
    });

    this.productoService.getMarcas().subscribe({
      next: (marcas) => this.marcas.set(marcas)
    });
  }
}
