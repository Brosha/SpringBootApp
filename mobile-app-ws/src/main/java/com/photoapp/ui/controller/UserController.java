package com.photoapp.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.photoapp.ui.model.request.PasswordResetModel;
import com.photoapp.ui.model.request.PasswordResetRequestModel;
import com.photoapp.ui.model.request.UserDetailsRequestModel;
import com.photoapp.ui.model.response.AddressesRest;
import com.photoapp.ui.model.response.ErrorMessages;
import com.photoapp.ui.model.response.OperationStatusModel;
import com.photoapp.ui.model.response.RequestOperationStatus;
import com.photoapp.ui.model.response.UserRest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/users") // http://localhost:8080/users
//http://localhost:8080/mobile-app-ws/users
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;
	
	
	@ApiOperation(value = "The Get User Details Web Service Endpoint",
			notes="${userController.GetUser.ApiOperation.Notes}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })
	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue = new UserRest();
		UserDTO userDTO = userService.getUserByUserId(id);
		// BeanUtils.copyProperties(userDTO, returnValue);
		ModelMapper modelMapper = new ModelMapper();
		returnValue = modelMapper.map(userDTO, UserRest.class);
		return returnValue;
	}
	
	
	@ApiOperation(value = "The Create User Details Web Service Endpoint",
			notes="${userController.CreateUser.ApiOperation.Notes}")

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {

		UserRest returnValue = new UserRest();
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		// UserDTO userDTO = new UserDTO();
		// BeanUtils.copyProperties(userDetails, userDTO);
		ModelMapper modelMapper = new ModelMapper();

		UserDTO userDTO = modelMapper.map(userDetails, UserDTO.class);
		UserDTO createdUser = userService.createUser(userDTO);
		returnValue = modelMapper.map(createdUser, UserRest.class);

		return returnValue;

	}
	
	@ApiOperation(value = "The Create User Details Web Service Endpoint",
			notes="${userController.UpdateUser.ApiOperation.Notes}")

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();
		UserDTO userDTO = new UserDTO();
		// BeanUtils.copyProperties(userDetails, userDTO);
		ModelMapper modelMapper = new ModelMapper();

		userDTO = modelMapper.map(userDetails, UserDTO.class);
		UserDTO updatedUser = userService.updateUser(id, userDTO);
		// BeanUtils.copyProperties(updatedUser, returnValue);
		returnValue = new ModelMapper().map(updatedUser, UserRest.class);
		return returnValue;
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {

		OperationStatusModel status = new OperationStatusModel();
		status.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(id);
		status.setOperationResult(RequestOperationStatus.SUCCESS.name());

		return status;

	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "20") int limit) {
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

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })

	// http://localhost:8080/users/{id}/addresses
	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, "application/hal+json" })
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> addressesListRestModel = new ArrayList<AddressesRest>();
		List<AddressDTO> addressesDTO = addressService.getAddresses(id);
		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			ModelMapper modelMapper = new ModelMapper();
			Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			addressesListRestModel = modelMapper.map(addressesDTO, listType);

			for (AddressesRest addressRest : addressesListRestModel) {
				Link userLink = WebMvcLinkBuilder.linkTo(
						WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(userLink);
			}

		}

		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(id))
				.withSelfRel();

		return CollectionModel.of(addressesListRestModel, userLink, selfLink);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })
	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, "application/hal+json" })
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDTO addressDTO = addressService.getAddress(addressId);

		ModelMapper modelMapper = new ModelMapper();
		AddressesRest addressesRestModel = modelMapper.map(addressDTO, AddressesRest.class);
		// http://localhost:8080/users/<userId>
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(userId))
				// .slash(userId)
				// .slash("addresses")
				.withRel("addresses");
		Link selfLink = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(userId, addressId))
				// .slash(userId)
				// .slash("addresses")
				// .slash(addressId)
				.withSelfRel();

		// addressesRestModel.add(userLink);
		// addressesRestModel.add(userAddressesLink);
		// addressesRestModel.add(selfLink);

		return EntityModel.of(addressesRestModel, Arrays.asList(userLink, userAddressesLink, selfLink));
	}

	/*
	 * http://localhost:8080/mobile-app-ws/users/email-verification?token=asdd
	 * 
	 */
	@GetMapping(path = "/email-verification", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE, })
	// @CrossOrigin(origins="*")
	public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		boolean isVerified = userService.verifyEmailToken(token);
		if (isVerified) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}

		return returnValue;
	}

	/*
	 * http://localhost:8080/mobile-app-ws/users/password-reset-request
	 * 
	 */
	@PostMapping(path = "/password-reset-request", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
		OperationStatusModel returnValue = new OperationStatusModel();

		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());

		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

		if (operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}

		return returnValue;
	}

	@PostMapping(path = "/password-reset", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
		OperationStatusModel returnValue = new OperationStatusModel();

		boolean operationResult = userService.resetPassword(passwordResetModel.getToken(),
				passwordResetModel.getPassword());

		returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

		if (operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}

		return returnValue;
	}

}
