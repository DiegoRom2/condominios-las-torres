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
import mx.unam.aragon.tsp.lastorres.condominios.businessLayer.ResidentesBusinessLayer;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.IdDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDto;
import mx.unam.aragon.tsp.lastorres.condominios.dtos.ResidenteDtoIn;

@RestController
@RequestMapping("/api/v1/residentes")
public class ResidenteController {

    private final ResidentesBusinessLayer residentesBusinessLayer;

    public ResidenteController(ResidentesBusinessLayer residentesBusinessLayer) {
        this.residentesBusinessLayer = residentesBusinessLayer;
    }

    @GetMapping
    public List<ResidenteDto> obtenerTodos() {
        return residentesBusinessLayer.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResidenteDto obtenerPorId(@PathVariable int id) {
        return residentesBusinessLayer.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<IdDto> agregar(@Valid @RequestBody ResidenteDtoIn dtoIn) {
        return ResponseEntity.status(HttpStatus.CREATED).body(residentesBusinessLayer.agregar(dtoIn));
    }

    @PutMapping("/{id}")
    public IdDto actualizar(@PathVariable int id, @Valid @RequestBody ResidenteDtoIn dtoIn) {
        return residentesBusinessLayer.actualizar(id, dtoIn);
    }

    @DeleteMapping("/{id}")
    public IdDto borrar(@PathVariable int id) {
        return residentesBusinessLayer.borrar(id);
    }
}
