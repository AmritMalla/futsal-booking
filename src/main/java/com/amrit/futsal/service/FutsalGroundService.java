package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalGroundDTO;

import java.util.List;

public interface FutsalGroundService {

    List<FutsalGroundDTO> getAll();

    FutsalGroundDTO findById(Long id);

    FutsalGroundDTO saveFutsalGround(FutsalGroundDTO futsalGroundDTO);

    CustomResponse<FutsalGroundDTO> updateFutsalGround(FutsalGroundDTO futsalGroundDTO);

    void deleteFutsalGround(Long id);

}
