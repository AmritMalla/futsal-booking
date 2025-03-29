package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutsalCompanyRepository extends JpaRepository<FutsalCompany, Long> {

    List<FutsalCompany> findByUser_UserId(Long userId); // Find companies by user ID
}
