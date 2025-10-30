package br.com.fiap.oficina.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "br.com.fiap.oficina.budget",
        "br.com.fiap.oficina.shared"
})
@EnableAsync
public class BudgetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BudgetServiceApplication.class, args);
    }
}
