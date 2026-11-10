package mx.unam.aragon.tsp.lastorres.condominios.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.AccesosBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.AccesoDtoIn;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;

/**
 * Caseta de vigilancia: registro de entradas y salidas.
 * Los accesos no se editan ni se borran, porque son la bitácora del condominio.
 */
@RestController
@RequestMapping("/api/v1/accesos")
public class AccesoController {

    private final AccesosBusinessLayer accesosBusinessLayer;

    public AccesoController(AccesosBusinessLayer accesosBusinessLayer) {
        this.accesosBusinessLayer = accesosBusinessLayer;
    }

    @GetMapping
    public List<AccesoDto> obtenerTodos() {
        return accesosBusinessLayer.obtenerTodos();
    }

    /** ¿Quién está dentro del condominio en este momento? */
    @GetMapping("/dentro")
    public List<AccesoDto> obtenerDentro() {
        return accesosBusinessLayer.obtenerDentro();
    }

    @GetMapping("/{id}")
    public AccesoDto obtenerPorId(@PathVariable int id) {
        return accesosBusinessLayer.obtenerPorId(id);
    }

    @PostMapping("/entrada")
    public ResponseEntity<IdDto> registrarEntrada(@Valid @RequestBody AccesoDtoIn dtoIn) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accesosBusinessLayer.registrarEntrada(dtoIn));
    }

    @PutMapping("/{id}/salida")
    public IdDto registrarSalida(@PathVariable int id) {
        return accesosBusinessLayer.registrarSalida(id);
    }
}
