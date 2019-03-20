package com.amrit.futsal.api;


import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Vendor;
import com.amrit.futsal.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/fustsalApp/vendor")
public class VendorController {

    @Autowired
    private VendorRepository vendorRepository;

    @GetMapping("")
    public CustomResponse getAllVendors(){
        CustomResponse customResponse=new CustomResponse();
        try{
            Map<String,Object> map=new HashMap<>();
            List<Vendor> vendors=vendorRepository.findAll();
            if(vendors.size()==0){
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("vendors",vendors);
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
}
