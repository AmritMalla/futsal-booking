package com.amrit.futsal.converter;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.PlayTime;
import com.amrit.futsal.model.PlayTimeDTO;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class PlayTimeConverter {

    @Autowired
    FutsalGroundService futsalGroundService;

    @Autowired
    FutsalGroundConverter futsalGroundConverter;

    public PlayTimeDTO convertToDto(PlayTime playTime) {
        PlayTimeDTO playTimeDTO = new PlayTimeDTO();
        playTimeDTO.setId(playTime.getId());
        playTimeDTO.setStartTime(playTime.getStartTime());
        playTimeDTO.setEndTime(playTime.getEndTime());
        playTimeDTO.setFutsalGround(playTime.getFutsalGround().getId());
        playTimeDTO.setPlayTimeStatus(playTime.getPlayTimeStatus());
        playTimeDTO.setPrice(playTime.getPrice());
        return playTimeDTO;
    }

    public List<PlayTimeDTO> convertToDtoList(List<PlayTime> playTimeList) {
        List<PlayTimeDTO> playTimeDTOS = new ArrayList<>();
        Iterator<PlayTime> playTimeIterator = playTimeList.iterator();
        while (playTimeIterator.hasNext()) {
            playTimeDTOS.add(convertToDto(playTimeIterator.next()));
        }
        return playTimeDTOS;
    }

    public PlayTime convertToEntity(PlayTimeDTO playTimeDTO) {
        PlayTime playTime = new PlayTime();

        if(playTimeDTO.getId() != null){
            playTime.setId(playTimeDTO.getId());
        }

        playTime.setStartTime(playTimeDTO.getStartTime());
        playTime.setEndTime(playTimeDTO.getEndTime());
        playTime.setPlayTimeStatus(playTimeDTO.getPlayTimeStatus());
        playTime.setPrice(playTimeDTO.getPrice());
        FutsalGround futsalGround = futsalGroundConverter.convertToEntity(futsalGroundService.getById(playTime.getFutsalGround().getId()));
        playTime.setFutsalGround(futsalGround);

        return playTime;

    }

    List<PlayTime> convertToEntityList(List<PlayTimeDTO> playTimeDTOS) {
        List<PlayTime> playTimeList = new ArrayList<>();
        Iterator<PlayTimeDTO> playTimeDTOIterator = playTimeDTOS.iterator();
        while (playTimeDTOIterator.hasNext()) {
            playTimeList.add(convertToEntity(playTimeDTOIterator.next()));
        }
        return playTimeList;
    }
}
