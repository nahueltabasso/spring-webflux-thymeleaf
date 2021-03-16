package com.springboot.webflux.models.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.springboot.webflux.models.documents.Producto;

public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

}
