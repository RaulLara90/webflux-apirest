package com.reactor.webfluxapirest.services;

import com.reactor.webfluxapirest.models.Categoria;
import com.reactor.webfluxapirest.models.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {

    Flux<Producto> findAll();

    Flux<Producto> findAllUpperCase();

    Flux<Producto> findAllUpperCaseRepeat();

    Mono<Producto> findById(String id);

    Mono<Producto> save(Producto producto);

    Mono<Void> delete(Producto producto);

    Flux<Categoria> findAllCategoria();

    Mono<Categoria> findCategoriaById(String id);

    Mono<Categoria> saveCategoria(Categoria categoria);

    Mono<Producto> findByNombre(String nombre);

    Mono<Categoria> findCategoriaByNombre(String nombre);
}
