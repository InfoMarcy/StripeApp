package com.luxmart.repository;

import org.springframework.data.repository.CrudRepository;

import com.luxmart.model.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

}
