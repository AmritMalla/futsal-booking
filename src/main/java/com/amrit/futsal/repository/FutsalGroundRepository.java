package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalGround;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FutsalGroundRepository extends JpaRepository<FutsalGround,Long> {


}
