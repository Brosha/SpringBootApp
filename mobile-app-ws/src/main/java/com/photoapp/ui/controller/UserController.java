package com.photoapp.ui.controller;




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
import org.springframework.web.bind.annotation.RestController;

import com.photoapp.exceptions.UserServiceException;
import com.photoapp.service.UserService;
import com.photoapp.shared.dto.UserDTO;
import com.photoapp.ui.model.request.UserDetailsRequestModel;
import com.photoapp.ui.model.response.ErrorMessages;
import com.photoapp.ui.model.response.OperationStatusModel;
import com.photoapp.ui.model.response.RequestOperationStatus;
import com.photoapp.ui.model.response.UserRest;

@RestController
@RequestMapping("users") // http://localhost:8080/users
public class UserController {
	
	@Autowired
	UserService userService;
	
	@GetMapping(path = "/{id}",
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();
		UserDTO userDTO = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDTO, returnValue);
		return returnValue;
	}
	
	@PostMapping(
			consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
			)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception{
		
		UserRest returnValue = new UserRest();
		if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		UserDTO userDTO = new UserDTO();
		BeanUtils.copyProperties(userDetails, userDTO);
		UserDTO createdUser = userService.createUser(userDTO);
		BeanUtils.copyProperties(createdUser, returnValue);
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
	
	
	
	
	
}
