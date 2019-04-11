package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.OpenTimeDTO;
import com.amrit.futsal.service.OpenTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/opentimes")
public class OpenTimeController {
    @Autowired
    private OpenTimeService openTimeService;

    @GetMapping("")
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<OpenTimeDTO> openTimeDTOS = openTimeService.getAll();
            if (openTimeDTOS.size() == 0) {
                customResponse.setStatus(404);
                customResponse.setMessage("Success");
                return customResponse;
            }
            map.put("openTimes", openTimeDTOS);
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


    //With path variable to get
    @GetMapping(path = "/{id}")
    public CustomResponse<OpenTimeDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            OpenTimeDTO openTimeDTO = openTimeService.findById(id);
            if (openTimeDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("OpenTime with id :" + id + " not found");
                return customResponse;
            }
            map.put("openTime", openTimeDTO);
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
    public void deleteOpenTime(@RequestParam("id") Long id) {
        openTimeService.deleteOpenTime(id);
    }

    @PostMapping("")
    public CustomResponse<OpenTimeDTO> createOpenTime(@RequestBody OpenTimeDTO openTimeDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            OpenTimeDTO saveOpenTime = openTimeService.saveOpenTime(openTimeDTO);
            if(saveOpenTime == null){
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("OpenTime saved");
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;
        }catch (Exception e){
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @PutMapping("")
    public CustomResponse<OpenTimeDTO> updateOpenTime(@RequestBody OpenTimeDTO openTimeDTO) {
        return openTimeService.updateOpenTime(openTimeDTO);
    }

}
