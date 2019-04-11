package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.VendorConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Vendor;
import com.amrit.futsal.model.VendorDTO;
import com.amrit.futsal.repository.VendorRepository;
import com.amrit.futsal.service.VendorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VendorServiceImpl implements VendorService {

    Logger logger = LoggerFactory.getLogger(VendorServiceImpl.class);

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    VendorConverter vendorConverter;

    @Override
    public List<VendorDTO> getAll() {
        return vendorConverter.convertToDtoList(vendorRepository.findAll());
    }

    @Override
    public VendorDTO getById(Long id) {
//        Vendor vendor = vendorRepository.findById(id).orElse(new Vendor(0L));
        Optional<Vendor> optionalVendor = vendorRepository.findById(id);
        if (!optionalVendor.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return vendorConverter.convertToDto(optionalVendor.get());
    }

    @Override
    public VendorDTO createVendor(VendorDTO vendorDTO) {
        Vendor vendor = vendorConverter.convertToEntity(vendorDTO);
        Vendor save = vendorRepository.save(vendor);
        return vendorConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<VendorDTO> updateVendor(VendorDTO vendorDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<Vendor> optionalVendor = vendorRepository.findById(vendorDTO.getId());

            if (!optionalVendor.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("Vendor with given id doesn't exist");
                return customResponse;
            }
            Vendor vendor = vendorConverter.convertToEntity(vendorDTO);
            Vendor save = vendorRepository.save(vendor);
            map.put("vendor", vendorConverter.convertToDto(save));
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully updated");
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @Override
    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }
}
