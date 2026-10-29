package com.tienda.backend_techzone.Controller.api;

import com.tienda.backend_techzone.Model.Carrito;
import com.tienda.backend_techzone.Repository.CarritoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/carritos")
public class CarritoAdminApiController {

    private final CarritoRepository carritoRepository;

    public CarritoAdminApiController(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Carrito>> listar() {
        return ResponseEntity.ok(carritoRepository.findAllConCliente());
    }
}
