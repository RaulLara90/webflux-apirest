package com.reactor.webfluxapirest;

import com.reactor.webfluxapirest.models.Categoria;
import com.reactor.webfluxapirest.models.Producto;
import com.reactor.webfluxapirest.services.IProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private IProductService service;

    @Test
    void listTest() {
        client.get()
                .uri("/api/v2/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class).hasSize(9);
    }

    @Test
    void listTest2() {
        client.get()
                .uri("/api/v2/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> products = response.getResponseBody();
                    products.forEach(p -> {
                        System.out.println(p.getNombre());
                    });
                    Assertions.assertThat(products.size() > 0).isTrue();
                });
    }

    @Test
    void showTest() {

        Mono<Producto> product = service.findByNombre("TV Panasonic Pantalla LCD");
        client.get()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.block().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");
    }

    @Test
    void showTest2() {

        Mono<Producto> product = service.findByNombre("TV Panasonic Pantalla LCD");
        client.get()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.block().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    Assertions.assertThat(p.getId()).isNotEmpty();
                    Assertions.assertThat(p.getId().length() > 0).isTrue();
                    Assertions.assertThat(p.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
                });
    }

    @Test
    void createTest() {
        Mono<Categoria> category = service.findCategoriaByNombre("Muebles");

        Producto product = new Producto("Mesa", 100.0, category.block());

        client.post()
                .uri("/api/v2/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(product), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Mesa")
                .jsonPath("$.categoria.nombre").isEqualTo("Muebles");
    }

    @Test
    void createTest2() {
        Mono<Categoria> category = service.findCategoriaByNombre("Muebles");

        Producto product = new Producto("Mesa", 100.0, category.block());

        client.post()
                .uri("/api/v2/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(product), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    Assertions.assertThat(p.getId()).isNotEmpty();
                    Assertions.assertThat(p.getId().length() > 0).isTrue();
                    Assertions.assertThat(p.getNombre()).isEqualTo("Mesa");
                    Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
                });
    }

    @Test
    void updateTest() {

        Producto product = service.findByNombre("Sony Camara HD Digital").block();
        Categoria category = service.findCategoriaByNombre("Electrónica").block();

        Producto productEdit = new Producto("Asus Notebook", 589.09, category);

        client.put()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productEdit), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Asus Notebook")
                .jsonPath("$.categoria.nombre").isEqualTo("Electrónica");
    }

    @Test
    void updateTest2() {

        Producto product = service.findByNombre("Sony Camara HD Digital").block();
        Categoria category = service.findCategoriaByNombre("Electrónica").block();

        Producto productEdit = new Producto("Asus Notebook", 589.09, category);

        client.put()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productEdit), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();
                    Assertions.assertThat(p.getId()).isNotEmpty();
                    Assertions.assertThat(p.getId().length() > 0).isTrue();
                    Assertions.assertThat(p.getNombre()).isEqualTo("Asus Notebook");
                    Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Electrónica");
                });
    }

    @Test
    void deleteTest2() {

        Producto product = service.findByNombre("Sony Notebook").block();

        client.delete()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.getId()))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        client.get()
                .uri("/api/v2/productos/{id}", Collections.singletonMap("id", product.getId()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .isEmpty();
    }

}
