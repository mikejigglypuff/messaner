package messaner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"messaner.config", "messaner.controller", "messaner.DTO", "messaner.model", "messaner.service", "messaner"})
@Slf4j
public class MessanerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessanerApplication.class, args);
	}

}