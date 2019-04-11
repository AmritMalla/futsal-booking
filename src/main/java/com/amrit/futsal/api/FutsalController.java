package com.amrit.futsal.api;


import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalDTO;
import com.amrit.futsal.service.FutsalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/futsals")
public class FutsalController {

    @Autowired
    FutsalService futsalService;


    @GetMapping()
    public CustomResponse getAllFustsals() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<FutsalDTO> futsals = futsalService.getAll();
            if (futsals.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("futsals", futsals);
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;
        } catch (Exception e) {
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            e.printStackTrace();
            return customResponse;
        }
    }

    @GetMapping(path = "/{id}")
    public CustomResponse<FutsalDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            FutsalDTO futsalDTO = futsalService.getById(id);
            if (futsalDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("Futsal with id :" + id + " not found");
                return customResponse;
            }
            map.put("futsal", futsalDTO);
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
    public void deleteFutsal(@RequestParam("id") Long id) {
        futsalService.deleteFutsal(id);
    }

    @PostMapping("")
    public CustomResponse<FutsalDTO> createFutsal(@RequestBody FutsalDTO futsalDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            FutsalDTO saveFutsal = futsalService.createFutsal(futsalDTO);
            if (saveFutsal == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("Futsal saved");
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
    public CustomResponse<FutsalDTO> updateFutsal(@RequestBody FutsalDTO futsalDTO) {

        return futsalService.updateFutsal(futsalDTO);
    }
}

