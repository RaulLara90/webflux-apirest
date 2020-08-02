package com.reactor.webfluxapirest.handlers;

import com.reactor.webfluxapirest.models.Categoria;
import com.reactor.webfluxapirest.models.Producto;
import com.reactor.webfluxapirest.services.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductoHandler {

    @Autowired
    private IProductService service;
    @Autowired
    private Validator validator;

    @Value("${resources}")
    private String resources;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> show(ServerRequest request) {
        return service.findById(request.pathVariable("id"))
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {

        Mono<Producto> product = request.bodyToMono(Producto.class);

        return product.flatMap(p -> {

            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return service.save(p).flatMap(pDB -> ServerResponse.created(URI.create("/api/v2/productos/".concat(pDB.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(pDB)));
            }
        });
    }

    public Mono<ServerResponse> update(ServerRequest request) {

        String id = request.pathVariable("id");
        Mono<Producto> product = request.bodyToMono(Producto.class);

        Mono<Producto> productDB = service.findById(id);

        return productDB.zipWith(product, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategoria(req.getCategoria());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }


    public Mono<ServerResponse> delete(ServerRequest request) {

        String id = request.pathVariable("id");
        Mono<Producto> productDB = service.findById(id);

        return productDB.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> upload(ServerRequest request) {

        String id = request.pathVariable("id");
        return request.multipartData()
                .map(m -> m.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> service.findById(id).flatMap(p -> {
                    p.setFoto(UUID.randomUUID() + "-" + file.filename()
                            .replace(StringUtils.SPACE, StringUtils.EMPTY)
                            .replace(":", StringUtils.EMPTY)
                            .replace("\\", StringUtils.EMPTY));
                    return file.transferTo(new File(resources + p.getFoto())).then(service.save(p));
                })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createWithPhoto(ServerRequest request) {

        Mono<Producto> product = request.multipartData().map(m -> {
            FormFieldPart nombre = (FormFieldPart) m.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) m.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) m.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) m.toSingleValueMap().get("categoria.nombre");

            Categoria c = new Categoria(categoriaNombre.value());
            c.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), c);
        });

        return request.multipartData()
                .map(m -> m.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> product
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID() + "-" + file.filename()
                                    .replace(StringUtils.SPACE, StringUtils.EMPTY)
                                    .replace(":", StringUtils.EMPTY)
                                    .replace("\\", StringUtils.EMPTY));
                            p.setCreateAt(new Date());
                            return file.transferTo(new File(resources + p.getFoto())).then(service.save(p));
                        })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)));
    }
}
