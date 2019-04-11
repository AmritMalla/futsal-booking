package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.FutsalGroundConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.model.FutsalGroundDTO;
import com.amrit.futsal.repository.FutsalGroundRepository;
import com.amrit.futsal.service.FutsalGroundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FutsalGroundServiceImpl implements FutsalGroundService {

    Logger logger = LoggerFactory.getLogger(FutsalGroundServiceImpl.class);

    @Autowired
    FutsalGroundRepository futsalGroundRepository;

    @Autowired
    FutsalGroundConverter futsalGroundConverter;

    @Override
    public List<FutsalGroundDTO> getAll() {
        return futsalGroundConverter.convertToDtoList(futsalGroundRepository.findAll());
    }

    @Override
    public FutsalGroundDTO findById(Long id) {
//        FutsalGround futsalGround = futsalGroundRepository.findById(id).orElse(new FutsalGround(0L));
        Optional<FutsalGround> optionalFutsalGround = futsalGroundRepository.findById(id);
        if (!optionalFutsalGround.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return futsalGroundConverter.convertToDto(optionalFutsalGround.get());
    }

    @Override
    public FutsalGroundDTO saveFutsalGround(FutsalGroundDTO futsalGroundDTO) {
        FutsalGround futsalGround = futsalGroundConverter.convertToEntity(futsalGroundDTO);
        FutsalGround save = futsalGroundRepository.save(futsalGround);
        return futsalGroundConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<FutsalGroundDTO> updateFutsalGround(FutsalGroundDTO futsalGroundDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<FutsalGround> optionalFutsalGround = futsalGroundRepository.findById(futsalGroundDTO.getId());

            if (!optionalFutsalGround.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("FutsalGround with given id doesn't exist");
                return customResponse;
            }
            FutsalGround futsalGround = futsalGroundConverter.convertToEntity(futsalGroundDTO);
            FutsalGround save = futsalGroundRepository.save(futsalGround);
            map.put("futsalGround", futsalGroundConverter.convertToDto(save));
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
    public void deleteFutsalGround(Long id) {
        futsalGroundRepository.deleteById(id);
    }
}
