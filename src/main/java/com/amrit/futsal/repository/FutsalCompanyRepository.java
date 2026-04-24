package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FutsalCompanyRepository extends JpaRepository<FutsalCompany, UUID> {

    List<FutsalCompany> findByOwnerId(UUID ownerId);
    
    Optional<FutsalCompany> findByName(String name);
}
