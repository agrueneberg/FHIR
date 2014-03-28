package info.agrueneberg.fhir;

import info.agrueneberg.fhir.configurations.ApplicationConfig;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApplicationConfig.class);
    }

}