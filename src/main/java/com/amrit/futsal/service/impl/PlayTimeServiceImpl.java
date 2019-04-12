package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.PlayTimeConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.PlayTime;
import com.amrit.futsal.model.PlayTimeDTO;
import com.amrit.futsal.repository.PlayTimeRepository;
import com.amrit.futsal.service.PlayTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlayTimeServiceImpl implements PlayTimeService {

    Logger logger = LoggerFactory.getLogger(PlayTimeServiceImpl.class);

    @Autowired
    PlayTimeRepository playTimeRepository;

    @Autowired
    PlayTimeConverter playTimeConverter;

    @Override
    public List<PlayTimeDTO> getAll() {
        return playTimeConverter.convertToDtoList(playTimeRepository.findAll());
    }

    @Override
    public PlayTimeDTO getById(Long id) {
        Optional<PlayTime> optionalPlayTime = playTimeRepository.findById(id);
        if (!optionalPlayTime.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return playTimeConverter.convertToDto(optionalPlayTime.get());
    }

    @Override
    public PlayTimeDTO savePlayTime(PlayTimeDTO playTimeDTO) {
        PlayTime playTime = playTimeConverter.convertToEntity(playTimeDTO);
        PlayTime save = playTimeRepository.save(playTime);
        return playTimeConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<PlayTimeDTO> updatePlayTime(PlayTimeDTO playTimeDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<PlayTime> optionalPlayTime = playTimeRepository.findById(playTimeDTO.getId());

            if (!optionalPlayTime.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("PlayTime with given id doesn't exist");
                return customResponse;
            }
            PlayTime playTime = playTimeConverter.convertToEntity(playTimeDTO);
            PlayTime save = playTimeRepository.save(playTime);
            map.put("playTime", playTimeConverter.convertToDto(save));
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
    public void deletePlayTime(Long id) {
        playTimeRepository.deleteById(id);
    }
}
