package com.reactor.webfluxapirest;

import com.reactor.webfluxapirest.handlers.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
        return route(GET("/api/v2/productos"), handler::list)
                .andRoute(GET("/api/v2/productos/{id}"), handler::show)
                .andRoute(POST("/api/v2/productos"), handler::create)
                .andRoute(PUT("/api/v2/productos/{id}"), handler::update)
                .andRoute(DELETE("/api/v2/productos/{id}"), handler::delete)
                .andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
                .andRoute(POST("/api/v2/productos/create-with-photo"), handler::createWithPhoto);
    }

}
