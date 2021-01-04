package com.example.csvman.config;

import com.example.csvman.model.Person;
import com.example.csvman.processor.PersonItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

  private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

  @Autowired public JobBuilderFactory jobBuilderFactory;

  @Autowired public StepBuilderFactory stepBuilderFactory;

  @Bean
  public FlatFileItemReader<Person> reader() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .resource(new ClassPathResource("sample-data.csv"))
        .delimited()
        .delimiter(";")
        .names(new String[] {"firstName", "lastName"})
        .fieldSetMapper(
            new BeanWrapperFieldSetMapper<>() {
              {
                setTargetType(Person.class);
              }
            })
        .build();
  }

  @Bean
  public PersonItemProcessor processor() {
    return new PersonItemProcessor();
  }

  @Bean
  public ItemWriter<Person> writer() {
    Resource outputResource =
        new FileSystemResource("output/output-" + LocalDateTime.now() + ".csv");
    // Create writer instance
    FlatFileItemWriter<Person> writer = new FlatFileItemWriter<>();

    // Set output file location
    writer.setResource(outputResource);

    // All job repetitions should "append" to same output file
    writer.setAppendAllowed(true);

    // Name field values sequence based on object properties
    writer.setLineAggregator(
        new DelimitedLineAggregator<>() {
          {
            setDelimiter(";");
            setFieldExtractor(
                new BeanWrapperFieldExtractor<>() {
                  {
                    setNames(new String[] {"firstName", "lastName"});
                  }
                });
          }
        });
    return writer;
  }

  @Bean
  public Job importUserJob(Step step1) {
    return jobBuilderFactory
        .get("importUserJob")
        .listener(new JobExecutionListenerSupport())
        .flow(step1)
        .end()
        .build();
  }

  @Bean
  public Step step1(ItemWriter<Person> writer) {
    return stepBuilderFactory
        .get("step1")
        .<Person, Person>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer)
        .build();
  }
}
