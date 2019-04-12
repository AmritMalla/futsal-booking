package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.FutsalDetailConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.FutsalDetail;
import com.amrit.futsal.model.FutsalDetailDTO;
import com.amrit.futsal.repository.FutsalDetailRepository;
import com.amrit.futsal.service.FutsalDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FutsalDetailServiceImpl implements FutsalDetailService {

    Logger logger = LoggerFactory.getLogger(FutsalDetailServiceImpl.class);

    @Autowired
    FutsalDetailRepository futsalDetailRepository;

    @Autowired
    FutsalDetailConverter futsalDetailConverter;

    @Override
    public List<FutsalDetailDTO> getAll() {
        return futsalDetailConverter.convertToDtoList(futsalDetailRepository.findAll());
    }

    @Override
    public FutsalDetailDTO getById(Long id) {
//        FutsalDetail futsalDetail = futsalDetailRepository.getById(id).orElse(new FutsalDetail(0L));
        Optional<FutsalDetail> optionalFutsalDetail = futsalDetailRepository.findById(id);
        if (!optionalFutsalDetail.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return futsalDetailConverter.convertToDto(optionalFutsalDetail.get());
    }

    @Override
    public FutsalDetailDTO createFutsalDetail(FutsalDetailDTO futsalDetailDTO) {
        FutsalDetail futsalDetail = futsalDetailConverter.convertToEntity(futsalDetailDTO);
        FutsalDetail save = futsalDetailRepository.save(futsalDetail);
        return futsalDetailConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<FutsalDetailDTO> updateFutsalDetail(FutsalDetailDTO futsalDetailDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<FutsalDetail> optionalFutsalDetail = futsalDetailRepository.findById(futsalDetailDTO.getId());

            if (!optionalFutsalDetail.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("FutsalDetail with given id doesn't exist");
                return customResponse;
            }
            FutsalDetail futsalDetail = futsalDetailConverter.convertToEntity(futsalDetailDTO);
            FutsalDetail save = futsalDetailRepository.save(futsalDetail);
            map.put("futsalDetail", futsalDetailConverter.convertToDto(save));
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
    public void deleteFutsalDetail(Long id) {
        futsalDetailRepository.deleteById(id);
    }
}

