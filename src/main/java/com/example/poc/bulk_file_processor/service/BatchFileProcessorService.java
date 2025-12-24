package com.example.poc.bulk_file_processor.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.poc.bulk_file_processor.entity.FileProcessingAudit;
import com.example.poc.bulk_file_processor.entity.FileStatus;
import com.example.poc.bulk_file_processor.repository.FileProcessingAuditRepository;

@Service
public class BatchFileProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchFileProcessorService.class);
    private final FileProcessingAuditRepository repository;

    public BatchFileProcessorService(FileProcessingAuditRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void processBatch(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // Step 1: Extract filenames and perform one DB query to find existing files.
        List<String> fileNames = files.stream()
                .map(File::getName)
                .collect(Collectors.toList());
        
        List<FileProcessingAudit> existingAudits = repository.findByFileNameIn(fileNames);
        
        Set<String> existingFileNames = existingAudits.stream()
                .map(FileProcessingAudit::getFileName)
                .collect(Collectors.toSet());

        // Step 2: Filter the list in memory to find only new files.
        List<FileProcessingAudit> newAudits = files.stream()
                .filter(file -> !existingFileNames.contains(file.getName()))
                .map(file -> {
                    FileProcessingAudit newRecord = new FileProcessingAudit();
                    newRecord.setFileName(file.getName());
                    newRecord.setFilePath(file.getAbsolutePath());
                    newRecord.setStatus(FileStatus.PENDING);
                    newRecord.setCreatedAt(LocalDateTime.now());
                    return newRecord;
                })
                .collect(Collectors.toList());

        // Step 3: Perform one batch insert for all new files.
        if (!newAudits.isEmpty()) {
            repository.saveAll(newAudits);
            LOGGER.info("Processed a batch of {} new files.", newAudits.size());
        }
    }
}
