package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.FutsalConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Futsal;
import com.amrit.futsal.model.FutsalDTO;
import com.amrit.futsal.repository.FutsalRepository;
import com.amrit.futsal.service.FutsalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FutsalServiceImpl implements FutsalService {


    Logger logger = LoggerFactory.getLogger(FutsalServiceImpl.class);

    @Autowired
    FutsalRepository futsalRepository;

    @Autowired
    FutsalConverter futsalConverter;

    @Override
    public List<FutsalDTO> getAll() {
        return futsalConverter.convertToDtoList(futsalRepository.findAll());
    }

    @Override
    public FutsalDTO getById(Long id) {
        Optional<Futsal> optionalFutsal = futsalRepository.findById(id);
        if (!optionalFutsal.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return futsalConverter.convertToDto(optionalFutsal.get());
    }

    @Override
    public FutsalDTO createFutsal(FutsalDTO futsalDTO) {
        Futsal futsal = futsalConverter.convertToEntity(futsalDTO);
        Futsal save = futsalRepository.save(futsal);
        return futsalConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<FutsalDTO> updateFutsal(FutsalDTO futsalDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<Futsal> optionalFutsal = futsalRepository.findById(futsalDTO.getId());

            if (!optionalFutsal.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("Futsal with given id doesn't exist");
                return customResponse;
            }
            Futsal futsal = futsalConverter.convertToEntity(futsalDTO);
            Futsal save = futsalRepository.save(futsal);
            map.put("futsal", futsalConverter.convertToDto(save));
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
    public void deleteFutsal(Long id) {
        futsalRepository.deleteById(id);
    }
}

