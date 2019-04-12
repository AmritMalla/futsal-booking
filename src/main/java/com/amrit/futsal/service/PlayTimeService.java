package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.PlayTimeDTO;

import java.util.List;

public interface PlayTimeService {

    List<PlayTimeDTO> getAll();

    PlayTimeDTO getById(Long id);

    PlayTimeDTO savePlayTime(PlayTimeDTO playTimeDTO);

    CustomResponse<PlayTimeDTO> updatePlayTime(PlayTimeDTO playTimeDTO);

    void deletePlayTime(Long id);
}