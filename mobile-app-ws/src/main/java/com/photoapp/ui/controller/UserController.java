package com.photoapp.ui.controller;




import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.photoapp.exceptions.UserServiceException;
import com.photoapp.service.AddressService;
import com.photoapp.service.UserService;
import com.photoapp.shared.dto.AddressDTO;
import com.photoapp.shared.dto.UserDTO;
import com.photoapp.ui.model.request.UserDetailsRequestModel;
import com.photoapp.ui.model.response.AddressesRest;
import com.photoapp.ui.model.response.ErrorMessages;
import com.photoapp.ui.model.response.OperationStatusModel;
import com.photoapp.ui.model.response.RequestOperationStatus;
import com.photoapp.ui.model.response.UserRest;



@RestController
@RequestMapping("users") // http://localhost:8080/users
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressService;
	
	@GetMapping(path = "/{id}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();
		UserDTO userDTO = userService.getUserByUserId(id);
		//BeanUtils.copyProperties(userDTO, returnValue);
		ModelMapper modelMapper = new ModelMapper();
		returnValue = modelMapper.map(userDTO, UserRest.class);
		return returnValue;
	}
	
	@PostMapping(
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception{
		
		UserRest returnValue = new UserRest();
		if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		//UserDTO userDTO = new UserDTO();
		//BeanUtils.copyProperties(userDetails, userDTO);		
		ModelMapper modelMapper = new ModelMapper();
		
		UserDTO userDTO = modelMapper.map(userDetails, UserDTO.class);
		UserDTO createdUser = userService.createUser(userDTO);
		returnValue = modelMapper.map(createdUser, UserRest.class);	
		
		return returnValue;
		
	}
	
	@PutMapping(
			path = "/{id}",
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();
		UserDTO userDTO = new UserDTO();
		BeanUtils.copyProperties(userDetails, userDTO);
		UserDTO updatedUser = userService.updateUser(id, userDTO);
		BeanUtils.copyProperties(updatedUser, returnValue);
		return returnValue;
	}
	
	@DeleteMapping(
			path = "/{id}",			
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public OperationStatusModel deleteUser(@PathVariable String id) {
		
		OperationStatusModel status = new OperationStatusModel();
		status.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(id);		
		status.setOperationResult(RequestOperationStatus.SUCCESS.name());
		
		return status;
		
	}
	
	@GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public List<UserRest> getUsers(
			@RequestParam(value="page", defaultValue="0") int page,
			@RequestParam(value="limit", defaultValue="20") int limit 
			){
		List<UserRest> returnValue = new ArrayList<>();
		ModelMapper modelMapper = new ModelMapper();
		List<UserDTO> users = userService.getUsers(page, limit);
		for (UserDTO userDTO : users) {
			UserRest model = new UserRest();
			model = modelMapper.map(userDTO, UserRest.class);
			returnValue.add(model);
		}
		return returnValue;
	}
	
	// http://localhost:8080/users/{id}/addresses
	@GetMapping(path = "/{id}/addresses",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public List<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue  = new ArrayList<AddressesRest>(); 
		List<AddressDTO> addressesDTO= addressService.getAddresses(id);
		if(addressesDTO!=null && !addressesDTO.isEmpty()) {
			ModelMapper modelMapper = new ModelMapper();
			Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
			returnValue = modelMapper.map(addressesDTO, listType);
		}
		return returnValue;
	}
	@GetMapping(path = "/{id}/addresses/{addressId}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public AddressesRest getUserAddress(@PathVariable String addressId) {
		AddressesRest returnValue  = new AddressesRest(); 
		AddressDTO addressDTO= addressService.getAddress(addressId);
		
		ModelMapper modelMapper = new ModelMapper();			
		returnValue = modelMapper.map(addressDTO, AddressesRest.class);
		
		return returnValue;
	}
	
	
}
