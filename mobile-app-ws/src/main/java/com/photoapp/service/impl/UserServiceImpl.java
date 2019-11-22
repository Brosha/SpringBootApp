package com.photoapp.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.photoapp.exceptions.UserServiceException;
import com.photoapp.io.entity.PasswordResetTokenEntity;
import com.photoapp.io.entity.UserEntity;
import com.photoapp.io.repository.PasswordResetTokenRepository;
import com.photoapp.io.repository.UserRepository;
import com.photoapp.service.UserService;
import com.photoapp.shared.AmazonSES;
import com.photoapp.shared.Utils;
import com.photoapp.shared.dto.AddressDTO;
import com.photoapp.shared.dto.UserDTO;
import com.photoapp.ui.model.response.ErrorMessages;


@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	AmazonSES amazonSES;

	@Override
	public UserDTO createUser(UserDTO userDTO) {
			
		if (userRepository.findByEmail(userDTO.getEmail()) != null) {		
			
			throw new UserServiceException("Record already exists");
			
		}		
		
		for (int i=0; i<userDTO.getAddresses().size();i++) {
			AddressDTO addressDTO = userDTO.getAddresses().get(i);
			addressDTO.setUserDetails(userDTO);
			addressDTO.setAddressId(utils.generateAddressId(30));
			userDTO.getAddresses().set(i, addressDTO);
			
		}
		
		
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
		userEntity.setUserId(publicUserId);
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);
		UserEntity storedUserDetails = userRepository.save(userEntity);
		UserDTO returnUserDTO =  modelMapper.map(storedUserDetails, UserDTO.class);
		
		amazonSES.verifyEmail(returnUserDTO);

		return returnUserDTO;
	}

	@Override
	public UserDTO getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		UserDTO returnUserDTO = new UserDTO();
		BeanUtils.copyProperties(userEntity, returnUserDTO);
		return returnUserDTO;
	}

	@Override
	public UserDTO getUserByUserId(String userId) {
		UserDTO userDTO = new UserDTO();
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);
		BeanUtils.copyProperties(userEntity, userDTO);

		return userDTO;
	}

	@Override
	public UserDTO updateUser(String userId, UserDTO userDTO) {

		UserDTO returnValue = new UserDTO();

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userEntity.setFirstName(userDTO.getFirstName());
		userEntity.setLastName(userDTO.getLastName());
		UserEntity updatedValue = userRepository.save(userEntity);
		BeanUtils.copyProperties(updatedValue, returnValue);

		return returnValue;
	}

	@Override
	public List<UserDTO> getUsers(int page, int limit) {
		List<UserDTO> returnValue = new ArrayList<>();
		Pageable pageable = PageRequest.of(page, limit);
		Page<UserEntity> usersPage = userRepository.findAll(pageable);
		List<UserEntity> users = usersPage.getContent();
		for (UserEntity userEntity : users) {
			UserDTO userDTO = new UserDTO();
			BeanUtils.copyProperties(userEntity, userDTO);
			returnValue.add(userDTO);
		}
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
				userEntity.getEmailVerificationStatus(), true, true, true, new ArrayList<>());
		//return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);

	}

	@Override
	public boolean verifyEmailToken(String token) {
		
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		if(userEntity!=null) {
			
			boolean hastokenExpired= Utils.hasTokenExpired(token);
			if(!hastokenExpired){
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean returnValue = false;
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity==null) {
			return returnValue;
		}
		String token =utils.generatePasswordResetToken(userEntity.getUserId());
		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);
		returnValue = amazonSES.sendPasswordResetRequest(userEntity.getFirstName(), email, token);
		
		return returnValue;
	}

	@Override
	public boolean resetPassword(String token, String password) {
        boolean returnValue = false;
        
        if( Utils.hasTokenExpired(token) )
        {
            return returnValue;
        }
 
        PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);

        if (passwordResetTokenEntity == null) {
            return returnValue;
        }

        // Prepare new password
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        
        // Update User password in database
        UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
        userEntity.setEncryptedPassword(encodedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);
 
        // Verify if password was saved successfully
        if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
            returnValue = true;
        }
   
        // Remove Password Reset token from database
        passwordResetTokenRepository.delete(passwordResetTokenEntity);
        
        return returnValue;
	}

}
