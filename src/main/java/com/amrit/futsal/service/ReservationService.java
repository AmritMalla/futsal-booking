package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.ReservationDTO;

import java.util.List;

public interface ReservationService {
    List<ReservationDTO> getAll();

    ReservationDTO getById(Long id);

    ReservationDTO createReservation(ReservationDTO reservationDTO);

    CustomResponse<ReservationDTO> updateReservation(ReservationDTO reservationDTO);

    void deleteReservation(Long id);
}