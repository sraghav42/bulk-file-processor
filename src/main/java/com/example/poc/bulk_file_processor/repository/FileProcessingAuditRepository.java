package com.example.poc.bulk_file_processor.repository;

import com.example.poc.bulk_file_processor.entity.FileProcessingAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessingAuditRepository extends JpaRepository<FileProcessingAudit, Long> {

}
