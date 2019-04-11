package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalDTO;

import java.util.List;

public interface FutsalService {
    List<FutsalDTO> getAll();

    FutsalDTO getById(Long id);

    FutsalDTO createFutsal(FutsalDTO futsalDTO);

    CustomResponse<FutsalDTO> updateFutsal(FutsalDTO futsalDTO);

    void deleteFutsal(Long id);
}