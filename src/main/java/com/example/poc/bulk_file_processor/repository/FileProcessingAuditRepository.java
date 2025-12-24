package com.example.poc.bulk_file_processor.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.poc.bulk_file_processor.entity.FileProcessingAudit;

public interface FileProcessingAuditRepository extends JpaRepository<FileProcessingAudit, Long> {

    List<FileProcessingAudit> findByFileNameIn(Collection<String> fileNames);
}
