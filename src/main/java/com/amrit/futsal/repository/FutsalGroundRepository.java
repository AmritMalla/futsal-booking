package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FutsalGroundRepository extends JpaRepository<FutsalDetail,Long> {


}
