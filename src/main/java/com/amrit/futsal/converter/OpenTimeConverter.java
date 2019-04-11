package com.amrit.futsal.converter;

import com.amrit.futsal.entity.OpenTime;
import com.amrit.futsal.model.OpenTimeDTO;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class OpenTimeConverter {

    @Autowired
    FutsalGroundService futsalGroundService;

    @Autowired
    FutsalGroundConverter futsalGroundConverter;

    public OpenTimeDTO convertToDto(OpenTime openTime) {
        OpenTimeDTO openTimeDTO = new OpenTimeDTO();
        openTimeDTO.setId(openTime.getId());
        openTimeDTO.setStartTime(openTime.getStartTime());
        openTimeDTO.setEndTime(openTime.getEndTime());
        openTimeDTO.setLocalDateTime(openTime.getLocalDateTime());
        openTimeDTO.setFutsalGround(openTime.getFutsalGround().getId());
        return openTimeDTO;
    }

    public List<OpenTimeDTO> convertToDtoList(List<OpenTime> openTimeList) {
        List<OpenTimeDTO> openTimeDTOS = new ArrayList<>();
        Iterator<OpenTime> openTimeIterator = openTimeList.iterator();
        while (openTimeIterator.hasNext()) {
            openTimeDTOS.add(convertToDto(openTimeIterator.next()));
        }
        return openTimeDTOS;
    }

    public OpenTime convertToEntity(OpenTimeDTO openTimeDTO) {
        OpenTime openTime = new OpenTime();

        if (openTimeDTO.getId() != null) {
            openTime.setId(openTimeDTO.getId());
        }
        openTime.setStartTime(openTimeDTO.getStartTime());
        openTime.setEndTime(openTimeDTO.getEndTime());
        openTime.setLocalDateTime(openTimeDTO.getLocalDateTime());
        openTime.setFutsalGround(futsalGroundConverter.convertToEntity(
                futsalGroundService.findById(openTime.getFutsalGround().getId())));
        return openTime;

    }

    List<OpenTime> convertToEntityList(List<OpenTimeDTO> openTimeDTOS) {
        List<OpenTime> openTimeList = new ArrayList<>();
        Iterator<OpenTimeDTO> openTimeDTOIterator = openTimeDTOS.iterator();
        while (openTimeDTOIterator.hasNext()) {
            openTimeList.add(convertToEntity(openTimeDTOIterator.next()));
        }
        return openTimeList;
    }
}
