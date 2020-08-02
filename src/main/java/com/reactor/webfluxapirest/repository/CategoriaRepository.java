package com.reactor.webfluxapirest.repository;


import com.reactor.webfluxapirest.models.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {

    Mono<Categoria> findByNombre(String nombre);
}
