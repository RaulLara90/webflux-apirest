package com.reactor.webfluxapirest;

import com.reactor.webfluxapirest.models.Categoria;
import com.reactor.webfluxapirest.models.Producto;
import com.reactor.webfluxapirest.services.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
@EnableEurekaClient
public class WebfluxApirestApplication implements CommandLineRunner {

    @Autowired
    private IProductService productoService;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(WebfluxApirestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApirestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria elecrtronica = new Categoria("Electrónica");
        Categoria deporte = new Categoria("Deporte");
        Categoria informatica = new Categoria("Informática");
        Categoria mueble = new Categoria("Muebles");

        Flux.just(elecrtronica, deporte, informatica, mueble)
                .flatMap(productoService::saveCategoria)
                .doOnNext(c -> log.info("Categoria creada: " + c.getNombre() + ", Id: " + c.getId()))
                .thenMany(Flux.just(
                        new Producto("TV Panasonic Pantalla LCD", 456.89, elecrtronica),
                        new Producto("Sony Camara HD Digital", 177.89, elecrtronica),
                        new Producto("Apple Ipod", 46.89, elecrtronica),
                        new Producto("Sony Notebook", 846.89, informatica),
                        new Producto("Hewlett Packard Multifuncional", 200.89, informatica),
                        new Producto("Bianchi Bicicleta", 70.89, deporte),
                        new Producto("HP Notebook Omen 17", 2500.89, informatica),
                        new Producto("Mica Cómoda 5 Cajones", 150.89, mueble),
                        new Producto("TV Sony Bravia OLED 4k Ultra HD", 2255.89, elecrtronica)
                )
                        .flatMap(producto -> {
                            producto.setCreateAt(new Date());
                            return productoService.save(producto);
                        }))
                .subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
    }
}
