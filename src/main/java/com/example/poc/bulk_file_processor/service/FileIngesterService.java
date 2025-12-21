package com.example.poc.bulk_file_processor.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.poc.bulk_file_processor.entity.FileProcessingAudit;
import com.example.poc.bulk_file_processor.entity.FileStatus;
import com.example.poc.bulk_file_processor.repository.FileProcessingAuditRepository;

@Service
public class FileIngesterService {

    private final FileProcessingAuditRepository repository;
    private final Path watchPath;

    @Autowired
    public FileIngesterService(FileProcessingAuditRepository repository,
            @Value("${file.ingester.input-directory}") String readDirectory) throws IOException {
        this.repository = repository;
        this.watchPath = Paths.get(readDirectory);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        this.watchPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        new Thread(() -> {
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path filePath = watchPath.resolve((Path) event.context());
                        File file = filePath.toFile();
                        if (file.getName().endsWith(".xml")) {
                            writeToDB(file);
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void writeToDB(File file) {
        FileProcessingAudit newRecord = new FileProcessingAudit();
        newRecord.setFileName(file.getName());
        newRecord.setFilePath(file.getAbsolutePath());
        newRecord.setStatus(FileStatus.PENDING);
        newRecord.setCreatedAt(LocalDateTime.now());
        repository.save(newRecord);
    }
}
