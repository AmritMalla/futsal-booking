package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.VendorDTO;

import java.util.List;

public interface VendorService{

    List<VendorDTO> getAll();

    VendorDTO getById(Long id);

    VendorDTO createVendor(VendorDTO vendorDTO);

    CustomResponse<VendorDTO> updateVendor(VendorDTO vendorDTO);

    void deleteVendor(Long id);
}
