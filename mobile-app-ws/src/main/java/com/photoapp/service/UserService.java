package com.photoapp.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.photoapp.shared.dto.UserDTO;

public interface UserService extends UserDetailsService {
	UserDTO createUser(UserDTO userDTO);
	
	UserDTO getUser(String email);

	UserDTO getUserByUserId(String userId);

	UserDTO updateUser(String id, UserDTO userDTO);
	
}
