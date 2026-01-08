package br.com.fiap.oficina.eureka.controller;

import br.com.fiap.oficina.eureka.config.SwaggerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SwaggerAggregatorController {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerAggregatorController.class);

    @Autowired
    private SwaggerConfig swaggerConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Retorna a configuração do Swagger UI com todos os services descobertos dinamicamente
     */
    @GetMapping(value = "/v3/api-docs/swagger-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> swaggerConfig() {
        List<String> services = swaggerConfig.getRegisteredServices();

        logger.info("Discovered services for Swagger aggregation: {}", services);

        List<Map<String, String>> urls = services.stream()
                .map(serviceName -> {
                    String baseUrl = swaggerConfig.getServiceBaseUrl(serviceName);
                    if (baseUrl != null) {
                        Map<String, String> urlConfig = new HashMap<>();
                        urlConfig.put("name", swaggerConfig.formatServiceName(serviceName));
                        urlConfig.put("url", baseUrl + "v3/api-docs");
                        return urlConfig;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> config = new HashMap<>();
        config.put("urls", urls);
        config.put("displayRequestDuration", true);
        config.put("filter", true);
        config.put("tryItOutEnabled", true);

        return ResponseEntity.ok(config);
    }

    /**
     * Endpoint para listar todos os services registrados e suas URLs de API docs
     */
    @GetMapping(value = "/swagger/services", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listServices() {
        List<String> services = swaggerConfig.getRegisteredServices();

        List<Map<String, String>> serviceList = services.stream()
                .map(serviceName -> {
                    String baseUrl = swaggerConfig.getServiceBaseUrl(serviceName);
                    Map<String, String> serviceInfo = new HashMap<>();
                    serviceInfo.put("name", serviceName);
                    serviceInfo.put("displayName", swaggerConfig.formatServiceName(serviceName));
                    serviceInfo.put("baseUrl", baseUrl);
                    serviceInfo.put("apiDocsUrl", baseUrl != null ? baseUrl + "v3/api-docs" : null);
                    serviceInfo.put("swaggerUiUrl", baseUrl != null ? baseUrl + "swagger-ui.html" : null);
                    return serviceInfo;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalServices", serviceList.size());
        response.put("services", serviceList);
        response.put("aggregatedSwaggerUrl", "/swagger-ui.html");

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint para verificar se o agregador está funcionando
     */
    @GetMapping(value = "/swagger/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> health() {
        List<String> services = swaggerConfig.getRegisteredServices();

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("servicesDiscovered", services.size());
        health.put("services", services);

        return ResponseEntity.ok(health);
    }
}
