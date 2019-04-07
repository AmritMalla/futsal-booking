package com.amrit.futsal.api;


import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Futsal;
import com.amrit.futsal.repository.FutsalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/futsalapp/futsal")
public class FutsalController {

    @Autowired
    FutsalRepository futsalRepository;


    @GetMapping()
    public CustomResponse getAllFustsals(){
        CustomResponse customResponse=new CustomResponse();
        try{
            Map<String,Object> map=new HashMap<>();
            List<Futsal> futsals=futsalRepository.findAll();
            if(futsals.size()==0){
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("futsals",futsals);
            customResponse.setStatus(200);
            customResponse.setBody(map);
            return customResponse;
        }catch (Exception e){
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            e.printStackTrace();
            return customResponse;

        }
    }
}
