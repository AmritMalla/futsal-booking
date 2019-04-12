package com.amrit.futsal.repository;

import com.amrit.futsal.entity.OpenTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpenTimeRepository extends JpaRepository<OpenTime,Long> {




}
