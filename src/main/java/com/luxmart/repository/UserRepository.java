package com.luxmart.repository;

import org.springframework.data.repository.CrudRepository;

import com.luxmart.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
