package com.example.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Hidden
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<RepresentationModel<?>> home() {
        RepresentationModel<?> model = new RepresentationModel<>();

        // Добавляем ссылки на основные API endpoints
        model.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        model.add(linkTo(methodOn(HomeController.class).apiDocumentation()).withRel("api-docs"));
        model.add(linkTo(methodOn(HomeController.class).swaggerUI()).withRel("swagger-ui"));

        return ResponseEntity.ok(model);
    }

    @GetMapping("/api")
    public ResponseEntity<RepresentationModel<?>> apiDocumentation() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("get-all-users"));
        model.add(linkTo(methodOn(UserController.class).createUser(null)).withRel("create-user"));
        model.add(Link.of("/swagger-ui.html", "swagger-ui"));
        model.add(Link.of("/v3/api-docs", "openapi-spec"));

        return ResponseEntity.ok(model);
    }

    @GetMapping("/swagger-ui")
    public ResponseEntity<RepresentationModel<?>> swaggerUI() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(Link.of("/swagger-ui.html", "swagger-ui"));
        model.add(Link.of("/v3/api-docs", "openapi-spec"));
        model.add(Link.of("/v3/api-docs.yaml", "openapi-spec-yaml"));

        return ResponseEntity.ok(model);
    }
}