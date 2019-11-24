package com.photoapp.ui.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.photoapp.service.UserService;
import com.photoapp.shared.dto.AddressDTO;
import com.photoapp.shared.dto.UserDTO;
import com.photoapp.ui.model.response.UserRest;

class UserControllerTest {
	
	@InjectMocks
	UserController userController;
	
	@Mock
	UserService userService;
	
	UserDTO userDTO;
	String userId= "h7xzbmf";
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		userDTO = new UserDTO();
		userDTO.setAddresses(getAddressesDTO());
		userDTO.setFirstName("Tony");
		userDTO.setLastName("TestLastName");
		userDTO.setEmail("test@test.com");
		userDTO.setEmailVerificationStatus(Boolean.FALSE);
		userDTO.setEmailVerificationToken(null);
		userDTO.setEncryptedPassword("hcx17xzbmf");
		userDTO.setUserId(userId);
	}

	@Test
	final void testGetUser() {
		when(userService.getUserByUserId(anyString())).thenReturn(userDTO);
		UserRest userRest = userController.getUser(userId);
		
		assertNotNull(userRest);
		assertEquals(userId, userRest.getUserId());
		assertEquals(userDTO.getFirstName(), userRest.getFirstName());
		assertTrue(userDTO.getAddresses().size() == userRest.getAddresses().size());
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
	

}
