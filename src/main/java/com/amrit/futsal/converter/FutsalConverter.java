package com.amrit.futsal.converter;

import com.amrit.futsal.entity.Futsal;
import com.amrit.futsal.entity.Vendor;
import com.amrit.futsal.model.FutsalDTO;
import com.amrit.futsal.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class FutsalConverter {

    @Autowired
    VendorService vendorService;

    @Autowired
    VendorConverter vendorConverter;

    public FutsalDTO convertToDto(Futsal futsal) {
        FutsalDTO futsalDTO = new FutsalDTO();
        futsalDTO.setId(futsal.getId());
        futsalDTO.setFutsalName(futsal.getFustsalName());
        futsalDTO.setCity(futsal.getCity());
        futsalDTO.setCountry(futsal.getCountry());
        futsalDTO.setLatitude(futsal.getLatitude());
        futsalDTO.setLongitude(futsal.getLongitude());
        futsalDTO.setStreetAddress(futsal.getStreetAddress());
        futsalDTO.setRating(futsal.getRating());
        futsalDTO.setVendorId(futsal.getVendorId().getId());
        return futsalDTO;
    }

    public List<FutsalDTO> convertToDtoList(List<Futsal> futsalList) {
        List<FutsalDTO> futsalDTOS = new ArrayList<>();
        Iterator<Futsal> futsalIterator = futsalList.iterator();
        while (futsalIterator.hasNext()) {
            futsalDTOS.add(convertToDto(futsalIterator.next()));
        }
        return futsalDTOS;
    }

    public Futsal convertToEntity(FutsalDTO futsalDTO) {
        Futsal futsal = new Futsal();

        if (futsalDTO.getId() != null) {
            futsal.setId(futsalDTO.getId());
        }
        futsal.setFutsalName(futsalDTO.getFutsalName());
        futsal.setCity(futsalDTO.getCity());
        futsal.setCountry(futsalDTO.getCountry());
        futsal.setLatitude(futsalDTO.getLatitude());
        futsal.setLongitude(futsalDTO.getLongitude());
        futsal.setRating(futsalDTO.getRating());
        futsal.setStreetAddress(futsalDTO.getStreetAddress());

        Vendor vendor = vendorConverter.convertToEntity(vendorService.getById(futsalDTO.getVendorId()));
        futsal.setVendorId(vendor);
        return futsal;
    }

    List<Futsal> convertToEntityList(List<FutsalDTO> futsalDTOS) {
        List<Futsal> futsalList = new ArrayList<>();
        Iterator<FutsalDTO> futsalDTOIterator = futsalDTOS.iterator();
        while (futsalDTOIterator.hasNext()) {
            futsalList.add(convertToEntity(futsalDTOIterator.next()));
        }
        return futsalList;
    }
}

