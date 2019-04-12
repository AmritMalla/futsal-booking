package com.amrit.futsal.converter;

import com.amrit.futsal.entity.User;
import com.amrit.futsal.entity.Vendor;
import com.amrit.futsal.model.VendorDTO;
import com.amrit.futsal.repository.UserRepository;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class VendorConverter {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserConverter userConverter;

    public VendorDTO convertToDto(Vendor vendor) {
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setId(vendor.getId());
        vendorDTO.setFirstName(vendor.getFirstName());
        vendorDTO.setMiddleName(vendor.getMiddleName());
        vendorDTO.setLastName(vendor.getLastName());
        vendorDTO.setPhoneNumber(vendor.getPhoneNumber());
        vendorDTO.setCity(vendor.getCity());
        vendorDTO.setAddressline1(vendor.getAddressline1());
        vendorDTO.setAddressline2(vendor.getAddressline2());
        vendorDTO.setUser(vendor.getUser().getId());
        return vendorDTO;
    }

    public List<VendorDTO> convertToDtoList(List<Vendor> vendorList) {
        List<VendorDTO> vendorDTOS = new ArrayList<>();
        Iterator<Vendor> vendorIterator = vendorList.iterator();
        while (vendorIterator.hasNext()) {
            vendorDTOS.add(convertToDto(vendorIterator.next()));
        }
        return vendorDTOS;
    }

    public Vendor convertToEntity(VendorDTO vendorDTO) {
        Vendor vendor = new Vendor();

        if (vendorDTO.getId() != null) {
            vendor.setId(vendorDTO.getId());
        }
        vendor.setId(vendorDTO.getId());
        vendor.setFirstName(vendorDTO.getFirstName());
        vendor.setMiddleName(vendorDTO.getMiddleName());
        vendor.setLastName(vendorDTO.getLastName());
        vendor.setCity(vendorDTO.getCity());
        vendor.setPhoneNumber(vendorDTO.getPhoneNumber());
        vendor.setAddressline1(vendorDTO.getAddressline1());
        vendor.setAddressline2(vendorDTO.getAddressline2());
        User user = userConverter.convertToEntity(userService.getById(vendorDTO.getUser()));
        vendor.setUser(user);
        return vendor;
    }

    List<Vendor> convertToEntityList(List<VendorDTO> vendorDTOS) {
        List<Vendor> vendorList = new ArrayList<>();
        Iterator<VendorDTO> vendorDTOIterator = vendorDTOS.iterator();
        while (vendorDTOIterator.hasNext()) {
            vendorList.add(convertToEntity(vendorDTOIterator.next()));
        }
        return vendorList;
    }
}

