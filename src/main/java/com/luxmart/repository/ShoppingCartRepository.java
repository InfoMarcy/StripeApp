package com.luxmart.repository;

import org.springframework.data.repository.CrudRepository;

import com.luxmart.model.ShoppingCart;

public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, Long> {

}
