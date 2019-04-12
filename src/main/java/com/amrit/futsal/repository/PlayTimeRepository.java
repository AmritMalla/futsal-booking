package com.amrit.futsal.repository;

import com.amrit.futsal.entity.PlayTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayTimeRepository extends JpaRepository<PlayTime,Long> {


}
