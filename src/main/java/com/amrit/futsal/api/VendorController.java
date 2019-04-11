package com.amrit.futsal.api;


import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.VendorDTO;
import com.amrit.futsal.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("")
    public CustomResponse getAllVendors() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<VendorDTO> vendorDTOS = vendorService.getAll();
            if (vendorDTOS.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("vendors", vendorDTOS);
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
    public CustomResponse<VendorDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            VendorDTO vendorDTO = vendorService.getById(id);
            if (vendorDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("Vendor with id :" + id + " not found");
            }
            map.put("vendor", vendorDTO);
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

    //With pathvariable id to delete
//    @DeleteMapping("/{id}")
//    public void deleteVendor(@PathVariable("id") Long id){
//        vendorService.deleteVendor(id);
//    }


    //With Parameters id to delete
    @DeleteMapping("")
    public void deleteVendor(@RequestParam("id") Long id) {
        vendorService.deleteVendor(id);
    }

    @PostMapping("")
    public CustomResponse<VendorDTO> createVendor(@RequestBody VendorDTO vendorDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            VendorDTO saveVendor = vendorService.createVendor(vendorDTO);
            if (saveVendor == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("Vendor saved");
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
    public CustomResponse<VendorDTO> updateVendor(@RequestBody VendorDTO vendorDTO) {
        return vendorService.updateVendor(vendorDTO);
    }

}
