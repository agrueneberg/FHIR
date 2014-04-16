package info.agrueneberg.fhir.configurations;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "info.agrueneberg.fhir")
@EnableMongoRepositories(basePackages = "info.agrueneberg.fhir.repositories")
public class ApplicationConfig {}