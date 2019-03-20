package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer,Long> {

    @Query("select c from Customer c where c.id =:id")
    Customer findCustomerById(@Param("id") Long id);

    @Query("select  c from  Customer c where c.user =:userId")
    Customer findCustomerByUser(@Param("userId")Long id);

    @Query("select  c from Customer c")
    List<Customer> findAll();




}
