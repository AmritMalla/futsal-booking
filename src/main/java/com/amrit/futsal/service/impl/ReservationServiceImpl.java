package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.ReservationConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Reservation;
import com.amrit.futsal.model.ReservationDTO;
import com.amrit.futsal.repository.ReservationRepository;
import com.amrit.futsal.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReservationServiceImpl implements ReservationService {

    Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationConverter reservationConverter;

    @Override
    public List<ReservationDTO> getAll() {
        return reservationConverter.convertToDtoList(reservationRepository.findAll());
    }

    @Override
    public ReservationDTO getById(Long id) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        if (!optionalReservation.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return reservationConverter.convertToDto(optionalReservation.get());
    }

    @Override
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        Reservation reservation = reservationConverter.convertToEntity(reservationDTO);
        Reservation save = reservationRepository.save(reservation);
        return reservationConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<ReservationDTO> updateReservation(ReservationDTO reservationDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<Reservation> optionalReservation = reservationRepository.findById(reservationDTO.getId());

            if (!optionalReservation.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("Reservation with given id doesn't exist");
                return customResponse;
            }
            Reservation reservation = reservationConverter.convertToEntity(reservationDTO);
            Reservation save = reservationRepository.save(reservation);
            map.put("reservation", reservationConverter.convertToDto(save));
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully updated");
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
