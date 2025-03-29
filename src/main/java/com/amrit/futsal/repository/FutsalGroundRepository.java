package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalGround;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutsalGroundRepository extends JpaRepository<FutsalGround, Long> {

    List<FutsalGround> findByFutsalCompany_FutsalId(Long futsalId); // Find grounds by futsal company ID
}
