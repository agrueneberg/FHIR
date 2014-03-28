package info.agrueneberg.fhir.configurations;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "info.agrueneberg.fhir")
public class ApplicationConfig {}