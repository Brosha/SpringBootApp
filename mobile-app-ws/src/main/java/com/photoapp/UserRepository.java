package com.photoapp;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.photoapp.io.entity.UserEntity;
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
	
	UserEntity findUserByEmail(String email);
	
	
}
