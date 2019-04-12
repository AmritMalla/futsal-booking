package com.amrit.futsal.service.impl;

import com.amrit.futsal.converter.CustomerConverter;
import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.entity.Customer;
import com.amrit.futsal.model.CustomerDTO;
import com.amrit.futsal.repository.CustomerRepository;
import com.amrit.futsal.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerConverter customerConverter;

    @Override
    public List<CustomerDTO> getAll() {
        return customerConverter.convertToDtoList(customerRepository.findAll());
    }

    @Override
    public CustomerDTO getById(Long id) {
//        Customer customer = customerRepository.getById(id).orElse(new Customer(0L));
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (!optionalCustomer.isPresent()) {
            try {
                throw new Exception("Id -" + id + " not found");
            } catch (Exception e) {
                logger.info(e.toString());
            }
            return null;
        }
        return customerConverter.convertToDto(optionalCustomer.get());
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = customerConverter.convertToEntity(customerDTO);
        Customer save = customerRepository.save(customer);
        return customerConverter.convertToDto(save);
    }

    @Override
    public CustomResponse<CustomerDTO> updateCustomer(CustomerDTO customerDTO) {
        CustomResponse customResponse = new CustomResponse();
        try {

            Map<String, Object> map = new HashMap<>();
            Optional<Customer> optionalCustomer = customerRepository.findById(customerDTO.getId());

            if (!optionalCustomer.isPresent()) {
                customResponse.setStatus(404);
                customResponse.setMessage("Customer with given id doesn't exist");
                return customResponse;
            }
            Customer customer = customerConverter.convertToEntity(customerDTO);
            Customer save = customerRepository.save(customer);
            map.put("customer", customerConverter.convertToDto(save));
            customResponse.setStatus(200);
            customResponse.setMessage("Successfully updated");
            customResponse.setBody(map);
            return customResponse;

        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setStatus(500);
            customResponse.setMessage(e.getMessage());
            return customResponse;
        }
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
