package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalDetailDTO;

import java.util.List;

public interface FutsalDetailService {

    List<FutsalDetailDTO> getAll();

    FutsalDetailDTO getById(Long id);

    FutsalDetailDTO createFutsalDetail(FutsalDetailDTO futsalDetailDTO);

    CustomResponse<FutsalDetailDTO> updateFutsalDetail(FutsalDetailDTO futsalDetailDTO);

    void deleteFutsalDetail(Long id);
}

