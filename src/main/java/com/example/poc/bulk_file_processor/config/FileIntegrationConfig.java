package com.example.poc.bulk_file_processor.config;

import java.io.File;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.poc.bulk_file_processor.service.BatchFileProcessorService;

@Configuration
@EnableIntegration
public class FileIntegrationConfig {

    @Value("${file.ingester.input-directory}")
    private String inputDirectory;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("batch-processing-");
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel batchChannel() {
        return new ExecutorChannel(taskExecutor());
    }

    @Bean
    @InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "20"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(inputDirectory));
        source.setFilter(new SimplePatternFileListFilter("*.xml"));
        source.setUseWatchService(true);
        source.setWatchEvents(FileReadingMessageSource.WatchEventType.CREATE);
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "batchChannel")
    public MessageHandler aggregator() {
        AggregatingMessageHandler aggregator = new AggregatingMessageHandler(new DefaultAggregatingMessageGroupProcessor());
        aggregator.setCorrelationStrategy(message -> "static-correlation");
        aggregator.setReleaseStrategy(group -> group.size() >= 20);
        aggregator.setMessageStore(new SimpleMessageStore());
        aggregator.setSendPartialResultOnExpiry(true);
        aggregator.setGroupTimeoutExpression(new org.springframework.integration.expression.ValueExpression<>(5000L)); // Release partial batches after 5 seconds of inactivity
        aggregator.setExpireGroupsUponCompletion(true);

        aggregator.setOutputProcessor(group -> group.getMessages().stream()
                .map(message -> (File) message.getPayload())
                .collect(Collectors.toList()));

        return aggregator;
    }

    @Bean
    @ServiceActivator(inputChannel = "batchChannel")
    public MessageHandler batchProcessor(BatchFileProcessorService batchService) {
        return new ServiceActivatingHandler(batchService, "processBatch");
    }
}
