package com.photoapp.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.photoapp.exceptions.UserServiceException;
import com.photoapp.io.entity.AddressEntity;
import com.photoapp.io.entity.UserEntity;
import com.photoapp.io.repository.PasswordResetTokenRepository;
import com.photoapp.io.repository.UserRepository;
import com.photoapp.shared.AmazonSES;
import com.photoapp.shared.Utils;
import com.photoapp.shared.dto.AddressDTO;
import com.photoapp.shared.dto.UserDTO;

class UserServiceImplTest {

	@InjectMocks
	UserServiceImpl userService;

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Mock
	Utils utils;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Mock
	AmazonSES amazonSES;

	String userId = "h7xzbmf";
	String encryptedPassword = "hcx17xzbmf";
	UserEntity userEntity;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setFirstName("Tony");
		userEntity.setLastName("TestLastName");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("test@test.com");
		userEntity.setEmailVerificationToken("janjnsad");
		userEntity.setAddresses(getAddressesEntity());
	}

	@Test
	final void testGetUser() {

		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

		UserDTO userDTO = userService.getUser("test@test.com");
		assertNotNull(userDTO);
		assertEquals("Tony", userDTO.getFirstName());
	}

	@Test
	final void testGetUser_UsernameNotFoundException() {
		when(userRepository.findByEmail(anyString())).thenReturn(null);

		assertThrows(UsernameNotFoundException.class, () -> {
			userService.getUser("test@test.com");
		}

		);
	}
	
	@Test
	final void testCreateUser_CreateUserServiceException()
	{
		when(userRepository.findByEmail(anyString())).thenReturn(userEntity);
		UserDTO userDTO = new UserDTO();
		userDTO.setAddresses(getAddressesDTO());
		userDTO.setFirstName("Tony");
		userDTO.setLastName("TestLastName");
		userDTO.setPassword("12345");
		userDTO.setEmail("test@test.com");
 	
		assertThrows(UserServiceException.class,

				() -> {
					userService.createUser(userDTO);
				}

		);
	}
	

	@Test
	final void testCreateUser() {

		when(userRepository.findByEmail(anyString())).thenReturn(null);
		when(utils.generateAddressId(anyInt())).thenReturn("hjka31jsa8");
		when(utils.generateUserId(anyInt())).thenReturn(userId);
		when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptedPassword);
		when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDTO.class));
				
		
		UserDTO userDTO = new UserDTO();
		userDTO.setAddresses(getAddressesDTO());
		userDTO.setFirstName("Tony");
		userDTO.setLastName("TestLastName");
		userDTO.setPassword("12345");
		userDTO.setEmail("test@test.com");
		
		UserDTO storedUserDetails =userService.createUser(userDTO);
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
		verify(utils,times(storedUserDetails.getAddresses().size())).generateAddressId(30);
		verify(bCryptPasswordEncoder, times(1)).encode("12345");
		verify(userRepository,times(1)).save(any(UserEntity.class));
	}
	private List<AddressDTO> getAddressesDTO() {
		AddressDTO addressDto = new AddressDTO();
		addressDto.setType("shipping");
		addressDto.setCity("Moscow");
		addressDto.setCountry("Russia");
		addressDto.setPostalCode("aabbcc");
		addressDto.setStreetName("1 Street name");

		AddressDTO billingAddressDto = new AddressDTO();
		billingAddressDto.setType("billling");
		billingAddressDto.setCity("Moscow");
		billingAddressDto.setCountry("Russia");
		billingAddressDto.setPostalCode("aabbcc");
		billingAddressDto.setStreetName("1 Street name");

		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);

		return addresses;

	}
	
	private List<AddressEntity> getAddressesEntity()
	{
		List<AddressDTO> addresses = getAddressesDTO();
		
	    Type listType = new TypeToken<List<AddressEntity>>() {}.getType();
	    
	    return new ModelMapper().map(addresses, listType);
	}
	
	

}
