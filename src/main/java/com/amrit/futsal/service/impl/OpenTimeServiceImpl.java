package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.OpenTimeConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.OpenTime;
import com.amrit.futsal.model.OpenTimeDTO;
import com.amrit.futsal.repository.OpenTimeRepository;
import com.amrit.futsal.service.OpenTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenTimeServiceImpl implements OpenTimeService {

    Logger logger = LoggerFactory.getLogger(OpenTimeServiceImpl.class);

    @Autowired
    OpenTimeRepository openTimeRepository;

    @Autowired
    OpenTimeConverter openTimeConverter;

    @Override
    public List<OpenTimeDTO> getAll() {
        return openTimeConverter.convertToDtoList(openTimeRepository.findAll());
    }

    @Override
    public OpenTimeDTO getById(Long id) {
        Optional<OpenTime> optionalOpenTime = openTimeRepository.findById(id);
        if (!optionalOpenTime.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return openTimeConverter.convertToDto(optionalOpenTime.get());
    }

    @Override
    public OpenTimeDTO saveOpenTime(OpenTimeDTO openTimeDTO) {
        OpenTime openTime = openTimeConverter.convertToEntity(openTimeDTO);
        OpenTime save = openTimeRepository.save(openTime);
        return openTimeConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<OpenTimeDTO> updateOpenTime(OpenTimeDTO openTimeDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<OpenTime> optionalOpenTime = openTimeRepository.findById(openTimeDTO.getId());

            if (!optionalOpenTime.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("OpenTime with given id doesn't exist");
                return customResponse;
            }
            OpenTime openTime = openTimeConverter.convertToEntity(openTimeDTO);
            OpenTime save = openTimeRepository.save(openTime);
            map.put("openTime", openTimeConverter.convertToDto(save));
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
    public void deleteOpenTime(Long id) {
        openTimeRepository.deleteById(id);
    }
}
