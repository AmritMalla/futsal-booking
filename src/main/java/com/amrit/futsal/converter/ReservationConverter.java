package com.amrit.futsal.converter;

import com.amrit.futsal.entity.Customer;
import com.amrit.futsal.entity.FutsalGround;
import com.amrit.futsal.entity.PlayTime;
import com.amrit.futsal.entity.Reservation;
import com.amrit.futsal.model.ReservationDTO;
import com.amrit.futsal.service.CustomerService;
import com.amrit.futsal.service.FutsalGroundService;
import com.amrit.futsal.service.PlayTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ReservationConverter {

    @Autowired
    PlayTimeService playTimeService;

    @Autowired
    PlayTimeConverter playTimeConverter;

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerConverter customerConverter;

    @Autowired
    FutsalGroundService futsalGroundService;

    @Autowired
    FutsalGroundConverter futsalGroundConverter;


    public ReservationDTO convertToDto(Reservation reservation) {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setId(reservation.getId());
        reservationDTO.setCustomer(reservation.getCustomer().getId());
        reservationDTO.setPlayTime(reservation.getPlayTime().getId());
        reservationDTO.setFutsalGround(reservation.getFutsalGround().getId());
        reservationDTO.setReservationStatus(reservation.getReservationStatus());
        return reservationDTO;
    }

    public List<ReservationDTO> convertToDtoList(List<Reservation> reservationList) {
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        Iterator<Reservation> reservationIterator = reservationList.iterator();
        while (reservationIterator.hasNext()) {
            reservationDTOS.add(convertToDto(reservationIterator.next()));
        }
        return reservationDTOS;
    }

    public Reservation convertToEntity(ReservationDTO reservationDTO) {
        Reservation reservation = new Reservation();

        if (reservationDTO.getId() != null) {
            reservation.setId(reservationDTO.getId());
        }
        reservation.setReservationStatus(reservationDTO.getReservationStatus());
        PlayTime playTime = playTimeConverter.convertToEntity(playTimeService.findById(reservationDTO.getPlayTime()));
        FutsalGround futsalGround = futsalGroundConverter.convertToEntity(futsalGroundService.findById(reservationDTO.getFutsalGround()));
        Customer customer = customerConverter.convertToEntity(customerService.getById(reservationDTO.getCustomer()));
        reservation.setPlayTime(playTime);
        reservation.setFutsalGround(futsalGround);
        reservation.setCustomer(customer);
        return reservation;
    }

    List<Reservation> convertToEntityList(List<ReservationDTO> reservationDTOS) {
        List<Reservation> reservationList = new ArrayList<>();
        Iterator<ReservationDTO> reservationDTOIterator = reservationDTOS.iterator();
        while (reservationDTOIterator.hasNext()) {
            reservationList.add(convertToEntity(reservationDTOIterator.next()));
        }
        return reservationList;
    }
}

