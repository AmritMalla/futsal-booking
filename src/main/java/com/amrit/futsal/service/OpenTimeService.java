package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.OpenTimeDTO;

import java.util.List;

public interface OpenTimeService {
    List<OpenTimeDTO> getAll();

    OpenTimeDTO findById(Long id);

    OpenTimeDTO saveOpenTime(OpenTimeDTO openTimeDTO);

    CustomResponse<OpenTimeDTO> updateOpenTime(OpenTimeDTO openTimeDTO);

    void deleteOpenTime(Long id);
}