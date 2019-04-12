package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.ReservationDTO;
import com.amrit.futsal.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("")
    public CustomResponse getAllReservations() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<ReservationDTO> reservationDTOS = reservationService.getAll();
            if (reservationDTOS.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("reservations", reservationDTOS);
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @GetMapping(path = "/{id}")
    public CustomResponse<ReservationDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            ReservationDTO reservationDTO = reservationService.getById(id);
            if (reservationDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("Reservation with id :" + id + " not found");
                return  customResponse;
            }
            map.put("reservation", reservationDTO);
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully retrieved");
            customResponse.setBody(map);
            return customResponse;
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @DeleteMapping("")
    public void deleteReservation(@RequestParam("id") Long id) {
        reservationService.deleteReservation(id);
    }

    @PostMapping("")
    public CustomResponse<ReservationDTO> createReservation(@RequestBody ReservationDTO reservationDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            ReservationDTO saveReservation = reservationService.createReservation(reservationDTO);
            if (saveReservation == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("Reservation saved");
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;
        } catch (Exception e) {

            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @PutMapping("")
    public CustomResponse<ReservationDTO> updateReservation(@RequestBody ReservationDTO reservationDTO) {
        return reservationService.updateReservation(reservationDTO);
    }

}
