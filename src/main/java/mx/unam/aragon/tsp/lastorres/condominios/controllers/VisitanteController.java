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
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.VisitantesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.VisitanteDtoIn;

@RestController
@RequestMapping("/api/v1/visitantes")
public class VisitanteController {

    private final VisitantesBusinessLayer visitantesBusinessLayer;

    public VisitanteController(VisitantesBusinessLayer visitantesBusinessLayer) {
        this.visitantesBusinessLayer = visitantesBusinessLayer;
    }

    @GetMapping
    public List<VisitanteDto> obtenerTodos() {
        return visitantesBusinessLayer.obtenerTodos();
    }

    @GetMapping("/{id}")
    public VisitanteDto obtenerPorId(@PathVariable int id) {
        return visitantesBusinessLayer.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<IdDto> agregar(@Valid @RequestBody VisitanteDtoIn dtoIn) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitantesBusinessLayer.agregar(dtoIn));
    }

    @PutMapping("/{id}")
    public IdDto actualizar(@PathVariable int id, @Valid @RequestBody VisitanteDtoIn dtoIn) {
        return visitantesBusinessLayer.actualizar(id, dtoIn);
    }

    @DeleteMapping("/{id}")
    public IdDto borrar(@PathVariable int id) {
        return visitantesBusinessLayer.borrar(id);
    }
}
