package info.agrueneberg.fhir;

import info.agrueneberg.fhir.configurations.ApplicationConfig;
import org.springframework.boot.SpringApplication;

public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationConfig.class, args);
    }

}