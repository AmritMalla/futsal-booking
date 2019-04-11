package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalDetailDTO;
import com.amrit.futsal.service.FutsalDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/futsaldetails")
public class FutsalDetailController {

    @Autowired
    private FutsalDetailService futsalDetailService;

    @GetMapping("")
    public CustomResponse getAllFutsalDetails() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<FutsalDetailDTO> futsalDetailDTOS = futsalDetailService.getAll();
            if (futsalDetailDTOS.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("futsalDetails", futsalDetailDTOS);
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
    public CustomResponse<FutsalDetailDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            FutsalDetailDTO futsalDetailDTO = futsalDetailService.getById(id);
            if (futsalDetailDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("FutsalDetail with id :" + id + " not found");
                return  customResponse;
            }
            map.put("futsalDetail", futsalDetailDTO);
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
    public void deleteFutsalDetail(@RequestParam("id") Long id) {
        futsalDetailService.deleteFutsalDetail(id);
    }

    @PostMapping("")
    public CustomResponse<FutsalDetailDTO> createFutsalDetail(@RequestBody FutsalDetailDTO futsalDetailDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            FutsalDetailDTO saveFutsalDetail = futsalDetailService.createFutsalDetail(futsalDetailDTO);
            if (saveFutsalDetail == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("FutsalDetail saved");
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
    public CustomResponse<FutsalDetailDTO> updateFutsalDetail(@RequestBody FutsalDetailDTO futsalDetailDTO) {
        return futsalDetailService.updateFutsalDetail(futsalDetailDTO);
    }

}
