package com.amrit.futsal.converter;

import com.amrit.futsal.entity.User;
import com.amrit.futsal.entity.Customer;
import com.amrit.futsal.model.CustomerDTO;
import com.amrit.futsal.repository.UserRepository;
import com.amrit.futsal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CustomerConverter {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserConverter userConverter;

    public CustomerDTO convertToDto(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setFirstName(customer.getFirstName());
        customerDTO.setMiddleName(customer.getMiddleName());
        customerDTO.setLastName(customer.getLastName());
        customerDTO.setPhoneNumber(customer.getPhoneNumber());
        customerDTO.setCity(customer.getCity());
        customerDTO.setAddressLine1(customer.getAddressLine1());
        customerDTO.setAddressLine2(customer.getAddressLine2());
        customerDTO.setUser(customer.getUser().getId());
        return customerDTO;
    }

    public List<CustomerDTO> convertToDtoList(List<Customer> customerList) {
        List<CustomerDTO> customerDTOS = new ArrayList<>();
        Iterator<Customer> customerIterator = customerList.iterator();
        while (customerIterator.hasNext()) {
            customerDTOS.add(convertToDto(customerIterator.next()));
        }
        return customerDTOS;
    }

    public Customer convertToEntity(CustomerDTO customerDTO) {
        Customer customer = new Customer();

        if (customerDTO.getId() != null) {
            customer.setId(customerDTO.getId());
        }
        customer.setId(customerDTO.getId());
        customer.setFirstName(customerDTO.getFirstName());
        customer.setMiddleName(customerDTO.getMiddleName());
        customer.setCity(customerDTO.getCity());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setAddressLine1(customerDTO.getAddressLine1());
        customer.setAddressLine2(customerDTO.getAddressLine2());
        User user = userConverter.convertToEntity(userService.findById(customerDTO.getUser()));
        customer.setUser(user);
        return customer;
    }

    List<Customer> convertToEntityList(List<CustomerDTO> customerDTOS) {
        List<Customer> customerList = new ArrayList<>();
        Iterator<CustomerDTO> customerDTOIterator = customerDTOS.iterator();
        while (customerDTOIterator.hasNext()) {
            customerList.add(convertToEntity(customerDTOIterator.next()));
        }
        return customerList;
    }
}


