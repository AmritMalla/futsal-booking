package com.amrit.futsal.service;

import com.amrit.futsal.domain.CustomResponse;
import com.amrit.futsal.model.CustomerDTO;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> getAll();

    CustomerDTO getById(Long id);

    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomResponse<CustomerDTO> updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(Long id);
}
