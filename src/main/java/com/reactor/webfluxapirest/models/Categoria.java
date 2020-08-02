package com.reactor.webfluxapirest.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Document(collection = "categorias")
@NoArgsConstructor
public class Categoria {

    @Id
    @NotEmpty
    private String id;
    private String nombre;

    public Categoria(String nombre) {
        this.nombre = nombre;
    }
}
