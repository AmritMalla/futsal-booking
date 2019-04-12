package com.amrit.futsal.api;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.CustomerDTO;
import com.amrit.futsal.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("")
    public CustomResponse getAllCustomers() {
        CustomResponse customResponse = new CustomResponse();
        try {
            Map<String, Object> map = new HashMap<>();
            List<CustomerDTO> customerDTOS = customerService.getAll();
            if (customerDTOS.size() == 0) {
                customResponse.setStatus(404);
                return customResponse;
            }
            map.put("customers", customerDTOS);
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
    public CustomResponse<CustomerDTO> getOne(@PathVariable("id") Long id) {
        CustomResponse customResponse = new CustomResponse<>();
        try {
            Map<String, Object> map = new HashMap<>();
            CustomerDTO customerDTO = customerService.getById(id);
            if (customerDTO == null) {
                customResponse.setStatus(404);
                customResponse.setMessage("Customer with id :" + id + " not found");
                return  customResponse;
            }
            map.put("customer", customerDTO);
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
    public void deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
    }

    @PostMapping("")
    public CustomResponse<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> map = new HashMap<>();
            CustomerDTO saveCustomer = customerService.createCustomer(customerDTO);
            if (saveCustomer == null) {
                customResponse.setStatus(500);
                customResponse.setMessage("failed to save");
                return customResponse;
            }
            customResponse.setMessage("Customer saved");
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
    public CustomResponse<CustomerDTO> updateCustomer(@RequestBody CustomerDTO customerDTO) {
        return customerService.updateCustomer(customerDTO);
    }

}
