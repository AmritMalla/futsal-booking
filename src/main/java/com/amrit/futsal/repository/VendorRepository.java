package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor,Long> {
    @Override
    List<Vendor> findAll();

    @Override
    <S extends Vendor> S saveAndFlush(S s);

    @Override
    Vendor getOne(Long aLong);

    @Override
    boolean existsById(Long aLong);
}
