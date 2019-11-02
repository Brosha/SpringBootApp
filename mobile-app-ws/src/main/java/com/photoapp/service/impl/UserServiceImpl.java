package com.photoapp.service.impl;

import java.util.ArrayList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.photoapp.exceptions.UserServiceException;
import com.photoapp.io.entity.UserEntity;
import com.photoapp.io.repositories.UserRepository;
import com.photoapp.service.UserService;
import com.photoapp.shared.Utils;
import com.photoapp.shared.dto.UserDTO;
import com.photoapp.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public UserDTO createUser(UserDTO userDTO) {
				
		if( userRepository.findByEmail(userDTO.getEmail())!=null) throw new RuntimeException("Record already exists");
		
		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(userDTO, userEntity);
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
		userEntity.setUserId(publicUserId);
		UserEntity storedUserDetails = userRepository.save(userEntity);
		UserDTO returnUserDTO = new UserDTO();
		BeanUtils.copyProperties(storedUserDetails, returnUserDTO);
		
		
		return returnUserDTO;
	}
	
	@Override
	public UserDTO getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null) throw new UsernameNotFoundException(email);
		UserDTO returnUserDTO = new UserDTO();
		BeanUtils.copyProperties(userEntity, returnUserDTO);
		return returnUserDTO;
	}
	

	@Override
	public UserDTO getUserByUserId(String userId) {
		UserDTO userDTO = new UserDTO();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null) throw new UsernameNotFoundException(userId);
		BeanUtils.copyProperties(userEntity, userDTO);
		
		return userDTO;
	}

	@Override
	public UserDTO updateUser(String userId, UserDTO userDTO) {
		
		UserDTO returnValue = new UserDTO();
		
		UserEntity userEntity = userRepository.findByUserId(userId);		
		if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userEntity.setFirstName(userDTO.getFirstName());
		userEntity.setLastName(userDTO.getLastName());
		UserEntity updatedValue =userRepository.save(userEntity);
		BeanUtils.copyProperties(updatedValue, returnValue);
		
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null) throw new UsernameNotFoundException(email);
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);
		
	}

}
