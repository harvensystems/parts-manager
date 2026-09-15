package app.harven.partmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PartManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartManagerApplication.class, args);
    }
}
