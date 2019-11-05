package com.photoapp.service;

import java.util.List;

import com.photoapp.shared.dto.AddressDTO;

public interface AddressService {
	List<AddressDTO> getAddresses(String userId);

	AddressDTO getAddress(String addressId);
}
