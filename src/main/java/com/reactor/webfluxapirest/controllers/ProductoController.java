package com.reactor.webfluxapirest.controllers;

import com.reactor.webfluxapirest.models.Producto;
import com.reactor.webfluxapirest.services.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private IProductService service;

    @Value("${resources}")
    private String resources;

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> list() {
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> show(@PathVariable String id) {
        return service.findById(id)
                .map(p -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Producto> monoProducto) {

        Map<String, Object> response = new HashMap<>();
        return monoProducto.flatMap(producto -> {
            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }
            return service.save(producto).map(p -> {
                response.put("producto", p);
                response.put("mensaje", "Producto creado con éxito");
                response.put("timestamp", new Date());
                return ResponseEntity
                        .created(URI.create("/api/v1/productos".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            });
        })
                .onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
                        .flatMap(e -> Mono.just(e.getFieldErrors()))
                        .flatMapMany(Flux::fromIterable)
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> {
                            response.put("errors", list);
                            response.put("timestamp", new Date());
                            response.put("status", HttpStatus.BAD_REQUEST.value());
                            return Mono.just(ResponseEntity.badRequest().body(response));
                        })
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> update(@PathVariable String id, @RequestBody Producto producto) {
        return service.findById(id)
                .flatMap(p -> {
                    p.setNombre(producto.getNombre());
                    p.setPrecio(producto.getPrecio());
                    p.setCategoria(producto.getCategoria());
                    return service.save(p);
                })
                .map(p -> ResponseEntity
                        .created(URI.create("/api/v1/productos".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.findById(id)
                .flatMap(p -> service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return service.findById(id).flatMap(p -> {
            p.setFoto(UUID.randomUUID() + "-" + file.filename()
                    .replace(StringUtils.SPACE, StringUtils.EMPTY)
                    .replace(":", StringUtils.EMPTY)
                    .replace("\\", StringUtils.EMPTY));
            return file.transferTo(new File(resources + p.getFoto())).then(service.save(p));
        }).map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> createWithPhoto(Producto producto, @RequestPart FilePart file) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }
        producto.setFoto(UUID.randomUUID() + "-" + file.filename()
                .replace(StringUtils.SPACE, StringUtils.EMPTY)
                .replace(":", StringUtils.EMPTY)
                .replace("\\", StringUtils.EMPTY));
        return file.transferTo(new File(resources + producto.getFoto())).then(service.save(producto))
                .map(p -> ResponseEntity
                        .created(URI.create("/api/v1/productos".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p));
    }

}
