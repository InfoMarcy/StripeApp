package com.luxmart.repository;

import org.springframework.data.repository.CrudRepository;

import com.luxmart.model.CartItem;

public interface CartItemRepository extends CrudRepository<CartItem, Long> {

}
