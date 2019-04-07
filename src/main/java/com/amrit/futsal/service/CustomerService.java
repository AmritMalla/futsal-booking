package com.amrit.futsal.service;

import com.amrit.futsal.model.CustomerDTO;

import java.util.List;

public interface CustomerService {
    List<CustomerDTO> getAll();

    CustomerDTO findById(Long id);

    boolean saveCustomer(CustomerDTO customerDTO);

    boolean updateCustomer(CustomerDTO customerDTO);



}
