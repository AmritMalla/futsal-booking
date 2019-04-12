package com.amrit.futsal.api;


import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.FutsalGroundDTO;
import com.amrit.futsal.service.FutsalGroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/futsalgrounds")
public class FutsalGroundController {

    @Autowired
    private FutsalGroundService futsalGroundService;

    @GetMapping("")
    public CustomResponse getAllFutsalGrounds() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<FutsalGroundDTO> futsalGroundDTOS = futsalGroundService.getAll();
            if (futsalGroundDTOS.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("futsalGrounds", futsalGroundDTOS);
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
    public CustomResponse<FutsalGroundDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            FutsalGroundDTO futsalGroundDTO = futsalGroundService.getById(id);
            if (futsalGroundDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("FutsalGround with id :" + id + " not found");
                return  customResponse;
            }
            map.put("futsalGround", futsalGroundDTO);
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
    public void deleteFutsalGround(@RequestParam("id") Long id) {
        futsalGroundService.deleteFutsalGround(id);
    }

    @PostMapping("")
    public CustomResponse<FutsalGroundDTO> createFutsalGround(@RequestBody FutsalGroundDTO futsalGroundDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            FutsalGroundDTO saveFutsalGround = futsalGroundService.saveFutsalGround(futsalGroundDTO);
            if (saveFutsalGround == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("FutsalGround saved");
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
    public CustomResponse<FutsalGroundDTO> updateFutsalGround(@RequestBody FutsalGroundDTO futsalGroundDTO) {
        return futsalGroundService.updateFutsalGround(futsalGroundDTO);
    }

}
