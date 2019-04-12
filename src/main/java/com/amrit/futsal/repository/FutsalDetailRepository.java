package com.amrit.futsal.repository;

import com.amrit.futsal.entity.FutsalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FutsalDetailRepository extends JpaRepository<FutsalDetail,Long> {


}
