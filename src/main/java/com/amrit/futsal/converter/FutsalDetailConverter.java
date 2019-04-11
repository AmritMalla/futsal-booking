package com.amrit.futsal.converter;

import com.amrit.futsal.entity.Futsal;
import com.amrit.futsal.entity.FutsalDetail;
import com.amrit.futsal.model.FutsalDetailDTO;
import com.amrit.futsal.service.FutsalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class FutsalDetailConverter {

    @Autowired
    FutsalService futsalService;

    @Autowired
    FutsalConverter futsalConverter;

    public FutsalDetailDTO convertToDto(FutsalDetail futsalDetail) {
        FutsalDetailDTO futsalDetailDTO = new FutsalDetailDTO();
        futsalDetailDTO.setId(futsalDetail.getId());
        futsalDetailDTO.setTopic(futsalDetail.getTopic());
        futsalDetailDTO.setDescription(futsalDetail.getDescription());
        futsalDetailDTO.setFutsal(futsalDetail.getFutsal().getId());
        return futsalDetailDTO;
    }

    public List<FutsalDetailDTO> convertToDtoList(List<FutsalDetail> futsalDetailList) {
        List<FutsalDetailDTO> futsalDetailDTOS = new ArrayList<>();
        Iterator<FutsalDetail> futsalDetailIterator = futsalDetailList.iterator();
        while (futsalDetailIterator.hasNext()) {
            futsalDetailDTOS.add(convertToDto(futsalDetailIterator.next()));
        }
        return futsalDetailDTOS;
    }

    public FutsalDetail convertToEntity(FutsalDetailDTO futsalDetailDTO) {
        FutsalDetail futsalDetail = new FutsalDetail();

        if(futsalDetailDTO.getId() != null){
            futsalDetail.setId(futsalDetailDTO.getId());
        }
        futsalDetail.setTopic(futsalDetailDTO.getTopic());
        futsalDetail.setDescription(futsalDetailDTO.getDescription());
        Futsal futsal = futsalConverter.convertToEntity(futsalService.getById(futsalDetailDTO.getFutsal()));
        futsalDetail.setFutsal(futsal);
        return futsalDetail;

    }

    List<FutsalDetail> convertToEntityList(List<FutsalDetailDTO> futsalDetailDTOS) {
        List<FutsalDetail> futsalDetailList = new ArrayList<>();
        Iterator<FutsalDetailDTO> futsalDetailDTOIterator = futsalDetailDTOS.iterator();
        while (futsalDetailDTOIterator.hasNext()) {
            futsalDetailList.add(convertToEntity(futsalDetailDTOIterator.next()));
        }
        return futsalDetailList;
    }
}
