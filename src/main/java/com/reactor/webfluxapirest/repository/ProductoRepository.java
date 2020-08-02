package com.reactor.webfluxapirest.repository;


import com.reactor.webfluxapirest.models.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

    Mono<Producto> findByNombre(String nombre);

    @Query("{'nombre' :?0}")
    Mono<Producto> queryByNombre(String nombre);
}
