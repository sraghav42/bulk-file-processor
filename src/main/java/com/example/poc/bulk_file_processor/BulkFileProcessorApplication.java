package com.example.poc.bulk_file_processor;

import com.example.poc.bulk_file_processor.entity.FileProcessingAudit;
import com.example.poc.bulk_file_processor.entity.FileStatus;
import com.example.poc.bulk_file_processor.repository.FileProcessingAuditRepository;
import com.example.poc.bulk_file_processor.service.FileIngesterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BulkFileProcessorApplication {

	private static final Logger log = LoggerFactory.getLogger(BulkFileProcessorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BulkFileProcessorApplication.class, args);
	}

	/**
	 * This CommandLineRunner bean is for testing purposes only.
	 * It runs on application startup and saves a sample record to the database.
	 * This is a simple way to verify that the database connection and the repository save method are working correctly.
	 * It should be removed for production.
	 */
	// @Bean
	// public CommandLineRunner demo(FileProcessingAuditRepository repository) {
	// 	return (args) -> {
	// 		// Create a new audit record
	// 		FileProcessingAudit newAudit = new FileProcessingAudit();
	// 		newAudit.setFileName("test-on-startup.csv");
	// 		newAudit.setFilePath("/files/test-on-startup.csv");
	// 		newAudit.setStatus(FileStatus.PENDING);
	// 		newAudit.setRetryCount(0);

	// 		// Save it to the database
	// 		FileProcessingAudit savedAudit = repository.save(newAudit);

	// 		// Log the result
	// 		log.info("********************************************************************************");
	// 		log.info("TEST RECORD SAVED ON STARTUP. This is for testing purposes and should be removed.");
	// 		log.info("Saved record details: {}", savedAudit.getFileID());
	// 		log.info("You can check your MS SQL Server database to confirm this record was saved.");
	// 		log.info("********************************************************************************");
	// 	};
	// }

}
