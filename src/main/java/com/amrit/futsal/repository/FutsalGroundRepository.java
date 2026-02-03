package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalGround;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FutsalGroundRepository extends JpaRepository<FutsalGround, UUID> {

    List<FutsalGround> findByCompanyId(UUID companyId);
    
    Optional<FutsalGround> findByName(String name);
    
    List<FutsalGround> findBySurfaceType(String surfaceType);
}
