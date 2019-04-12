package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.PlayTimeDTO;
import com.amrit.futsal.service.PlayTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/playtimes")
public class PlayTimeController {

    @Autowired
    private PlayTimeService playTimeService;

    @GetMapping("")
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<PlayTimeDTO> playTimeDTOS = playTimeService.getAll();
            if (playTimeDTOS.size() == 0) {
                customResponse.setStatus(404);
                customResponse.setMessage("Success");
                return customResponse;
            }
            map.put("playTimes", playTimeDTOS);
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
    public CustomResponse<PlayTimeDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            PlayTimeDTO playTimeDTO = playTimeService.getById(id);
            if (playTimeDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("PlayTime with id :" + id + " not found");
                return customResponse;
            }
            map.put("playTime", playTimeDTO);
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
    public void deletePlayTime(@RequestParam("id") Long id) {
        playTimeService.deletePlayTime(id);
    }

    @PostMapping("")
    public CustomResponse<PlayTimeDTO> createPlayTime(@RequestBody PlayTimeDTO playTimeDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            PlayTimeDTO savePlayTime = playTimeService.savePlayTime(playTimeDTO);
            if(savePlayTime == null){
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("PlayTime saved");
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
    public CustomResponse<PlayTimeDTO> updatePlayTime(@RequestBody PlayTimeDTO playTimeDTO) {
        return playTimeService.updatePlayTime(playTimeDTO);
    }

}
