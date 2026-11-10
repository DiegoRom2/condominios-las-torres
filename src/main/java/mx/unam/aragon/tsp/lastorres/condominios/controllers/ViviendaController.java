package mx.unam.aragon.tsp.lastorres.condominios.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ViviendasBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ViviendaDtoIn;

@RestController
@RequestMapping("/api/v1/viviendas")
public class ViviendaController {

    private final ViviendasBusinessLayer viviendasBusinessLayer;

    public ViviendaController(ViviendasBusinessLayer viviendasBusinessLayer) {
        this.viviendasBusinessLayer = viviendasBusinessLayer;
    }

    @GetMapping
    public List<ViviendaDto> obtenerTodas() {
        return viviendasBusinessLayer.obtenerTodas();
    }

    @GetMapping("/{id}")
    public ViviendaDto obtenerPorId(@PathVariable int id) {
        return viviendasBusinessLayer.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<IdDto> agregar(@Valid @RequestBody ViviendaDtoIn dtoIn) {
        return ResponseEntity.status(HttpStatus.CREATED).body(viviendasBusinessLayer.agregar(dtoIn));
    }

    @PutMapping("/{id}")
    public IdDto actualizar(@PathVariable int id, @Valid @RequestBody ViviendaDtoIn dtoIn) {
        return viviendasBusinessLayer.actualizar(id, dtoIn);
    }

    @DeleteMapping("/{id}")
    public IdDto borrar(@PathVariable int id) {
        return viviendasBusinessLayer.borrar(id);
    }
}
