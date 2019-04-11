package com.amrit.futsal.converter;

import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.model.FutsalGroundDTO;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class FutsalGroundConverter {

    @Autowired
    FutsalGroundService futsalGroundService;

    @Autowired
    FutsalGroundConverter futsalGroundConverter;

    public FutsalGroundDTO convertToDto(FutsalGround futsalGround) {
        FutsalGroundDTO futsalGroundDTO = new FutsalGroundDTO();
        futsalGroundDTO.setId(futsalGround.getId());
        futsalGroundDTO.setCourtType(futsalGround.getCourtType());
        futsalGroundDTO.setFutsalGroundStatus(futsalGround.getFutsalGroundStatus());
        futsalGroundDTO.setWidth(futsalGround.getWidth());
        futsalGroundDTO.setLength(futsalGround.getLength());
        return futsalGroundDTO;
    }

    public List<FutsalGroundDTO> convertToDtoList(List<FutsalGround> futsalGroundList) {
        List<FutsalGroundDTO> futsalGroundDTOS = new ArrayList<>();
        Iterator<FutsalGround> futsalGroundIterator = futsalGroundList.iterator();
        while (futsalGroundIterator.hasNext()) {
            futsalGroundDTOS.add(convertToDto(futsalGroundIterator.next()));
        }
        return futsalGroundDTOS;
    }

    public FutsalGround convertToEntity(FutsalGroundDTO futsalGroundDTO) {
        FutsalGround futsalGround = new FutsalGround();

        if (futsalGroundDTO.getId() != null) {
            futsalGround.setId(futsalGroundDTO.getId());
        }
        futsalGround.setId(futsalGroundDTO.getId());
        futsalGround.setCourtType(futsalGroundDTO.getCourtType());
        futsalGround.setFutsalGroundStatus(futsalGroundDTO.getFutsalGroundStatus());
        futsalGround.setLength(futsalGroundDTO.getLength());
        futsalGround.setWidth(futsalGroundDTO.getWidth());
        return futsalGround;
    }

    List<FutsalGround> convertToEntityList(List<FutsalGroundDTO> futsalGroundDTOS) {
        List<FutsalGround> futsalGroundList = new ArrayList<>();
        Iterator<FutsalGroundDTO> futsalGroundDTOIterator = futsalGroundDTOS.iterator();
        while (futsalGroundDTOIterator.hasNext()) {
            futsalGroundList.add(convertToEntity(futsalGroundDTOIterator.next()));
        }
        return futsalGroundList;
    }
}

