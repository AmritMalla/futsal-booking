package com.amrit.futsal.repository;

import com.amrit.futsal.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByOwnerId(UUID ownerId);
    
    List<Report> findByReportType(Report.ReportType reportType);
    
    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Report> findByOwnerIdAndReportType(UUID ownerId, Report.ReportType reportType);
}
