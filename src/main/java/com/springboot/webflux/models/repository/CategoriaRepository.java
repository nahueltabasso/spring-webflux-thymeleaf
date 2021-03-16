package com.springboot.webflux.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.models.documents.Categoria;

public interface CategoriaRepository extends ReactiveMongoRepository<Categoria, String> {

}
