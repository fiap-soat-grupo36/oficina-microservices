package br.com.fiap.oficina.eureka.config;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Oficina Microservices - API Unificada")
                        .version("1.0.0")
                        .description("Documentação agregada de todos os microserviços da Oficina Mecânica. " +
                                "Use o dropdown no topo para selecionar o microserviço desejado.")
                        .contact(new Contact()
                                .name("FIAP - Grupo 36")
                                .url("https://github.com/fiap-soat-grupo36/oficina-microservices")));
    }

    /**
     * Retorna lista de serviços registrados no Eureka (exceto o próprio Eureka Server)
     */
    public List<String> getRegisteredServices() {
        return eurekaClient.getApplications().getRegisteredApplications()
                .stream()
                .map(Application::getName)
                .map(String::toLowerCase)
                .filter(name -> !name.equals(applicationName.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Retorna a URL base de um serviço registrado no Eureka
     */
    public String getServiceBaseUrl(String serviceName) {
        Application application = eurekaClient.getApplication(serviceName.toUpperCase());
        if (application != null && !application.getInstances().isEmpty()) {
            var instance = application.getInstances().get(0);
            return instance.getHomePageUrl();
        }
        return null;
    }

    /**
     * Formata o nome do serviço para exibição no Swagger UI
     */
    public String formatServiceName(String serviceName) {
        return serviceName.replace("-", " ")
                .substring(0, 1).toUpperCase() + serviceName.replace("-", " ").substring(1);
    }
}
